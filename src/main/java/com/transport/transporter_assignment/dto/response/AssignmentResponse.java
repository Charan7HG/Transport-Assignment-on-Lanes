package com.transport.transporter_assignment.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class AssignmentResponse {

    private String status;
    private BigDecimal totalCost;
    private List<LaneAssignmentResponse> assignments;
    private List<Long> selectedTransporters;

    public AssignmentResponse(
            String status,
            BigDecimal totalCost,
            List<LaneAssignmentResponse> assignments,
            List<Long> selectedTransporters) {

        this.status = status;
        this.totalCost = totalCost;
        this.assignments = assignments;
        this.selectedTransporters = selectedTransporters;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public List<LaneAssignmentResponse> getAssignments() {
        return assignments;
    }

    public List<Long> getSelectedTransporters() {
        return selectedTransporters;
    }
}

