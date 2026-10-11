package com.anusha.ticket_triage.service;

import com.anusha.ticket_triage.entity.Ticket;
import com.anusha.ticket_triage.model.Category;
import com.anusha.ticket_triage.model.Sentiment;
import com.anusha.ticket_triage.model.TicketAnalysis;
import com.anusha.ticket_triage.model.Urgency;
import com.anusha.ticket_triage.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private AnalysisService analysisService;

    @Mock
    private DraftService draftService;

    @Mock
    private TicketRepository ticketRepository;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(
                analysisService, new RoutingService(), draftService, ticketRepository);
    }

    @Test
    void billingTicketIsAnalyzedRoutedDraftedAndSaved() {
        when(analysisService.analyze(anyString()))
                .thenReturn(new TicketAnalysis(Category.BILLING, Sentiment.ANGRY, Urgency.HIGH));
        when(draftService.draftReply(anyString(), any(TicketAnalysis.class)))
                .thenReturn("Sorry about the double charge.");
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.processTicket("a@example.com", "Charged twice!");

        assertEquals(Category.BILLING, result.getCategory());
        assertEquals(Sentiment.ANGRY, result.getSentiment());
        assertEquals(Urgency.HIGH, result.getUrgency());
        assertEquals("FINANCE_TEAM", result.getAssignedTeam());
        assertEquals("Sorry about the double charge.", result.getDraftReply());
        assertEquals("a@example.com", result.getCustomerEmail());
    }

    @Test
    void ticketIsStillSavedWithDefaultsWhenAnalysisFails() {
        when(analysisService.analyze(anyString()))
                .thenThrow(new RuntimeException("Gemini is down"));
        when(draftService.draftReply(anyString(), any(TicketAnalysis.class)))
                .thenReturn("Some draft");
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.processTicket("b@example.com", "Hello");

        assertEquals(Category.GENERAL, result.getCategory());
        assertEquals(Sentiment.NEUTRAL, result.getSentiment());
        assertEquals(Urgency.MEDIUM, result.getUrgency());
        assertEquals("GENERAL_SUPPORT_TEAM", result.getAssignedTeam());
    }

    @Test
    void standardReplyIsUsedWhenDraftingFails() {
        when(analysisService.analyze(anyString()))
                .thenReturn(new TicketAnalysis(Category.TECHNICAL, Sentiment.FRUSTRATED, Urgency.HIGH));
        when(draftService.draftReply(anyString(), any(TicketAnalysis.class)))
                .thenThrow(new RuntimeException("Gemini is down"));
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.processTicket("c@example.com", "App crashes");

        assertEquals("TECH_SUPPORT_TEAM", result.getAssignedTeam());
        assertTrue(result.getDraftReply().contains("Thank you for contacting us"));
    }
}