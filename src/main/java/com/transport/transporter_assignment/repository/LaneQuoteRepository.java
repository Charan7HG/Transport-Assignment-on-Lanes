package com.transport.transporter_assignment.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.transport.transporter_assignment.entity.LaneQuote;
import java.util.List;

public interface LaneQuoteRepository
        extends JpaRepository<LaneQuote, Long> {
        List<LaneQuote> findAllByOrderByLane_IdAsc();

}

