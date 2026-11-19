package com.transport.transporter_assignment.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.transport.transporter_assignment.dto.request.AssignmentRequest;
import com.transport.transporter_assignment.dto.request.TransporterInputRequest;
import com.transport.transporter_assignment.dto.response.AssignmentResponse;
import com.transport.transporter_assignment.dto.response.InputResponse;
import com.transport.transporter_assignment.service.TransporterAssignmentService;
import com.transport.transporter_assignment.service.TransporterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transporters")
public class TransporterController {

    private final TransporterService transporterService;
    private final TransporterAssignmentService assignmentService;

    public TransporterController(
        TransporterService transporterService,
        TransporterAssignmentService assignmentService) {

    this.transporterService = transporterService;
    this.assignmentService = assignmentService;
}

    @PostMapping("/input")
    public ResponseEntity<InputResponse> saveInput(
            @Valid @RequestBody TransporterInputRequest request) {

        InputResponse response = transporterService.saveInput(request);

        return ResponseEntity.ok(response);
    }
@PostMapping("/assignment")
public ResponseEntity<AssignmentResponse> assignTransporters(
        @Valid @RequestBody AssignmentRequest request) {

    AssignmentResponse response =
            assignmentService.assignTransporters(
                    request.getMaxTransporters()
            );

    return ResponseEntity.ok(response);
}




}
