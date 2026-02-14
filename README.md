Bank Management System – JPA + JDBC Hybrid Version
📌 Project Overview

The Bank Management System is a console-based Java application designed to simulate core banking operations.
This upgraded version demonstrates a migration from JDBC to JPA (Hibernate) while maintaining a hybrid architecture that still allows JDBC usage where required.

The project focuses on applying Core Java, Object-Oriented Programming, and ORM concepts to build a scalable and enterprise-style backend system.

This project is part of my backend development portfolio and highlights my ability to:

Design layered architectures

Work with relational databases

Perform ORM migration

Maintain backward compatibility during refactoring

🎯 Objectives

Simulate real-world banking operations

Apply Object-Oriented Programming principles in Java

Implement database connectivity using JPA & JDBC

Perform CRUD operations using ORM and SQL

Demonstrate migration from JDBC → JPA

Follow clean code and layered architecture principles

Showcase maintainability and scalability practices

🛠️ Technologies Used

Programming Language: Java (Core Java)
Database: MySQL
ORM Framework: JPA (Hibernate)
Database Connectivity: JDBC & JPA
IDE: IntelliJ IDEA / Eclipse
Build Tool: Maven
Architecture: Layered Architecture (DAO, Service, Model)

🧱 Project Architecture

The project follows a Layered Hybrid Architecture to separate concerns and improve maintainability.

Model Layer

Contains entity classes such as:

Account

User

Transaction

EmploymentProfile

UserRole
These classes are mapped using JPA annotations to represent database tables.

DAO Layer

Split into two implementations:

JDBC Implementation (jdbc_impl)

Direct SQL queries

Manual connection handling

Raw database control

JPA Implementation (jpa_impl)

EntityManager usage

JPQL queries

ORM-based persistence

Service Layer

Contains business logic, validations, and transaction coordination.
Acts as an intermediary between DAO and UI layers.

Utility Layer

JPAUtil – Manages EntityManagerFactory and persistence lifecycle

Connection helpers and reusable utilities

UI Layer (Console)

Menu-driven console interface allowing users to perform banking operations interactively.

✨ Features Implemented

Create new bank accounts

View account details

Deposit money

Withdraw money

Transfer funds between accounts

Check account balance

View transaction history

Role-based operations (Admin/User)

Input validation and exception handling

Hybrid DAO switching (JDBC ↔ JPA)

🗄️ Database Design

The application uses a MySQL relational database with tables such as:

users – stores user information

accounts – stores account balances and relationships

transactions – records all debit and credit operations

employment_profiles – employment data

user_roles – authorization roles

Primary and foreign keys maintain relational integrity, while JPA mappings handle entity relationships.

⚙️ Setup & Installation

Clone the repository

Create a MySQL database

Execute SQL scripts to create required tables

Configure persistence.xml with database credentials

Build the project using Maven

Run the main application class

▶️ How to Run

Run the main class from your IDE or terminal

Follow the console menu to perform banking operations

Switch DAO implementation if needed (JDBC/JPA)

🚧 Project Status

Completed – Hybrid JPA Migration Version

📚 Learning Outcomes

Through this project, I strengthened my understanding of:

Java OOP principles

JDBC and raw SQL operations

JPA & Hibernate ORM

Entity relationships and annotations

Transaction management

Layered backend architecture

Code refactoring and migration strategies

Real-world backend problem solving

👤 Author

Fardaws Jawad
Aspiring Java Backend Developer

📄 License

This project is for learning and portfolio purposes.
