package com.transport.transporter_assignment.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transport.transporter_assignment.dto.request.LaneQuoteRequest;
import com.transport.transporter_assignment.dto.request.LaneRequest;
import com.transport.transporter_assignment.dto.request.TransporterInputRequest;
import com.transport.transporter_assignment.dto.request.TransporterRequest;
import com.transport.transporter_assignment.dto.response.InputResponse;
import com.transport.transporter_assignment.entity.Lane;
import com.transport.transporter_assignment.entity.LaneQuote;
import com.transport.transporter_assignment.entity.Transporter;
import com.transport.transporter_assignment.repository.LaneQuoteRepository;
import com.transport.transporter_assignment.repository.LaneRepository;
import com.transport.transporter_assignment.repository.TransporterRepository;

@Service
public class TransporterService {

    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;

    public TransporterService(
            LaneRepository laneRepository,
            TransporterRepository transporterRepository,
            LaneQuoteRepository laneQuoteRepository) {

        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
    }

    @Transactional
    public InputResponse saveInput(TransporterInputRequest request) {

        // 1. Validate the input data
        validateInput(request);

        // 2. Replace the previous dataset
        laneQuoteRepository.deleteAllInBatch();
        laneRepository.deleteAllInBatch();
        transporterRepository.deleteAllInBatch();

        // 3. Convert and save lanes
        List<Lane> lanes = new ArrayList<>();

        for (LaneRequest laneRequest : request.getLanes()) {

            Lane lane = new Lane();

            lane.setId(laneRequest.getId());
            lane.setOrigin(laneRequest.getOrigin());
            lane.setDestination(laneRequest.getDestination());

            lanes.add(lane);
        }

        laneRepository.saveAll(lanes);

        // 4. Convert and save transporters
        List<Transporter> transporters = new ArrayList<>();

        Map<Long, Transporter> transporterMap = new HashMap<>();

        for (TransporterRequest transporterRequest : request.getTransporters()) {

            Transporter transporter = new Transporter();

            transporter.setId(transporterRequest.getId());
            transporter.setName(transporterRequest.getName());

            transporters.add(transporter);

            transporterMap.put(transporter.getId(), transporter);
        }

        transporterRepository.saveAll(transporters);

        // 5. Create and save lane quotes
        Map<Long, Lane> laneMap = new HashMap<>();

        for (Lane lane : lanes) {
            laneMap.put(lane.getId(), lane);
        }

        List<LaneQuote> quotes = new ArrayList<>();

        for (TransporterRequest transporterRequest : request.getTransporters()) {

            Transporter transporter =
                    transporterMap.get(transporterRequest.getId());

            for (LaneQuoteRequest quoteRequest :
                    transporterRequest.getLaneQuotes()) {

                LaneQuote laneQuote = new LaneQuote();

                Lane lane = laneMap.get(quoteRequest.getLaneId());

                laneQuote.setLane(lane);
                laneQuote.setTransporter(transporter);
                laneQuote.setQuote(quoteRequest.getQuote());

                quotes.add(laneQuote);
            }
        }

        laneQuoteRepository.saveAll(quotes);

        return new InputResponse(
                "success",
                "Input data saved successfully."
        );
    }

    private void validateInput(TransporterInputRequest request) {

        Set<Long> laneIds = new HashSet<>();

        // Check duplicate lane IDs
        for (LaneRequest lane : request.getLanes()) {

            if (!laneIds.add(lane.getId())) {
                throw new IllegalArgumentException(
                        "Duplicate lane ID: " + lane.getId()
                );
            }
        }

        Set<Long> transporterIds = new HashSet<>();

        Set<Long> coveredLaneIds = new HashSet<>();

        // Check transporter IDs and their quotes
        for (TransporterRequest transporter : request.getTransporters()) {

            if (!transporterIds.add(transporter.getId())) {
                throw new IllegalArgumentException(
                        "Duplicate transporter ID: " + transporter.getId()
                );
            }

            Set<Long> quotedLaneIds = new HashSet<>();

            for (LaneQuoteRequest quote : transporter.getLaneQuotes()) {

                Long laneId = quote.getLaneId();

                // Check whether the quoted lane exists
                if (!laneIds.contains(laneId)) {
                    throw new IllegalArgumentException(
                            "Quote references unknown lane ID: " + laneId
                    );
                }

                // Check duplicate quotes for the same transporter and lane
                if (!quotedLaneIds.add(laneId)) {
                    throw new IllegalArgumentException(
                            "Duplicate quote for lane ID " + laneId
                                    + " from transporter ID "
                                    + transporter.getId()
                    );
                }

                coveredLaneIds.add(laneId);
            }
        }

        // Every lane must have at least one available quote
        for (Long laneId : laneIds) {

            if (!coveredLaneIds.contains(laneId)) {
                throw new IllegalArgumentException(
                        "No transporter quote available for lane ID: " + laneId
                );
            }
        }
    }
}

