# AI Interview Preparation Platform

An AI-powered mock interview platform that generates personalized interview questions based on your resume, evaluates your answers in real time, and provides detailed feedback with scoring.

Built with **Java 17**, **Spring Boot 3**, **PostgreSQL**, and the **Google Gemini API**.

## Features

- **Resume Upload & Analysis** — Upload a PDF resume. The system extracts text using Apache PDFBox and sends it to Gemini API for structured skill extraction and gap analysis.
- **AI-Generated Questions** — Start a mock interview by selecting a target role and difficulty. The AI generates contextual questions based on your resume and avoids repetition using conversation history.
- **Real-Time Answer Evaluation** — Submit answers and receive instant feedback scored on three axes: Relevance, Depth, and Clarity (0-10 each).
- **Session Management** — Track your interview history, view past sessions, and monitor your improvement over time.
- **Dashboard Analytics** — View overall stats, per-skill performance breakdown, and recent session history.
- **JWT Authentication** — Secure user registration and login with Spring Security 6 and JWT tokens.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3, Spring Security 6, Spring MVC |
| AI Integration | Google Gemini API, Prompt Engineering, RAG (PGVector) |
| Database | PostgreSQL, JPA/Hibernate |
| PDF Parsing | Apache PDFBox 3.0 |
| Auth | JWT (jjwt), BCrypt |
| Frontend | Next.js, React (coming soon) |
| DevOps | Docker, Docker Compose |

## Architecture

```
┌──────────────┐     ┌──────────────────┐     ┌─────────────┐
│   Frontend   │────▶│  Spring Boot API  │────▶│  PostgreSQL │
│   (Next.js)  │◀────│  (REST + SSE)     │◀────│  (PGVector) │
└──────────────┘     └────────┬─────────┘     └─────────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │  Google Gemini   │
                     │     API         │
                     └─────────────────┘
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |

### Resume
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/resume/upload` | Upload PDF resume for AI analysis |

### Interview
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/interview/start` | Start a new interview session |
| GET | `/api/interview/{id}/question` | Get the next AI-generated question |
| POST | `/api/interview/answer` | Submit answer and get scored feedback |
| POST | `/api/interview/{id}/end` | End session and get summary |
| GET | `/api/interview/history` | View all past sessions |
| GET | `/api/interview/session/{id}` | View detailed session with Q&A |

### Dashboard & Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats` | Get user analytics and stats |
| GET | `/api/user/profile` | Get user profile info |

## Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 15+
- Maven 3.8+
- Gemini API key from [Google AI Studio](https://aistudio.google.com)

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/ShivamChauhan-108/interviewai.git
cd interviewai
```

2. **Create the PostgreSQL database**
```sql
CREATE DATABASE interviewai_db;
```

3. **Configure application.yaml**

Update `src/main/resources/application.yaml` with your database credentials and Gemini API key:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/interviewai_db
    username: postgres
    password: your_password

gemini:
  api:
    key: your_gemini_api_key
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`

5. **Test with Postman**

Register a user:
```json
POST /api/auth/register
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

## Project Structure

```
src/main/java/com/project/interviewai/
├── config/          # Security, JWT filter, CORS, RestTemplate
├── controller/      # REST controllers (Auth, Resume, Interview, Dashboard)
├── dto/             # Request/Response DTOs
├── exception/       # Custom exceptions and global handler
├── model/           # JPA entities (User, InterviewSession, InterviewQA)
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic (AI, JWT, Resume parsing, Interview)
```

## License

This project is for educational and portfolio purposes.
