package com.anusha.ticket_triage.service;

import com.anusha.ticket_triage.entity.Ticket;
import com.anusha.ticket_triage.model.TicketAnalysis;
import com.anusha.ticket_triage.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Comparator;
import com.anusha.ticket_triage.model.Category;
import com.anusha.ticket_triage.model.Sentiment;
import com.anusha.ticket_triage.model.Urgency;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        // 1. Analyze with the LLM, fall back to safe defaults if it fails
        TicketAnalysis analysis;
        try {
            analysis = analysisService.analyze(message);
            if (analysis == null || analysis.category() == null
                    || analysis.sentiment() == null || analysis.urgency() == null) {
                throw new IllegalStateException("Incomplete analysis from LLM");
            }
        } catch (Exception e) {
            log.warn("LLM analysis failed, using defaults: {}", e.getMessage());
            analysis = new TicketAnalysis(Category.GENERAL, Sentiment.NEUTRAL, Urgency.MEDIUM);
        }

        // 2. Decide the team using our own rules
        String team = routingService.assignTeam(analysis.category());

        // 3. Draft a reply, fall back to a standard message if the LLM fails
        String draft;
        try {
            draft = draftService.draftReply(message, analysis);
        } catch (Exception e) {
            log.warn("LLM draft failed, using standard reply: {}", e.getMessage());
            draft = "Thank you for contacting us. We have received your request "
                    + "and our team will review it and get back to you shortly.\n\n"
                    + "Best regards,\nCustomer Support Team";
        }

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