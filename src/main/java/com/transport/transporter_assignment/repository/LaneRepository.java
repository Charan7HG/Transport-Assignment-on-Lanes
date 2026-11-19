package com.transport.transporter_assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.transport.transporter_assignment.entity.Lane;

public interface LaneRepository extends JpaRepository<Lane, Long> {

}

