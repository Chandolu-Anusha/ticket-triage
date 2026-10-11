package com.anusha.ticket_triage.service;

import com.anusha.ticket_triage.model.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingServiceTest {

    private final RoutingService routingService = new RoutingService();

    @Test
    void billingTicketGoesToFinanceTeam() {
        assertEquals("FINANCE_TEAM", routingService.assignTeam(Category.BILLING));
    }

    @Test
    void technicalTicketGoesToTechSupportTeam() {
        assertEquals("TECH_SUPPORT_TEAM", routingService.assignTeam(Category.TECHNICAL));
    }

    @Test
    void accountTicketGoesToAccountTeam() {
        assertEquals("ACCOUNT_TEAM", routingService.assignTeam(Category.ACCOUNT));
    }

    @Test
    void generalTicketGoesToGeneralSupportTeam() {
        assertEquals("GENERAL_SUPPORT_TEAM", routingService.assignTeam(Category.GENERAL));
    }
}