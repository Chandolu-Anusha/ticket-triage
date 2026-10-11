package com.anusha.ticket_triage.repository;

import com.anusha.ticket_triage.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByAssignedTeam(String assignedTeam);
}