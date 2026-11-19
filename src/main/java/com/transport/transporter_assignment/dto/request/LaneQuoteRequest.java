package com.transport.transporter_assignment.dto.request;


import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class LaneQuoteRequest {

    @NotNull
    @Positive
    private Long laneId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quote;

    public Long getLaneId() {
        return laneId;
    }

    public void setLaneId(Long laneId) {
        this.laneId = laneId;
    }

    public BigDecimal getQuote() {
        return quote;
    }

    public void setQuote(BigDecimal quote) {
        this.quote = quote;
    }
}
