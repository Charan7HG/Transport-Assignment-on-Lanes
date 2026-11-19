package com.transport.transporter_assignment.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AssignmentRequest {

    @NotNull
    @Min(1)
    private Integer maxTransporters;

    public Integer getMaxTransporters() {
        return maxTransporters;
    }

    public void setMaxTransporters(Integer maxTransporters) {
        this.maxTransporters = maxTransporters;
    }
}

