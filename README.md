# Chatter

A desktop client-server chat application built in Java. The project provides real-time messaging over TCP sockets, a Java Swing user interface, and MySQL-backed user and message storage.

## Features

- Real-time client-server messaging using TCP sockets
- Multi-threaded server design for handling multiple connected clients
- Java Swing desktop interface
- User registration and authentication
- MySQL database integration
- HikariCP connection pooling
- Message persistence
- Offline message delivery
- Object-oriented application design

## Tech Stack

| Area | Technologies |
|---|---|
| Language | Java |
| Desktop UI | Java Swing |
| Networking | TCP/IP sockets |
| Database | MySQL |
| Database connection pooling | HikariCP |
| Concurrency | Java multithreading |

## Prerequisites

Before running the application, make sure you have:

- A Java Development Kit (JDK) installed.
- A running MySQL server and the database configuration required by the project.
- The project dependencies available, including HikariCP and the MySQL JDBC driver.

## Launching the Application

### Client

The client is intended to be launched using the provided JAR file. However, since the source code is also included, developers can also open and run it through an IDE.

1. Install a JDK for your operating system.
2. Launch `Client\target\Chatter-1.0.jar`.

### Server

The server is intended for developers and can be run from an IDE, such as NetBeans, or another Java development application. Configure and start MySQL before running the server.

## Architecture Overview

The application follows a client-server model:

- **Client:** Java Swing desktop application used for registration, authentication, and chat.
- **Server:** A multi-threaded Java server that manages client connections and message communication.
- **Database:** MySQL stores user data and persisted messages.
- **Connection Pool:** HikariCP manages database connections efficiently.

## Notes

Database credentials, database name, port settings, and shortcut configuration depend on the project files and local environment. Update those settings before launching if necessary.
