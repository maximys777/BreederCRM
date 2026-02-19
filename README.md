# Breeder CRM

A specialized CRM system for professional dog breeders to manage leads, secure access, and automate sales processes.

## Key Features

* **Role-Based Access Control (RBAC):** Secure access for Admins, Owners, Editors, and Users using **Spring Security**.
* **2FA Authentication:** Custom implementation using **Redis (TTL)** and **Telegram Bot API** for OTP delivery.
* **Media Storage:** Integration with **AWS S3** for scalable image hosting.
* **Database Management:** Schema migration handling via **Liquibase**.

## Technology Stack

* **Core:** Java 21, Spring Boot 4
* **Security:** Spring Security, JWT
* **Data:** PostgreSQL, Spring Data JPA, Redis
* **DevOps:** Docker, AWS S3
* **Testing:** JUnit, Mockito

##  Architecture (Work in Progress)

Currently migrating from a monolithic structure to a more modular approach.
* [x] User Authentication & Security
* [x] Telegram Notification Service
