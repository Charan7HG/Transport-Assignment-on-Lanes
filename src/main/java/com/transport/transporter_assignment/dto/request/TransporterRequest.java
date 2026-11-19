package com.transport.transporter_assignment.dto.request;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransporterRequest {

    @NotNull
    @Positive
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Valid
    private List<LaneQuoteRequest> laneQuotes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LaneQuoteRequest> getLaneQuotes() {
        return laneQuotes;
    }

    public void setLaneQuotes(List<LaneQuoteRequest> laneQuotes) {
        this.laneQuotes = laneQuotes;
    }
}
