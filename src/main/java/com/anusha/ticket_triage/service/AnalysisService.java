package com.anusha.ticket_triage.service;

import com.anusha.ticket_triage.model.TicketAnalysis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final ChatClient chatClient;

    public AnalysisService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public TicketAnalysis analyze(String ticketMessage) {
        return chatClient.prompt()
                .system("""
                        You are a customer support triage assistant for a
                        subscription-based SaaS company.
                        Read the customer's ticket and classify it.
                        Choose the category, sentiment and urgency only from
                        the allowed values.
                        """)
                .user(ticketMessage)
                .call()
                .entity(TicketAnalysis.class);
    }
}