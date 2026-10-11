package com.anusha.ticket_triage.controller;

import com.anusha.ticket_triage.entity.Ticket;
import com.anusha.ticket_triage.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public record TicketRequest(
            @NotBlank @Email String customerEmail,
            @NotBlank String message
    ) {
    }

    @PostMapping
    public Ticket createTicket(@Valid @RequestBody TicketRequest request) {
        return ticketService.processTicket(request.customerEmail(), request.message());
    }
    @GetMapping
    public List<Ticket> getTickets(@RequestParam(required = false) String team) {
        if (team != null) {
            return ticketService.getTeamQueue(team);
        }
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }
}