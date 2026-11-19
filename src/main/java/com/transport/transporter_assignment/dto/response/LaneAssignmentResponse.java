package com.transport.transporter_assignment.dto.response;



public class LaneAssignmentResponse {

    private Long laneId;
    private Long transporterId;

    public LaneAssignmentResponse(Long laneId, Long transporterId) {
        this.laneId = laneId;
        this.transporterId = transporterId;
    }

    public Long getLaneId() {
        return laneId;
    }

    public Long getTransporterId() {
        return transporterId;
    }
}

