package com.transport.transporter_assignment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transport.transporter_assignment.dto.response.AssignmentResponse;
import com.transport.transporter_assignment.dto.response.LaneAssignmentResponse;
import com.transport.transporter_assignment.entity.Lane;
import com.transport.transporter_assignment.entity.LaneQuote;
import com.transport.transporter_assignment.entity.Transporter;
import com.transport.transporter_assignment.repository.LaneQuoteRepository;
import com.transport.transporter_assignment.repository.LaneRepository;
import com.transport.transporter_assignment.repository.TransporterRepository;

@Service
public class TransporterAssignmentService {

    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;

    public TransporterAssignmentService(
            LaneRepository laneRepository,
            TransporterRepository transporterRepository,
            LaneQuoteRepository laneQuoteRepository) {

        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
    }

    @Transactional(readOnly = true)
    public AssignmentResponse assignTransporters(int maxTransporters) {
     

        if (maxTransporters <= 0) {
    throw new IllegalArgumentException(
            "Maximum transporters must be greater than zero."
    );
}
        // 1. Load data from database
      List<Lane> lanes = new ArrayList<>(laneRepository.findAll());

      List<Transporter> transporters =
        new ArrayList<>(transporterRepository.findAll());

        List<LaneQuote> quotes =
                laneQuoteRepository.findAllByOrderByLane_IdAsc();

        if (lanes.isEmpty() || transporters.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please submit input data before requesting assignments."
            );
        }

        lanes.sort(Comparator.comparing(Lane::getId));
        transporters.sort(Comparator.comparing(Transporter::getId));

        // 2. Create quote lookup map
        Map<Long, Map<Long, BigDecimal>> quoteMap = new HashMap<>();

        for (LaneQuote quote : quotes) {

            Long laneId = quote.getLane().getId();
            Long transporterId = quote.getTransporter().getId();

            quoteMap
                    .computeIfAbsent(laneId, key -> new HashMap<>())
                    .put(transporterId, quote.getQuote());
        }

        // 3. Calculate maximum feasible transporter count
        int maximum = Math.min(
                maxTransporters,
                Math.min(transporters.size(), lanes.size())
        );

        // Java int bitmask supports at most 30 selected transporters
        if (maximum > 30) {
            throw new IllegalArgumentException(
                    "Bitmask assignment supports at most 30 transporters."
            );
        }

        // 4. Try transporter counts from maximum down to 1
        for (int count = maximum; count >= 1; count--) {

            List<List<Transporter>> combinations = new ArrayList<>();

            generateCombinations(
                    transporters,
                    count,
                    0,
                    new ArrayList<>(),
                    combinations
            );

            AssignmentResponse bestResult = null;

            BigDecimal bestCost = null;

            // 5. Evaluate every combination
            for (List<Transporter> combination : combinations) {

                AssignmentResponse result = solveCombination(
                        lanes,
                        combination,
                        quoteMap
                );

                if (result == null) {
                    continue;
                }

                if (bestCost == null ||
                        result.getTotalCost().compareTo(bestCost) < 0) {

                    bestCost = result.getTotalCost();
                    bestResult = result;
                }
            }

            // Maximum feasible transporter count found
            if (bestResult != null) {
                return bestResult;
            }
        }

        throw new IllegalStateException(
                "No feasible assignment exists for the supplied transporter limit."
        );
    }

    // Generate all combinations of the required size
    private void generateCombinations(
            List<Transporter> transporters,
            int requiredCount,
            int startIndex,
            List<Transporter> current,
            List<List<Transporter>> result) {

        if (current.size() == requiredCount) {

            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = startIndex; i < transporters.size(); i++) {

            current.add(transporters.get(i));

            generateCombinations(
                    transporters,
                    requiredCount,
                    i + 1,
                    current,
                    result
            );

            current.remove(current.size() - 1);
        }
    }

    // Find minimum-cost assignment for one fixed transporter combination
    private AssignmentResponse solveCombination(
            List<Lane> lanes,
            List<Transporter> combination,
            Map<Long, Map<Long, BigDecimal>> quoteMap) {

        int laneCount = lanes.size();
        int transporterCount = combination.size();

        int totalMasks = 1 << transporterCount;

        int fullMask = totalMasks - 1;

        // dp[i][mask] = minimum cost after assigning first i lanes
        // with the transporter usage represented by mask
        BigDecimal[][] dp =
                new BigDecimal[laneCount + 1][totalMasks];

        // Store previous mask for assignment reconstruction
        int[][] previousMasks =
                new int[laneCount + 1][totalMasks];

        // Store the transporter chosen for each transition
        int[][] chosenTransporters =
                new int[laneCount + 1][totalMasks];

        // Initialize predecessor arrays
        for (int i = 0; i <= laneCount; i++) {

            for (int mask = 0; mask < totalMasks; mask++) {

                previousMasks[i][mask] = -1;
                chosenTransporters[i][mask] = -1;
            }
        }

        // Base condition
        dp[0][0] = BigDecimal.ZERO;

        // Process lanes one by one
        for (int i = 0; i < laneCount; i++) {

            Lane lane = lanes.get(i);

            Map<Long, BigDecimal> laneQuotes =
                    quoteMap.get(lane.getId());

            if (laneQuotes == null) {
                continue;
            }

            // Check every previous DP state
            for (int mask = 0; mask < totalMasks; mask++) {

                if (dp[i][mask] == null) {
                    continue;
                }

                // Try assigning this lane to every selected transporter
                for (int j = 0; j < transporterCount; j++) {

                    Transporter transporter = combination.get(j);

                    BigDecimal quote = laneQuotes.get(transporter.getId());

                    // Transporter has no quote for this lane
                    if (quote == null) {
                        continue;
                    }

                    int newMask = mask | (1 << j);

                    BigDecimal newCost = dp[i][mask].add(quote);

                    // Keep only the cheapest cost for this state
                    if (dp[i + 1][newMask] == null ||
                            newCost.compareTo(dp[i + 1][newMask]) < 0) {

                        dp[i + 1][newMask] = newCost;

                        previousMasks[i + 1][newMask] = mask;

                        chosenTransporters[i + 1][newMask] = j;
                    }
                }
            }
        }

        // All selected transporters must be used
        if (dp[laneCount][fullMask] == null) {
            return null;
        }

        // Reconstruct the assignments
        List<LaneAssignmentResponse> assignments = new ArrayList<>();

        int currentMask = fullMask;

        for (int i = laneCount; i >= 1; i--) {

            int transporterIndex =
                    chosenTransporters[i][currentMask];

            int previousMask =
                    previousMasks[i][currentMask];

            Transporter transporter =
                    combination.get(transporterIndex);

            Lane lane = lanes.get(i - 1);

            assignments.add(
                    new LaneAssignmentResponse(
                            lane.getId(),
                            transporter.getId()
                    )
            );

            currentMask = previousMask;
        }

        // Reverse because reconstruction moves backwards
        java.util.Collections.reverse(assignments);

        List<Long> selectedTransporters = new ArrayList<>();

        for (Transporter transporter : combination) {
            selectedTransporters.add(transporter.getId());
        }

        return new AssignmentResponse(
                "success",
                dp[laneCount][fullMask],
                assignments,
                selectedTransporters
        );
    }
}
