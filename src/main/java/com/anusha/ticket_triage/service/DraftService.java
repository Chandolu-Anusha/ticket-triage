package com.anusha.ticket_triage.service;

import com.anusha.ticket_triage.model.TicketAnalysis;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class DraftService {

    private final ChatClient chatClient;

    public DraftService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String draftReply(String ticketMessage, TicketAnalysis analysis) {
        String userPrompt = """
                Customer ticket: %s

                Category: %s
                Sentiment: %s
                Urgency: %s
                """.formatted(
                ticketMessage,
                analysis.category(),
                analysis.sentiment(),
                analysis.urgency());

        return chatClient.prompt()
                .system("""
                        You are a polite customer support agent for a
                        subscription-based SaaS company.
                        Write a short, professional email reply to the customer.
                        If the customer is angry or frustrated, start with a
                        sincere apology.
                        Address the exact problem they mentioned.
                        Do not promise refunds or dates you cannot guarantee;
                        say the team will review and follow up.
                        Sign off as "Customer Support Team".
                        """)
                .user(userPrompt)
                .call()
                .content();
    }
}