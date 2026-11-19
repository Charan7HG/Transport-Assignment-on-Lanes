
package com.transport.transporter_assignment.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(
    name = "lane_quotes",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"lane_id", "transporter_id"}
        )
    }
)
public class LaneQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lane_id", nullable = false)
    private Lane lane;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transporter_id", nullable = false)
    private Transporter transporter;

    @Column(name = "quote_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal quote;

    public LaneQuote() {
    }

    public Long getId() {
        return id;
    }

    public Lane getLane() {
        return lane;
    }

    public void setLane(Lane lane) {
        this.lane = lane;
    }

    public Transporter getTransporter() {
        return transporter;
    }

    public void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }

    public BigDecimal getQuote() {
        return quote;
    }

    public void setQuote(BigDecimal quote) {
        this.quote = quote;
    }
}
