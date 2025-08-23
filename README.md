# 🚀 MongoDB Kitchensink Apps

A comprehensive **full-stack web application** demonstrating modern enterprise development patterns with **Spring Boot + Angular**, featuring secure **JWT Authentication**, **MongoDB** persistence, and **Redis** caching — all containerized using **Docker & Docker Compose**.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-15+-red.svg)](https://angular.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0+-green.svg)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

---

## 📋 Table of Contents

- [Project Structure](#-project-structure)
- [Architecture Overview](#-architecture-overview)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Services & Endpoints](#-services--endpoints)
- [Authentication Flow](#-authentication-flow)
- [API Documentation](#-api-documentation)
- [Development](#-development)
- [Configuration](#-configuration)
- [Code Coverage](#-codecoverage)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)

---

## 📂 Project Structure

```
mongodb-kitchensink-apps/
├── kitchen-sink-rest/          # 🟢 Spring Boot REST API (Backend)
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── kitchensink-user-app/       # 🔵 Angular UI (Frontend)
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml          # 🟡 Services Orchestration
└── README.md
```

---

## 🏗️ Architecture Overview

### Backend Components
- **🟢 Spring Boot REST API** (`kitchen-sink-rest`)
  - Spring Security with JWT Authentication
  - MongoDB for data persistence
  - Redis for session management and caching
  - Swagger/OpenAPI 3.0 documentation
  - RESTful API design patterns

### Frontend Components
- **🔵 Angular Application** (`kitchensink-user-app`)
  - Responsive UI with modern Angular features
  - HTTP client for API communication
  - JWT token management
  - Routing and navigation

### Infrastructure
- **🟡 MongoDB** - Document-based primary database
- **🔴 Redis** - In-memory caching and session store
- **🐳 Docker** - Containerization for all services

---

## 📋 Prerequisites

Ensure you have the following installed on your system:

| Tool | Version | Download Link |
|------|---------|---------------|
| 🐳 **Docker** | 20.10+ | [Download Docker](https://www.docker.com/get-started) |
| 🐙 **Docker Compose** | 2.0+ | [Install Compose](https://docs.docker.com/compose/install/) |
| 🌐 **Web Browser** | Modern | Chrome, Firefox, Safari, Edge |

> **Note:** MongoDB and Redis are included as Docker images - no manual installation required!

---

## 🚀 Quick Start

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/tripathiarpit/mongodb-kitchensink-apps.git
cd mongodb-kitchensink-apps
```

### 2️⃣ Build & Run with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d
```

### 3️⃣ Verify Services
Wait for all containers to start (usually 2-3 minutes), then verify:

```bash
# Check container status
docker ps

Expected O/P
CONTAINER ID   IMAGE                        COMMAND                  CREATED          STATUS                    PORTS                                         NAMES 
fbb76714dca5   kitchen-sink-frontend   "/docker-entrypoint.…"   10 minutes ago   Up 10 minutes             0.0.0.0:4200->80/tcp, [::]:4200->80/tcp       kitchensink-frontend
715ec961a373   kitchen-sink-backend    "java -jar app.jar"      10 minutes ago   Up 10 minutes (healthy)   0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp   kitchensink-app-rest
46517bedf77e   redis:7                 "docker-entrypoint.s…"   22 hours ago     Up 10 minutes             0.0.0.0:6379->6379/tcp                        kitchensink-redis
668aa2ba9237   mongo:6.0               "docker-entrypoint.s…"   22 hours ago     Up 10 minutes             0.0.0.0:27017->27017/tcp                      mongo


```

---

## 🌐 Services & Endpoints

| Service | URL | Description | Status Check |
|---------|-----|-------------|--------------|
| **🔵 Frontend** | http://localhost:4200 | Angular UI Application | Visit in browser, login with admin@example.com Admin@123 |
| **🟢 Backend API** | http://localhost:8080 | Spring Boot REST API | `curl http://localhost:8080/actuator/health` |
| **📖 Swagger UI** | http://localhost:8080/swagger-ui | Interactive API Documentation | Visit in browser |
| **🟡 MongoDB** | localhost:27017 | Database (internal) | Via application logs |
| **🔴 Redis** | localhost:6379 | Cache & Sessions (internal) | Via application logs |

---

## 🔑 Authentication Flow

### Step 1: User Registration/Login
1. Access the frontend at http://localhost:4200
2. Register a new account or login with existing credentials
3. Upon successful authentication, a JWT token is generated

### Step 2: API Access with JWT
```bash
# Example: Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"your-username","password":"your-password"}'

# Use the token for authenticated requests
curl -X GET http://localhost:8080/api/secure-endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Step 3: Swagger UI Authentication
1. Open http://localhost:8080/swagger-ui
2. Click the **🔒 Authorize** button
3. Enter your JWT token in the format: `Bearer YOUR_TOKEN`
4. Click **Authorize** to access secured endpoints

---

## 📖 API Documentation

### Accessing Swagger UI
- **URL:** http://localhost:8080/swagger-ui
- **Features:**
  - Interactive API testing
  - Request/response schemas
  - Authentication integration
  - Real-time API exploration

### Key API Endpoints
```
Authentication:
├── POST /api/auth/register    # User registration
├── POST /api/auth/login       # User login
└── POST /api/auth/logout      # User logout

User Management:
├── GET  /api/users           # List users (authenticated)
├── GET  /api/users/{id}      # Get user by ID
├── PUT  /api/users/{id}      # Update user
└── DELETE /api/users/{id}    # Delete user

Health & Monitoring:
├── GET /actuator/health      # Application health
└── GET /actuator/info        # Application info
```

---

## 🛠️ Development

### Running Individual Services

#### Backend Development
```bash
cd kitchen-sink-rest
mvn spring-boot:run
# API will be available at http://localhost:8080
```

#### Frontend Development
```bash
cd kitchensink-user-app
npm install
npm start
# UI will be available at http://localhost:4200
```

### Hot Reloading
- **Frontend:** Angular dev server provides automatic reload
- **Backend:** Use Spring Boot DevTools for automatic restart

### Database Management
```bash
# Access MongoDB shell
docker exec -it mongodb-kitchensink-apps_mongodb_1 mongosh

# Access Redis CLI
docker exec -it mongodb-kitchensink-apps_redis_1 redis-cli
```

---
## Testing
Execute command for executing test cases

```bash
mvn clean test
````

## ⚙️ Configuration

### Application Properties
Main configuration file: `kitchen-sink-rest/src/main/resources/application.properties`

### Some users are created in the application, use below to access the application
login with admin@example.com Admin@123

```properties
# Database Configuration
spring.data.mongodb.host=mongodb
spring.data.mongodb.port=27017
spring.data.mongodb.database=kitchensink

# Redis Configuration
spring.redis.host=redis
spring.redis.port=6379

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Server Configuration
server.port=8080
```

### Environment Variables
You can override configurations using environment variables in `docker-compose.yml`:

```yaml
environment:
  - SPRING_DATA_MONGODB_HOST=mongodb
  - SPRING_REDIS_HOST=redis
  - JWT_SECRET=your-custom-secret
```

---


## ⚙️ Code Coverage
Here's a complete set of instructions for generating a code coverage report and sending it to SonarQube, based on your current setup.

🏃‍♂️ Prerequisites
Ensure Docker is running.

Make sure you are in the root directory of your project (the one containing docker-compose.yml).

You have a SonarQube authentication token.

1. Start the Services
First, build and run all the necessary services, including MongoDB and SonarQube, using your docker-compose file.

Bash

docker-compose up --build
This command builds the backend image (skipping tests) and starts all the containers, including MongoDB, the REST API, and SonarQube, on the kitchen-sink_kitchensink-network. Wait a minute or two for all services to be fully up and running.

2. Run Tests and Generate the Report
Now that your database and SonarQube server are running, you can run your tests and trigger the SonarQube analysis. Use a dedicated Docker container with Maven to perform this task. Make sure you're in the kitchen-sink-rest directory before running this command.

Bash
```
docker run --rm -it -v "$(pwd):/app" --network=kitchen-sink_kitchensink-network -w /app maven:3.9-eclipse-temurin-21 mvn clean verify sonar:sonar -Dspring.data.mongodb.host=mongodb -Dspring.data.mongodb.database=kitchensinkdb -Dsonar.projectKey=kitchensink -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=squ_4f4b69499f09af05304af0653ed9ad5d9b66b83e -Dsonar.exclusions=**/dto/**/*,**/constants/**/*,**/mapper/**/*
docker run: This creates a temporary container to run Maven.
```

-v "$(pwd):/app": This mounts your current project directory (where your code is) into the container.

--network=kitchen-sink_kitchensink-network: This connects the temporary container to the same network as your other services, allowing it to communicate with mongodb and sonarqube.

-w /app: This sets the working directory inside the container to /app, ensuring Maven finds your pom.xml.

mvn clean verify sonar:sonar: This Maven command runs your tests (verify), generates the JaCoCo code coverage report, and then sends the results to your SonarQube server (sonar:sonar).

-Dspring.data.mongodb.host=mongodb and -Dspring.data.mongodb.database=kitchensinkdb: These properties tell your tests to connect to the mongodb container on the network instead of localhost.

-Dsonar.projectKey=..., -Dsonar.host.url=..., -Dsonar.login=...: These properties configure the SonarQube analysis to use your project key, the SonarQube service URL on the Docker network, and your authentication token.

3. View the Report
Once the command is complete, open your web browser and navigate to the SonarQube dashboard at http://localhost:9000. Your code coverage and quality report for the kitchensink project should be visible.


## 🔧 Troubleshooting

### Common Issues & Solutions

#### Port Already in Use
```bash
# Check what's using the port
lsof -i :4200  # or :8080, :27017, :6379

# Stop conflicting services
docker-compose down
```

#### Container Build Failures
```bash
# Clean build cache
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache
```

#### Database Connection Issues
```bash
# Check MongoDB logs
docker-compose logs mongodb

# Restart MongoDB container
docker-compose restart mongodb
```

#### Memory Issues
```bash
# Check Docker memory usage
docker stats

# Increase Docker memory allocation in Docker Desktop settings
```

### Logs & Debugging
```bash
# View all service logs
docker-compose logs

# View specific service logs
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mongodb
docker-compose logs redis

# Follow logs in real-time
docker-compose logs -f backend
```

## 📈 Performance Monitoring

### Application Metrics
- **Spring Boot Actuator:** http://localhost:8080/actuator
- **Health Check:** http://localhost:8080/actuator/health
- **Metrics:** http://localhost:8080/actuator/metrics

### Resource Monitoring
```bash
# Monitor container resources
docker stats

# Check disk usage
docker system df
```

---

## 🔮 Future Improvements

### Short Term
- [ ] Environment-specific configuration profiles (dev/test/prod)
- [ ] Unit and integration test coverage
- [ ] Error handling and logging improvements
- [ ] API versioning strategy

### Medium Term  
- [ ] CI/CD pipeline integration (GitHub Actions/Jenkins)
- [ ] Database migrations with Flyway/Liquibase
- [ ] Monitoring with Prometheus & Grafana
- [ ] Load balancing and horizontal scaling

### Long Term
- [ ] Kubernetes deployment manifests
- [ ] Microservices architecture migration
- [ ] API Gateway integration
- [ ] Service mesh implementation (Istio)
- [ ] Event-driven architecture with Apache Kafka

---

### Development Guidelines
- Follow Spring Boot and Angular best practices
- Write comprehensive tests
- Update documentation for new features
- Use conventional commit messages

---

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Angular team for the robust frontend framework  
- MongoDB and Redis communities
- Docker for containerization simplicity

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/tripathiarpit/mongodb-kitchensink-apps/issues)
---

**Happy Coding! 🚀**
