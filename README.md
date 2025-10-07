# Resilience with Resilience4j

This project demonstrates the use of Resilience4j with Spring Boot and Kotlin, implementing resilience patterns such as Circuit Breaker, Retry, and Fallback in a REST service.

The goal is to test different failure scenarios in external service calls and observe how each resilience mechanism behaves.

## Technologies and Versions
- Kotlin: 1.9.25
- Spring Boot: 3.5.4-SNAPSHOT
- Spring Cloud: 2025.0.0
- Resilience4j: via spring-cloud-starter-circuitbreaker-resilience4j
- JUnit 5 + Mockito Kotlin: for unit testing
- Java: 17

## Features
- **Circuit Breaker**: Stops calling an external service after a defined number of failures.
- **Retry**: Automatically retries calls that fail due to transient errors.
- **Fallback**: Returns a default response when the service is unavailable.

## Running and Testing
**Run the application**
```./gradlew bootRun```


Service available at:

http://localhost:8080

**Run all tests**
```./gradlew test```
