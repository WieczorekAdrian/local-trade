[![CI Build Status](https://img.shields.io/github/actions/workflow/status/WieczorekAdrian/local-trade/build-and-test.yml?branch=main&style=for-the-badge)](https://github.com/WieczorekAdrian/local-trade/actions)
[![Test Coverage](https://img.shields.io/badge/coverage-88%25-brightgreen?style=for-the-badge)](https://shields.io)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Java 17](https://img.shields.io/badge/Java-17-blue.svg?style=for-the-badge)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg?style=for-the-badge)](https://spring.io/projects/spring-boot)
# local-trade: Backend API Platform

A full-featured Spring Boot backend REST API for a local advertisement trading platform.
It supports user listings, messaging, ratings, and media management — designed for scalability and real-world deployment.

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

- 88% Code Test Coverage (verified by Jacoco).
- Over 200 unit and integration tests.
- Testcontainers are used for full end-to-end integration tests with real instances of PostgreSQL, Redis, MinIO, and RabbitMQ in isolated containers.
- CI/CD Pipeline (GitHub Actions) automatically builds and tests the application on every commit.
- Static code analysis and quality monitoring using Qodana

## Environment Configuration

The application requires the following environment variables. Example .env file:
```
DB_NAME= Postgres db name
DB_USER= Postgres db username
DB_PASSWORD= Postgres db password
JWT_SECRET= JWT Secret Key hs256 encrypted
```

### S3 / MinIO
```
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
S3_ENDPOINT=http://minio:9000
S3_ACCESSKEY=minioadmin
S3_SECRETKEY=minioadmin
AWS_REGION=eu-central-1
```
The application uses the s3.useMinio property to switch between storage providers:
```
s3.useMinio=true (Default)
```
Uses MinIO for local development.

This integration is fully confirmed by the test suite, which provisions a MinIO instance using Testcontainers.
```
s3.useMinio=false
```
Switches the configuration to use a live AWS S3 bucket.

**Cloud Note:** The project currently supports MinIO (local) and AWS S3. The roadmap includes a planned full pivot to **Azure Blob Storage** and production deployment on the Azure platform.

This profile requires additional configuration (like AWS credentials, region, and bucket name) to be provided in the relevant application-secret.yml profile.

### Redis configuration
```
REDIS_HOST=redis
REDIS_PORT=6379
```

### The application-secret.yml (or relevant profile) should reference these environment variables.


## Local Development Setup

Clone the project and start all services using Docker Compose:

```bash
git clone https://github.com/WieczorekAdrian/local-trade.git (https://github.com/WieczorekAdrian/local-trade.git)

cd local-trade

docker-compose up --build
```

Swagger UI will be available at:
http://localhost:8080/swagger-ui.html


## Testing
The project utilizes Testcontainers to run integration tests in isolated, containerized environments.
To execute all tests:

```bash
mvn test
```

This command automatically provisions PostgreSQL, Redis, and MinIO containers for the integration test suite.

## API Documentation

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
- **Reliability First:** Integration tests run against real infrastructure instances using **Testcontainers**, not mocks, guaranteeing production-like behavior during testing.
- **Security-First Approach:** Implements defense-in-depth strategies. Access tokens are short-lived, refresh tokens are rotated upon every use, and logout actions instantaneously invalidate tokens via Redis, mitigating token theft risks.

### License
This project is licensed under the MIT License.
See the LICENSE file for details.

### Project Roadmap

[IN PROGRESS] Frontend Development: Building a modern, responsive UI using React, Vite, and TypeScript.

[IN PROGRESS] Dynamic Filters: Implementing a fully dynamic, category-specific filtering system (similar to OLX/Allegro) using JSONB attributes in the database.

[IN PROGRESS] User Dashboard: Creating an aggregated BFF (Backend-for-Frontend) endpoint for the user dashboard.

[PLANNED] Adding user notification preferences.

[PLANNED] Azure Deployment and Storage Pivot.


[PLANNED] AI-based Image Moderation: Integration with an external API for image moderation.

[PLANNED] **Social-Marketplace Pivot:** Transitioning into a Social Commerce platform. 
  - Implementation of a **Global Feed** combining advertisements with social posts.
  - Enhanced User Profiles featuring professional history and social proof (LinkedIn-style).
  - Advanced social interactions (likes, shares, following system).

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

## Author

Adrian Wieczorek

GitHub: @WieczorekAdrian
