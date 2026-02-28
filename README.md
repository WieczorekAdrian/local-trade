[![CI Build Status](https://img.shields.io/github/actions/workflow/status/WieczorekAdrian/local-trade/build-and-test.yml?branch=main&style=for-the-badge)](https://github.com/WieczorekAdrian/local-trade/actions)
[![Test Coverage](https://img.shields.io/badge/coverage-80+%25-brightgreen?style=for-the-badge)](https://shields.io)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Java 17](https://img.shields.io/badge/Java-17-blue.svg?style=for-the-badge)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg?style=for-the-badge)](https://spring.io/projects/spring-boot)
[![Live Demo - Frontend](https://img.shields.io/badge/Live_Demo-Vercel-black?style=for-the-badge&logo=vercel)](https://local-trade-frontend.vercel.app)
[![Swagger API](https://img.shields.io/badge/API_Docs-Swagger-85EA2D?style=for-the-badge&logo=swagger)](https://local-trade.francecentral.cloudapp.azure.com/swagger-ui.html)
# local-trade: Backend API Platform

A full-featured Spring Boot backend REST API for a local advertisement trading platform.
It supports user listings, messaging, ratings, and media management — designed for scalability and real-world deployment.

## Live Deployment & Performance

The platform is fully deployed and operational.

**Infrastructure Highlight & Low-Resource Optimization:**
The entire backend stack (Spring Boot, PostgreSQL, Redis, RabbitMQ, MinIO) is successfully hosted on a highly constrained, cost-effective cloud instance (**Azure B1s - 1 vCPU, 1GB RAM**). To guarantee system stability and sub-100ms response times under such strict hardware limits, several low-level optimizations were implemented:

* **Container Tuning:** Swapped standard Debian-based Docker images for lightweight **Alpine Linux / Amazon Corretto** distributions.
* **Resource Orchestration:** Implemented strict Docker Compose memory limits to prevent out-of-memory (OOM) kills and guarantee database stability.
* **Aggressive Caching:** Redis caching and asynchronous RabbitMQ processing allow the API to drop latency from ~64ms (cold DB read) to **~2ms** for cached queries.
* **Cold-Start Elimination:** Configured custom OS-level cron keep-alive scripts and optimized Linux `swappiness` to prevent the hypervisor from paging JVM memory to disk during idle periods.
* **Reverse Proxy & SSL Termination:** Nginx serves as the single entry point for the platform. It handles full HTTPS encryption, manages CORS policies for the Vercel-hosted frontend, and securely routes traffic to the internal Docker network, keeping the underlying Spring Boot application and databases shielded from direct public access.

## Tech Stack
- **Java 17**
- **Spring Boot** (REST API, WebSocket chat, validation)
- **Frontend (In Development):** React, Vite, TypeScript
- **PostgreSQL** – relational database
- **RabbitMQ** – asynchronous message broker
- **Azure Blob Storage** – **target cloud storage solution**
- **Redis** – caching and distributed JWT Blacklist (for secure logout)
- **MinIO / AWS S3** – image storage and thumbnail generation  
- **Testcontainers** – integration testing environment  
- **Maven** – build and dependency management  
- **Docker & Docker Compose** – containerized deployment  
- **GitHub Actions** – CI/CD pipeline
- **Code Quality:** Qodana, Jacoco, **Spotless (Automated Code Formatting)**
- **k6** – Load & Performance Testing


## Core Features
- Advanced Security: Implementation of stateless authentication using **Secure & HttpOnly Cookies** to store JWT tokens. This approach mitigates XSS attacks. Features include Refresh Token Rotation and Redis-based Blacklisting for immediate token revocation (True Logout).
- S3/MinIO integration with automatic image thumbnail generation
- "Add to favourites" and user listing tracking
- Real-time chat using WebSockets
- Categories and filtering with pagination
- Rating system with transaction completion logic
- Redis caching for performance optimization
- Swagger UI for API documentation
- One-command startup with Docker Compose
- Environment Configuration
- Event-Driven Architecture: Asynchronous messaging via RabbitMQ for notifications.

## Testing & Quality Assurance
This project places a strong emphasis on code quality and reliability.

- 80%+ Code Test Coverage (verified by Jacoco).
- Over 200 unit and integration tests.
- Testcontainers are used for full end-to-end integration tests with real instances of PostgreSQL, Redis, MinIO, and RabbitMQ in isolated containers.
- CI/CD Pipeline (GitHub Actions) automatically builds and tests the application on every commit.
- Static code analysis and quality monitoring using Qodana

## Environment Configuration

The application is configured via environment variables. A template with all required variables is provided in the `.env.example` file.

Simply copy it to `.env` and adjust the values:
```bash
cp .env.example .env
```

## Local Development Setup

Clone the project and start all services using Docker Compose:

```bash
git clone https://github.com/WieczorekAdrian/local-trade.git (https://github.com/WieczorekAdrian/local-trade.git)

cd local-trade

docker-compose up --build
```

Swagger UI will be available at:
http://localhost:8080/swagger-ui.html

## Performance & Scalability
Beyond functional correctness, this project is engineered for high performance under load. The testing strategy focuses on **Critical Paths** and high-risk scenarios to validate system stability under stress using **k6**.

### Redis Caching Impact
Benchmarks demonstrate a **~30x latency reduction** for cached resources, effectively offloading read traffic from the primary database.
- **Cold Request (PostgreSQL):** ~64ms latency (Database I/O + Transaction overhead)
- **Warm Request (Redis Cache):** ~2ms latency (In-Memory access)

### High-Load Multipart Uploads
Robust file handling validated under heavy concurrency.
- **Scenario:** Simultaneous 5MB image uploads by multiple concurrent users.
- **Optimization:** Implemented **SharedArray** and memory-efficient streaming in k6 scripts to prevent client-side OOM and simulate real-world binary streams.
- **Stability:** Achieved **0% Error Rate** with correct transaction handling (SQL rollback + S3 cleanup) during stress tests.


## Testing
The project utilizes Testcontainers to run integration tests in isolated, containerized environments.
To execute all tests:

```bash
mvn test
```

This command automatically provisions PostgreSQL, Redis, and MinIO containers for the integration test suite.

### API documentation is available via the following endpoints:
Swagger UI: /swagger-ui.html
, OpenAPI v3 Specification: /v3/api-docs

### CI/CD Pipeline
- The GitHub Actions workflow executes the following:

- Build and test using Maven

- Code quality checks via Qodana

- Static analysis and test coverage reports


### Architectural Overview
- **Multi-service Monorepo:** The platform is built as a monorepo containing independent services (e.g., `main-api`, `notification-service`) orchestrated via Docker Compose.
- **Event-Driven Design:** Services communicate asynchronously using **RabbitMQ** for decoupled operations (e.g., triggering emails or notifications), ensuring resilience and non-blocking API responses.
- **Modular & Domain-Centric:** The core service enforces **strict domain isolation**. Cross-domain repository access is prohibited (e.g., *Trade Domain* interacts with *Advertisement Domain* only via Service interfaces), which prevents tight coupling and ensures clean boundaries.
- **Infrastructure-as-Code (Local):** The entire stack (DB, Cache, Broker, Storage) is fully containerized using Docker.
- **Reliability & Performance First:** Integration tests run against real infrastructure using **Testcontainers**. Critical system paths (e.g., file uploads, caching strategies) are further validated with **k6 load tests** to ensure stability under concurrency.
- **Security-First Approach:** Implements defense-in-depth strategies. Access tokens are short-lived, refresh tokens are rotated upon every use, and logout actions instantaneously invalidate tokens via Redis, mitigating token theft risks.

##  Project Roadmap

### Completed
* **Core Backend Architecture:** Domain-centric Spring Boot API with isolated services.
* **Event-Driven Messaging:** RabbitMQ integration for async notifications.
* **Frontend Client:** Fully responsive SPA built with React, Vite, and TypeScript.
* **Cloud Infrastructure:** Containerized deployment (Docker Compose) on Azure cloud.
* **Advanced Security:** Stateless JWT authentication with HttpOnly cookies, Reverse Proxy CORS bypass, and Redis token blacklisting.

### In Progress / Planned
* **Dynamic Filters:** Implementing a fully dynamic, category-specific filtering system using JSONB attributes in PostgreSQL.
* **AI-based Image Moderation:** Automated scanning of uploaded advertisements.

# Contributing to Local Trade Platform

Hi! I'm happy you want to contribute. This is a portfolio project, but I treat it seriously.

## How to start?
1. Pick an issue.
2. Fork the repository.
3. Create a branch (`feat/your-feature` or `fix/issue-number`).
4. **Run tests!** (`mvn test`). Ensure everything is green.
5. Submit a Pull Request.

## Requirements
- Java 17
- Docker (for Testcontainers)

### License
This project is licensed under the MIT License.
See the LICENSE file for details.

## Author

Adrian Wieczorek

GitHub: @WieczorekAdrian
