package com.anusha.ticket_triage.service;

import com.anusha.ticket_triage.entity.Ticket;
import com.anusha.ticket_triage.model.TicketAnalysis;
import com.anusha.ticket_triage.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Comparator;

@Service
public class TicketService {

    private final AnalysisService analysisService;
    private final RoutingService routingService;
    private final DraftService draftService;
    private final TicketRepository ticketRepository;

    public TicketService(AnalysisService analysisService,
                         RoutingService routingService,
                         DraftService draftService,
                         TicketRepository ticketRepository) {
        this.analysisService = analysisService;
        this.routingService = routingService;
        this.draftService = draftService;
        this.ticketRepository = ticketRepository;
    }

    public Ticket processTicket(String customerEmail, String message) {
        // 1. Ask the LLM to analyze the ticket
        TicketAnalysis analysis = analysisService.analyze(message);

        // 2. Decide the team using our own rules
        String team = routingService.assignTeam(analysis.category());

        // 3. Ask the LLM to draft a reply
        String draft = draftService.draftReply(message, analysis);

        // 4. Fill the entity and save it
        Ticket ticket = new Ticket();
        ticket.setCustomerEmail(customerEmail);
        ticket.setMessage(message);
        ticket.setCategory(analysis.category());
        ticket.setSentiment(analysis.sentiment());
        ticket.setUrgency(analysis.urgency());
        ticket.setAssignedTeam(team);
        ticket.setDraftReply(draft);

        return ticketRepository.save(ticket);
    }
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found with id " + id));
    }
    public List<Ticket> getTeamQueue(String team) {
        return ticketRepository.findByAssignedTeam(team).stream()
                .sorted(Comparator.comparing(Ticket::getUrgency).reversed())
                .toList();
    }
}