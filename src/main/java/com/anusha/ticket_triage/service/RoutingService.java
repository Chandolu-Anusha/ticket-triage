package com.anusha.ticket_triage.service;

import com.anusha.ticket_triage.model.Category;
import org.springframework.stereotype.Service;

@Service
public class RoutingService {

    public String assignTeam(Category category) {
        return switch (category) {
            case BILLING -> "FINANCE_TEAM";
            case TECHNICAL -> "TECH_SUPPORT_TEAM";
            case ACCOUNT -> "ACCOUNT_TEAM";
            case GENERAL -> "GENERAL_SUPPORT_TEAM";
        };
    }
}