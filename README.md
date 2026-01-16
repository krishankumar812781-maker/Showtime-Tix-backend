# 🎬 SeatBooker: A Resilient & Concurrent Movie Ticketing API

This project is a complete, production-grade backend API for a movie ticket booking platform, built from the ground up with Spring Boot.

The primary goal of this project was not just to build a simple API, but to solve a critical, real-world backend challenge: **preventing the "race condition"** where two users try to book the exact same seat at the exact same time. This was achieved by implementing **pessimistic locking** at the database level.

This application is built as a **modular monolith** that is secure, resilient, and event-driven.

-----

## 🎯 Core Features & Outcomes

This project demonstrates proficiency in several advanced, high-demand backend skills.

### 🔒 1. Solved Critical Concurrency with Pessimistic Locking

  * **Outcome:** Successfully prevented the "double booking" race condition.
  * **How:** When a user attempts to book, the `BookingService` uses a **`@Lock(LockModeType.PESSIMISTIC_WRITE)`** . This tells the database (MySQL) to place an exclusive lock on the selected `ShowSeat` rows.
  * **Proof:** A multi-threaded **integration test** (using `CountDownLatch` and `ExecutorService`) was written to simulate two users attempting to book the same seat simultaneously. The test **passes**, proving that one user successfully books the seat while the other correctly receives an exception, ensuring 100% data integrity.

### 🔑 2. Implemented a Secure, Stateless Authentication System

  * **Outcome:** Built a complete, production-ready auth system from scratch using Spring Security.
  * **How:**
      * **JWT (JSON Web Tokens):** The API is stateless. A user logs in and receives a short-lived `accessToken`.
      * **Rolling Refresh Tokens:** To provide a seamless "never log me out" user experience (like YouTube or Netflix), a long-lived `refreshToken` is also issued. This token can be used to get a new `accessToken` and is automatically "rolled" (re-issued) on each use to enhance security.
      * **Role-Based Access Control (RBAC):** Endpoints are secured based on user roles (e.g., `ROLE_USER` vs. `ROLE_ADMIN`).

### 🚀 3. Built an Event-Driven Architecture with Kafka

  * **Outcome:** Decoupled the application's services to improve resilience and scalability.
  * **How:** The application publishes events to Kafka for significant business actions.
      * **Producers:** The `BookingService` and `AuthService` use `KafkaTemplate` to send "fire and forget" messages. This ensures that a primary action (like user registration) succeeds *even if* a secondary service (like sending an email) is down.
      * **Consumers:** A `NotificationService` uses `@KafkaListener` to listen for topics like `user-registered-topic` and `booking-confirmed-topic` to handle asynchronous tasks (like sending welcome emails).
      * **Critical Events:** For critical events (like `show-cancelled`), This correctly uses the `@Transactional` nature of the method to **roll back** the entire operation if the refund/notification message fails to send, preventing data corruption.

### 🗺️ 4. Integrated Third-Party APIs for Data Enrichment

  * **Outcome:** Demonstrated the ability to consume external REST APIs to add value to the application's data.
  * **How:** When an admin creates a new `Theater`, the `TheaterService` makes a server-to-server call to the **OpenStreetMap (Nominatim) Geocoding API**. It parses the JSON response and saves the `latitude` and `longitude` to the database, allowing a frontend to later display the theater on a map.

### ✅ 5. Implemented Comprehensive Testing

  * **Outcome:** Ensured code reliability and correctness.
  * **How:**
      * **Unit Tests:** Used **Mockito** to test services like `AuthService` in isolation.
      * **Integration Tests:** Used SpringBootTest with an MySql database to test the full application context, including the pessimistic locking concurrency test.

-----

## 🏗️ Technical Architecture

This application is a **Modular Monolith** connected to a containerized environment managed by Docker Compose.

1.  A **Client** (like Postman) sends a request.
2.  The **Spring Boot Application** handles it:
      * `SecurityConfig` & `JwtFilter` intercept the request to check for a valid JWT.
      * The **Controller** routes the request to the correct **Service**.
      * The **Service** (e.g., `BookingService`) executes the business logic, locks the database rows, and saves the data.
      * The **Repository** (Spring Data JPA) maps the Java objects to the **MySQL Database**.
3.  **Asynchronously**, the Service sends an event to the **Kafka** broker.
4.  A **Consumer** (`NotificationService`) receives the event and performs a secondary action.

-----

## 💻 Technologies Used

  * **Backend:** Spring Boot, Spring Security, Spring Data JPA
  * **Database:** MySQL (Production)
  * **Messaging:** Apache Kafka
  * **Testing:** JUnit 5, Mockito, Spring Boot Test
  * **DevOps:** Docker, Docker Compose
  * **Utilities:** Lombok, ModelMapper, JWT

-----
