package com.anusha.ticket_triage.model;

public record TicketAnalysis(
        Category category,
        Sentiment sentiment,
        Urgency urgency
) {
}