package com.transport.transporter_assignment.dto.request;



import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class TransporterInputRequest {

    @NotEmpty
    @Valid
    private List<LaneRequest> lanes;

    @NotEmpty
    @Valid
    private List<TransporterRequest> transporters;

    public List<LaneRequest> getLanes() {
        return lanes;
    }

    public void setLanes(List<LaneRequest> lanes) {
        this.lanes = lanes;
    }

    public List<TransporterRequest> getTransporters() {
        return transporters;
    }

    public void setTransporters(List<TransporterRequest> transporters) {
        this.transporters = transporters;
    }
}