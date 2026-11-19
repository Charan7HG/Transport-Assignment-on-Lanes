package com.transport.transporter_assignment.service;




import com.transport.transporter_assignment.entity.Lane;
import com.transport.transporter_assignment.entity.LaneQuote;
import com.transport.transporter_assignment.entity.Transporter;
import com.transport.transporter_assignment.repository.LaneQuoteRepository;
import com.transport.transporter_assignment.repository.LaneRepository;
import com.transport.transporter_assignment.repository.TransporterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransporterAssignmentServiceTest {

    @Mock
    private LaneRepository laneRepository;

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private LaneQuoteRepository laneQuoteRepository;

    @InjectMocks
    private TransporterAssignmentService assignmentService;

    private Lane lane1;
    private Lane lane2;
    private Lane lane3;

    private Transporter transporter1;
    private Transporter transporter2;
    private Transporter transporter3;

    @BeforeEach
    void setUp() {
        lane1 = createLane(1L, "Mumbai", "Delhi");
        lane2 = createLane(2L, "Delhi", "Bangalore");
        lane3 = createLane(3L, "Chennai", "Kolkata");

        transporter1 = createTransporter(1L, "Transporter A");
        transporter2 = createTransporter(2L, "Transporter B");
        transporter3 = createTransporter(3L, "Transporter C");
    }

    @Test
    void shouldAssignAllLanesToOneTransporterWhenOnlyOneIsAllowed() {

        List<LaneQuote> quotes = List.of(
                createQuote(lane1, transporter1, "20000"),
                createQuote(lane2, transporter1, "15000"),

                createQuote(lane1, transporter2, "18000"),
                createQuote(lane2, transporter2, "17000"),
                createQuote(lane3, transporter2, "22000"),

                createQuote(lane3, transporter3, "19000")
        );

        mockRepositories(quotes);

        var response = assignmentService.assignTransporters(1);

        assertEquals("success", response.getStatus());
        assertEquals(0, new BigDecimal("57000")
                .compareTo(response.getTotalCost()));

        assertEquals(1, response.getSelectedTransporters().size());
        assertEquals(List.of(2L), response.getSelectedTransporters());

        assertEquals(3, response.getAssignments().size());

        assertTrue(response.getAssignments()
                .stream()
                .allMatch(a -> a.getTransporterId().equals(2L)));
    }

    @Test
    void shouldMaximizeTransporterUsageAndMinimizeTotalCost() {

        List<LaneQuote> quotes = List.of(
                createQuote(lane1, transporter1, "20000"),
                createQuote(lane2, transporter1, "15000"),

                createQuote(lane1, transporter2, "18000"),
                createQuote(lane2, transporter2, "17000"),
                createQuote(lane3, transporter2, "22000"),

                createQuote(lane3, transporter3, "19000")
        );

        mockRepositories(quotes);

        var response = assignmentService.assignTransporters(3);

        assertEquals(0, new BigDecimal("52000")
                .compareTo(response.getTotalCost()));

        assertEquals(3, response.getSelectedTransporters().size());
        assertEquals(List.of(1L, 2L, 3L),
                response.getSelectedTransporters());

        assertEquals(3, response.getAssignments().size());

        assertEquals(2L, response.getAssignments().get(0).getTransporterId());
        assertEquals(1L, response.getAssignments().get(1).getTransporterId());
        assertEquals(3L, response.getAssignments().get(2).getTransporterId());
    }

    @Test
    void shouldThrowExceptionWhenNoSingleTransporterCanCoverAllLanes() {

        List<LaneQuote> quotes = List.of(
                createQuote(lane1, transporter1, "20000"),
                createQuote(lane2, transporter2, "15000"),
                createQuote(lane3, transporter3, "19000")
        );

        mockRepositories(quotes);

        assertThrows(
                IllegalStateException.class,
                () -> assignmentService.assignTransporters(1)
        );
    }

@Test
void shouldNotExceedAvailableTransporterCount() {

    List<LaneQuote> quotes = List.of(
            createQuote(lane1, transporter1, "20000"),
            createQuote(lane2, transporter1, "15000"),

            createQuote(lane1, transporter2, "18000"),
            createQuote(lane2, transporter2, "17000"),
            createQuote(lane3, transporter2, "22000"),

            createQuote(lane3, transporter3, "19000")
    );

    mockRepositories(quotes);

    var response = assignmentService.assignTransporters(10);

    assertEquals(3, response.getSelectedTransporters().size());

    assertEquals(0, new BigDecimal("52000")
            .compareTo(response.getTotalCost()));
}

@Test
void shouldThrowExceptionWhenNoCombinationCanCoverAllLanes() {

    List<LaneQuote> quotes = List.of(
            createQuote(lane1, transporter1, "20000"),
            createQuote(lane2, transporter2, "15000")
            // No transporter has a quote for lane 3.
    );

    mockRepositories(quotes);

    assertThrows(
            IllegalStateException.class,
            () -> assignmentService.assignTransporters(3)
    );
}

//unit test cases started
@Test
void shouldThrowExceptionWhenMaximumTransportersIsZero() {

    assertThrows(
            IllegalArgumentException.class,
            () -> assignmentService.assignTransporters(0)
    );
}
@Test
void shouldThrowExceptionWhenMaximumTransportersIsNegative() {

    assertThrows(
            IllegalArgumentException.class,
            () -> assignmentService.assignTransporters(-1)
    );
}
@Test
void shouldThrowExceptionWhenNoLanesAreAvailable() {

    mockRepositories(List.of());

    when(laneRepository.findAll()).thenReturn(List.of());

    assertThrows(
            IllegalArgumentException.class,
            () -> assignmentService.assignTransporters(3)
    );
}
@Test
void shouldThrowExceptionWhenNoTransportersAreAvailable() {

    mockRepositories(List.of());

    when(transporterRepository.findAll()).thenReturn(List.of());

    assertThrows(
            IllegalArgumentException.class,
            () -> assignmentService.assignTransporters(3)
    );
}

//unit test cases ended

@Test
void shouldUseMaximumFeasibleTransportersBeforeMinimizingCost() {

    List<LaneQuote> quotes = List.of(
            createQuote(lane1, transporter1, "100"),
            createQuote(lane2, transporter1, "100"),

            createQuote(lane1, transporter2, "50"),
            createQuote(lane2, transporter2, "50"),

            createQuote(lane2, transporter3, "60")
    );

    mockRepositories(quotes);
    when(laneRepository.findAll())
        .thenReturn(List.of(lane1, lane2));

    var response = assignmentService.assignTransporters(3);

    assertEquals(2, response.getSelectedTransporters().size());

    assertEquals(0, new BigDecimal("110")
            .compareTo(response.getTotalCost()));
}


    private void mockRepositories(List<LaneQuote> quotes) {
        when(laneRepository.findAll())
                .thenReturn(List.of(lane1, lane2, lane3));

        when(transporterRepository.findAll())
                .thenReturn(List.of(
                        transporter1,
                        transporter2,
                        transporter3
                ));

        when(laneQuoteRepository.findAllByOrderByLane_IdAsc())
                .thenReturn(quotes);
    }

    private Lane createLane(Long id, String origin, String destination) {
        Lane lane = new Lane();
        lane.setId(id);
        lane.setOrigin(origin);
        lane.setDestination(destination);
        return lane;
    }

    private Transporter createTransporter(Long id, String name) {
        Transporter transporter = new Transporter();
        transporter.setId(id);
        transporter.setName(name);
        return transporter;
    }

    private LaneQuote createQuote(
            Lane lane,
            Transporter transporter,
            String amount
    ) {
        LaneQuote quote = new LaneQuote();
        quote.setLane(lane);
        quote.setTransporter(transporter);
        quote.setQuote(new BigDecimal(amount));
        return quote;
    }
}
