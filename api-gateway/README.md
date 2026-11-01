# API Gateway

## Overview
The API Gateway is the single entry point for client requests in the ShopSphere e-commerce microservices application.

It routes incoming requests to the appropriate backend microservices using Spring Cloud Gateway and Eureka Service Discovery.

## Technology Stack
- Java 21
- Spring Boot
- Spring Cloud Gateway
- Netflix Eureka Client
- Spring Cloud LoadBalancer
- Spring Boot Actuator

## Configuration
| Component | Port |
|---|---|
| API Gateway | 8080 |
| User Service | 8081 |
| Product Service | 8082 |
| Inventory Service | 8083 |
| Order Service | 8084 |
| Cart Service | 8085 |
| Payment Service | 8086 |
| Eureka Service Registry | 8761 |

## Service Discovery
The API Gateway uses Eureka to discover registered microservices.

Routes use the `lb://SERVICE-NAME` format to enable service discovery and load balancing.

## Health Check
```text
http://localhost:8080/actuator/health
```

## Eureka Dashboard
```text
http://localhost:8761
```
