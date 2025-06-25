# 💼 Personal Finance Manager

A secure, RESTful web application for managing personal financial transactions. Built with **Spring Boot**.It enables users to access and manage transactions securely using **JWT**.

---

## 🎯 Objective

The goal is to build a full-featured finance manager where users can:
- Register & log in securely
- Manage their income and expenses
- Track their account balance
- Ensure secure access using JWT

---

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring Security + JWT**
- **Spring Data JPA (Hibernate)**
- **H2 Database (File-based & In-Memory)**
- **JUnit 5 + Mockito**

---

## 🚀 Features

- 🔐 **User Authentication**
  - Registration and login with JWT-based authentication
- 💸 **Transaction Management**
  - Create, read, update, delete (CRUD)
  - Each transaction belongs to a logged-in user only
- 💰 **Balance Calculation**
  - Net balance = Income - Expense
- ✅ **Access Control**
  - Users can only access and modify their own transactions
- 🧪 **Unit Tests**
  - JUnit 5 and Mockito test coverage for services and controllers
- 🔍 **Error Handling**
  - Centralized global exception handling with informative error responses

---

## 🚀 Setup

### Prerequisites

- Java 17+
- Maven

### Run Locally

```bash
# Clone
git clone https://github.com/your-repo/personal-finance-manager.git
cd personal-finance-manager

# Build
mvn clean install

# Run
mvn spring-boot:run
```

---

## Challenges

- Setting up jwt authentication: Configuring jwt in spring security was initially confusing. It was hard for me to get the token from the request and validate it. In the end, I found the solution by looking at online docs and using AI tools to learn the concepts with good examples until the authentication flow worked properly.

- At first, access to anyone’s deletion of transactions was allowable for the user.  A logic that checks if a person has authenticated before allowing access to a particular resource took some time. Adding proper checks in the service layer fixed this.

- I noticed some faults that were returning a response of 500 (Internal Server Error) while they were supposed to rely on 403 (Forbidden) or 404 (Not Found). To fix this issue, I created a custom exception handler annotated with @RestControllerAdvice in the GlobalException to send the expected HTTP response code.

- I found writing tests of JUnit 5 and Mockito difficult. At the first, I couldn't figure out how to mock services and repositories. After trying it out for a while and after inspecting the sample test case, I have succeeded in writing the test for my project.

- Learning Project Structure: Figuring out how to arrange a Spring Boot app (controllers, services, repositories etc.) took a while. As I learnt better practices, I kept refactoring the code and improved the quality.

---
