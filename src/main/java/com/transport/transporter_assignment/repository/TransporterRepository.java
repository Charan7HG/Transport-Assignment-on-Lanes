package com.transport.transporter_assignment.repository;




import org.springframework.data.jpa.repository.JpaRepository;
import com.transport.transporter_assignment.entity.Transporter;

public interface TransporterRepository
        extends JpaRepository<Transporter, Long> {

}

