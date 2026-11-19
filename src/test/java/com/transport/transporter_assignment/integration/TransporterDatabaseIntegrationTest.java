package com.transport.transporter_assignment.integration;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.transport.transporter_assignment.repository.LaneRepository;
import com.transport.transporter_assignment.repository.TransporterRepository;
import com.transport.transporter_assignment.repository.LaneQuoteRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransporterDatabaseIntegrationTest {
    @Autowired
private MockMvc mockMvc;

    @Autowired
    private LaneRepository laneRepository;

    @Autowired
    private TransporterRepository transporterRepository;

    @Autowired
    private LaneQuoteRepository laneQuoteRepository;


@Test
void shouldSaveInputDataIntoDatabase() throws Exception {

    String request = """
            {
                "lanes": [
                    {
                        "id": 1,
                        "origin": "Bengaluru",
                        "destination": "Chennai"
                    },
                    {
                        "id": 2,
                        "origin": "Bengaluru",
                        "destination": "Hyderabad"
                    }
                ],
                "transporters": [
                    {
                        "id": 1,
                        "name": "Transporter A",
                        "laneQuotes": [
                            {
                                "laneId": 1,
                                "quote": 100.00
                            },
                            {
                                "laneId": 2,
                                "quote": 150.00
                            }
                        ]
                    },
                    {
                        "id": 2,
                        "name": "Transporter B",
                        "laneQuotes": [
                            {
                                "laneId": 1,
                                "quote": 120.00
                            },
                            {
                                "laneId": 2,
                                "quote": 90.00
                            }
                        ]
                    }
                ]
            }
            """;

    mockMvc.perform(post("/api/v1/transporters/input")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));

    // Verify actual database records
    assertEquals(2, laneRepository.count());

    assertEquals(2, transporterRepository.count());

    assertEquals(4, laneQuoteRepository.count());
}

@Test
void shouldGenerateOptimalAssignmentUsingDatabaseData() throws Exception {

    String inputRequest = """
            {
                "lanes": [
                    {
                        "id": 1,
                        "origin": "Bengaluru",
                        "destination": "Chennai"
                    },
                    {
                        "id": 2,
                        "origin": "Bengaluru",
                        "destination": "Hyderabad"
                    }
                ],
                "transporters": [
                    {
                        "id": 1,
                        "name": "Transporter A",
                        "laneQuotes": [
                            {"laneId": 1, "quote": 100.00},
                            {"laneId": 2, "quote": 150.00}
                        ]
                    },
                    {
                        "id": 2,
                        "name": "Transporter B",
                        "laneQuotes": [
                            {"laneId": 1, "quote": 120.00},
                            {"laneId": 2, "quote": 90.00}
                        ]
                    }
                ]
            }
            """;

    // Step 1: Save input
    mockMvc.perform(post("/api/v1/transporters/input")
            .contentType(MediaType.APPLICATION_JSON)
            .content(inputRequest))
            .andExpect(status().isOk());

    // Step 2: Request optimal assignment
    String assignmentRequest = """
            {
                "maxTransporters": 2
            }
            """;

    mockMvc.perform(post("/api/v1/transporters/assignment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(assignmentRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.totalCost").value(190))
            .andExpect(jsonPath("$.assignments.length()").value(2))
            .andExpect(jsonPath("$.selectedTransporters.length()").value(2));
}



    @Test
    void shouldConnectToTestDatabase() {

        assertNotNull(laneRepository);
        assertNotNull(transporterRepository);
        assertNotNull(laneQuoteRepository);
    }
}

