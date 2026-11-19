package com.transport.transporter_assignment.controller;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import tools.jackson.databind.ObjectMapper;

import com.transport.transporter_assignment.dto.request.AssignmentRequest;
import com.transport.transporter_assignment.dto.response.AssignmentResponse;
import com.transport.transporter_assignment.dto.response.InputResponse;
import com.transport.transporter_assignment.dto.response.LaneAssignmentResponse;
import com.transport.transporter_assignment.service.TransporterAssignmentService;
import com.transport.transporter_assignment.service.TransporterService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransporterController.class)
class TransporterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransporterService transporterService;

    @MockitoBean
    private TransporterAssignmentService assignmentService;

    @Test
    void shouldReturnSuccessfulAssignment() throws Exception {

        AssignmentRequest request = new AssignmentRequest();
        request.setMaxTransporters(2);

        AssignmentResponse response = new AssignmentResponse(
                "success",
                new BigDecimal("110"),
                List.of(
                        new LaneAssignmentResponse(1L, 2L),
                        new LaneAssignmentResponse(2L, 3L)
                ),
                List.of(2L, 3L)
        );

        when(assignmentService.assignTransporters(2))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transporters/assignment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.totalCost").value(110))
                .andExpect(jsonPath("$.assignments.length()").value(2))
                .andExpect(jsonPath("$.selectedTransporters.length()").value(2));
    }

    @Test
    void shouldRejectZeroMaximumTransporters() throws Exception {

        String request = """
                {
                    "maxTransporters": 0
                }
                """;

        mockMvc.perform(post("/api/v1/transporters/assignment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void shouldRejectMissingMaximumTransporters() throws Exception {

        String request = "{}";

        mockMvc.perform(post("/api/v1/transporters/assignment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenNoAssignmentExists()
            throws Exception {

        String request = """
                {
                    "maxTransporters": 2
                }
                """;

        when(assignmentService.assignTransporters(2))
                .thenThrow(new IllegalStateException(
                        "No feasible assignment exists."
                ));

        mockMvc.perform(post("/api/v1/transporters/assignment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
void shouldRejectNegativeMaximumTransporters() throws Exception {

    String request = """
            {
                "maxTransporters": -1
            }
            """;

    mockMvc.perform(post("/api/v1/transporters/assignment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"));
}
@Test
void shouldReturnInternalServerErrorForUnexpectedException()
        throws Exception {

    String request = """
            {
                "maxTransporters": 2
            }
            """;

    when(assignmentService.assignTransporters(2))
            .thenThrow(new RuntimeException("Database failure"));

    mockMvc.perform(post("/api/v1/transporters/assignment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.message")
                    .value("An unexpected error occurred."));
}
@Test
void shouldRejectEmptyTransporters() throws Exception {

    String request = """
            {
                "lanes": [
                    {
                        "id": 1,
                        "origin": "Bengaluru",
                        "destination": "Chennai"
                    }
                ],
                "transporters": []
            }
            """;

    mockMvc.perform(post("/api/v1/transporters/input")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"));
}
@Test
void shouldRejectZeroLaneQuote() throws Exception {

    String request = """
            {
                "lanes": [
                    {
                        "id": 1,
                        "origin": "Bengaluru",
                        "destination": "Chennai"
                    }
                ],
                "transporters": [
                    {
                        "id": 1,
                        "name": "Transporter A",
                        "laneQuotes": [
                            {
                                "laneId": 1,
                                "quote": 0
                            }
                        ]
                    }
                ]
            }
            """;

    mockMvc.perform(post("/api/v1/transporters/input")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"));
}

    @Test
    void shouldSaveInputSuccessfully() throws Exception {

        String request = """
                {
                    "lanes": [
                        {
                            "id": 1,
                            "origin": "Bengaluru",
                            "destination": "Chennai"
                        }
                    ],
                    "transporters": [
                        {
                            "id": 1,
                            "name": "Transporter A",
                            "laneQuotes": [
                                {
                                    "laneId": 1,
                                    "quote": 1000.00
                                }
                            ]
                        }
                    ]
                }
                """;

        when(transporterService.saveInput(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(new InputResponse(
                "success",
                "Input data saved successfully."
        ));

        mockMvc.perform(post("/api/v1/transporters/input")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message")
                        .value("Input data saved successfully."));
    }

    @Test
    void shouldRejectEmptyLanes() throws Exception {

        String request = """
                {
                    "lanes": [],
                    "transporters": [
                        {
                            "id": 1,
                            "name": "Transporter A",
                            "laneQuotes": []
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/transporters/input")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

}

