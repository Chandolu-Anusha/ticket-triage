# AI Support Ticket Triage

A Spring Boot backend that reads a raw customer support ticket, uses an LLM to understand it, routes it to the right team, and drafts a reply for a human agent to review.

## How it works

1. **Ingest:** `POST /tickets` receives the customer email and message.
2. **Analyze (LLM):** Spring AI asks Gemini to return the category, sentiment and urgency as a typed Java object (structured output).
3. **Route:** Plain Java rules assign the ticket to a team queue based on the category.
4. **Draft:** The LLM writes a polite reply using the ticket and the analysis.
5. **Store:** The ticket, analysis, team and draft are saved in MySQL.
6. **Queue:** Agents can read tickets, or a team queue sorted by urgency.

Example: "I was charged twice for my subscription" becomes `BILLING`, `ANGRY`, `HIGH`, assigned to `FINANCE_TEAM`, with a drafted apology email.

## Tech stack

- Java 21, Spring Boot 4
- Spring AI (OpenAI-compatible client, pointed at Google Gemini)
- Spring Data JPA, Hibernate, MySQL
- Lombok, Jakarta Validation
- Maven

## Design choices

- **Enums for LLM output** (`Category`, `Sentiment`, `Urgency`): the LLM can only answer with allowed values, so downstream code is safe.
- **Routing is deterministic Java code.** The LLM only classifies; business decisions are not left to the model.
- **Provider-independent:** the services depend on Spring AI's `ChatClient`, so the LLM provider can be changed through configuration.
- **Secrets are read from environment variables**, never stored in the repository.

## Project structure

```
controller/   REST endpoints
service/      AnalysisService, RoutingService, DraftService, TicketService
repository/   TicketRepository (Spring Data JPA)
entity/       Ticket
model/        Category, Sentiment, Urgency, TicketAnalysis
```

## Run locally

**Prerequisites:** JDK 21, MySQL 8, a Gemini API key from Google AI Studio.

1. Create the database in MySQL:
```sql
   CREATE DATABASE `ticket-triage`;
```
2. Set environment variables:
    - `GEMINI_API_KEY` = your Gemini API key
    - `DB_PASSWORD` = your MySQL password
3. If your MySQL user is not `root`, change `spring.datasource.username` in `application.properties`.
4. Run `TicketTriageApplication` (or `mvn spring-boot:run`).

The app starts on `http://localhost:8080`.

## API

| Method | URL | Description |
|---|---|---|
| POST | `/tickets` | Create and process a ticket |
| GET | `/tickets` | List all tickets |
| GET | `/tickets?team=FINANCE_TEAM` | Team queue, most urgent first |
| GET | `/tickets/{id}` | Get one ticket (404 if not found) |

**Create a ticket**

```json
POST /tickets
{
  "customerEmail": "test.customer@example.com",
  "message": "I was charged twice for my subscription this month, fix this now!"
}
```

**Response (example)**

```json
{
  "id": 1,
  "category": "BILLING",
  "sentiment": "ANGRY",
  "urgency": "HIGH",
  "assignedTeam": "FINANCE_TEAM",
  "draftReply": "Hello, I sincerely apologize for the frustration...",
  "createdAt": "2026-10-11T15:48:06"
}
```

## Notes

- Use fake tickets while testing. Free-tier prompts to Gemini may be used by Google to improve its products.
- Drafts are meant to be reviewed by a human agent before sending.
- Possible next steps: ticket status updates, authentication, tests, a simple agent dashboard.