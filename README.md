> **Note**: This is a fork of the original [MoraGames/registra-domini](https://github.com/MoraGames/registra-domini) project, developed for the "Sistemi Distribuiti" course. This version includes several fixes, refactoring, and functional improvements to the original codebase.

---

> TL;DR: The original project was a university exam. This fork fixes it and makes it fully functional. If you're curious, here follows a nice-looking GPT-generated README with added details on the improvements.

# Registra Domini 🧠💻

A **university exam project** focused on understanding and implementing the **core principles of backend system design** — from the logic and data storage layer to a functional frontend interface.

---

## 🎯 Project Goal

The main objective of **Registra Domini** is to **design and build a complete backend system from scratch**, without relying on external database engines or ORM frameworks.  
The project simulates the process of building an entire data-driven web application — from low-level storage logic to user interaction — in order to fully understand how backend architectures work internally.

---

## 🧩 What’s Inside

### 1. **Custom Database Engine**
- Implemented **from zero**, without using any SQL or NoSQL database system.  
- Includes a **custom data storage layer**, capable of handling tables, records, and indexing.
- Features a **homemade query language**, designed to resemble simplified SQL syntax.

### 2. **Backend Logic**
- Built around RESTful design principles.  
- Provides endpoints to:
  - Create, read, update, and delete data.
  - Handle authentication and user session management.
  - Execute queries against the custom database.

### 3. **Frontend Interface**
- A simple web interface that allows users to:
  - Create and manage their own profiles.
  - Perform basic operations on the database (insert, query, modify data).
  - Interact with the backend through the RESTful API.

---

## ✨ Fixes and Improvements

This fork addresses several issues from the original project to create a more robust and functional application:

- **Correct Purchase and Renewal Logic**: The frontend now correctly uses `POST /domains/new` for new purchases and `PUT /domains/{domainName}/renew` for renewals, resolving a critical bug.
- **Functional "Acquista" Button**: Fixed the JavaScript logic that prevented the purchase button from working.
- **Robust Domain Locking**: Implemented a reliable `unlockDominio` function that is triggered when the user navigates away from the purchase page, preventing domains from remaining stuck in the `ACQUIRING` state.
- **Refactored Frontend Code**: Unified duplicate code for purchase and renewal operations into a single, cleaner function (`eseguiTransazioneDominio`).
- **Improved User Session Handling**: The frontend now consistently retrieves the user ID from `localStorage`, making session management more secure and reliable.
- **General Bug Fixes**: Corrected various minor bugs, such as the `window.onload` initialization, to improve overall stability.


---

## ⚠️ Security Disclaimer

This project was developed with a primary focus on learning the architectural principles of distributed systems, not on implementing security best practices.

**The system is NOT secure and must not be used in a production environment.**

Specifically, user session management is implemented in a highly insecure manner:
- The client stores the user's raw ID in `localStorage` after login.
- This ID is then sent with every subsequent request to authenticate the user.
- There are no secure session tokens (e.g., JWT), encryption, or server-side session validation mechanisms in place. This makes the system vulnerable to session hijacking and unauthorized access if the user ID is intercepted or manipulated.

The goal was to build a working system from scratch, prioritizing the understanding of distributed components over security hardening.


---

## 🏗️ Project Structure
registra-domini/
│
├── backend/ # Core backend logic and custom database engine
├── frontend/ # Web interface for user interaction
├── api/ # RESTful API endpoints
├── docs/ # Documentation and design notes
└── README.md # You’re here!

---

## 🧪 Learning Focus

This project was developed as part of a **university examination in computer science / IT engineering**, aimed at testing understanding of:

- Backend architecture design
- Custom database implementation
- Query language design
- RESTful API design
- Frontend-backend integration

The intention is **not** to produce a production-ready system, but to **demonstrate full-stack system design knowledge** and **low-level understanding of database management concepts**.

---

## 🚀 Running the Project

> ⚠️ Note: This project is experimental and educational — setup steps may vary depending on your environment.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/neraf/registra-domini.git
   cd registra-domini
   ```
   *(Replace 'neraf' with your GitHub username if you are cloning your own fork)*

2. **Build and run the Database:**
   Open a terminal and run the following commands:
   ```bash
   cd database
   mvn clean compile
   mvn exec:java
   ```
   The database will start listening on port `3030`.

3. **Build and run the Web Server:**
   Open a **new terminal** and run the following commands:
   ```bash
   cd server-web
   mvn jetty:run
   ```
   The web server will start, providing the REST API on port `8080`.

4. **Open the Frontend:**
   The frontend is located in the `client-web` directory. You can serve it using a simple local server. For example, if you have VS Code with the "Live Server" extension, you can right-click on `client-web/index.html` and select "Open with Live Server".
