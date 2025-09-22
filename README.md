# Chat Application

A modular, enterprise-grade chat application built with Java, Spring Boot, and JavaFX.

## Project Structure

The application is divided into three main modules:

1. **chat-common**: Shared libraries, models, and utilities used by both client and server.
2. **chat-server**: Spring Boot application that manages WebSocket connections and message routing.
3. **chat-client**: JavaFX desktop client application for end users.

## Features

- Real-time messaging using STOMP WebSockets
- User authentication
- Multi-room chat support
- Message pinning and deletion
- Professional, modular architecture

## Architecture

The application follows a client-server architecture:

- **Client**: JavaFX desktop application that connects to the server via WebSockets
- **Server**: Spring Boot application that handles WebSocket connections and routes messages
- **Common**: Shared libraries used by both client and server

### Server Architecture

The server module is designed around the following components:

- **WebSocketConfig**: Configures STOMP WebSocket endpoints and message broker
- **ChatMessageController**: Handles incoming WebSocket messages from clients
- **ChatSessionService**: Manages active WebSocket sessions and user-chat mappings

### Client Architecture

The client module is organized as follows:

- **ChatClientApplication**: Entry point for the JavaFX application
- **WebSocketHandler**: Manages WebSocket communication with the server
- **UI Components**: JavaFX views and controllers for chat interface

## Technical Implementation

- **WebSocket Communication**: Uses STOMP over WebSockets for structured, real-time communication
- **Serialization**: Uses Jackson for JSON serialization/deserialization
- **Logging**: Log4j2 for comprehensive logging
- **Testing**: JUnit 5 for unit testing with Mockito for mocking

## Getting Started

### Prerequisites

- JDK 21 or higher
- Maven 3.6 or higher

### Building the Project

To build all modules, run:

```bash
mvn clean install
```

### Running the Server

```bash
cd chat-server
mvn spring-boot:run
```

### Running the Client

```bash
cd chat-client
mvn javafx:run
```

## Development Setup

The project uses Maven for dependency management:

- The parent POM (`pom.xml`) defines common dependencies and plugins
- Each module has its own POM file with module-specific dependencies

### Server Development

The server uses Spring Boot, so you can use Spring Boot Dev Tools for hot reloading:

```bash
cd chat-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Client Development

For client development, you can use Maven JavaFX plugin:

```bash
cd chat-client
mvn javafx:run
```

## Testing

The project uses JUnit 5 for unit testing:

```bash
mvn test
```

Code coverage reports are generated with JaCoCo:

```bash
mvn verify
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

# Auteurs

- REICHHART Damien 


© 2025 - CCI BTS SIO 23-25 - Atelier Professionnalisation 4