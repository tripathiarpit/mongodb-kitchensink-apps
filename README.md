# 🚀 **MongoDB Kitchensink Apps**

This repository contains a **_full-stack web application_** built using **Spring Boot + Angular**, with secure **JWT Authentication**, **MongoDB** persistence, and **Redis** caching — all containerized using **Docker & Docker Compose**.  

---

## 📂 **Project Structure**

mongodb-kitchensink-apps/
│── kitchen-sink-rest 🟢 Spring Boot REST API (Backend)
│── kitchensink-user-app 🔵 Angular UI (Frontend)
│── docker-compose.yml 🟡 Orchestration for services




---

## ⚙️ **Components**

### 🟢 **Backend (kitchen-sink-rest)**
- Spring Boot REST API  
- **Spring Security + JWT Authentication**  
- MongoDB persistence  
- Redis caching & session management  
- Swagger/OpenAPI documentation  

### 🔵 **Frontend (kitchensink-user-app)**
- Angular-based UI  
- Talks to REST APIs  
- Runs on: `http://localhost:4200`  

### 🟡 **Databases & Caching**
- **MongoDB** → Primary data persistence  
- **Redis** → Session & cache management  

### 📖 **API Documentation**
- Swagger UI available at: [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)  
- 🔑 Requires JWT token for accessing secured endpoints  

---

## 🛠️ **Technologies Used**

- **Backend** → Spring Boot, Spring Security, JWT, MongoDB, Redis  
- **Frontend** → Angular  
- **Containerization** → Docker, Docker Compose  
- **API Docs** → Swagger / OpenAPI  

---

## 📋 **Prerequisites**

Before running the app, install:  

- 🐳 [Docker](https://www.docker.com/)  
- 🐙 [Docker Compose](https://docs.docker.com/compose/)  

👉 *No need to install MongoDB/Redis manually — they are included as Docker images.*  

---

## 🚀 **Getting Started**

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/tripathiarpit/mongodb-kitchensink-apps.git
cd mongodb-kitchensink-apps






**2️⃣ Build & Run with Docker Compose**
navigate to the root directory of the project that has two folder one of UI and iothe rof REST and execute
docker-compose up --build
**
✅ This starts 4 containers:

🟢 REST API → http://localhost:8080

🔵 Angular UI → http://localhost:4200

🟡 MongoDB

🔴 Redis

3️⃣ Access Applications

🌐 Frontend (UI): http://localhost:4200

⚡ Backend (API): http://localhost:8080

📖 Swagger UI: http://localhost:8080/swagger-ui**



**🔑 Authentication with Swagger**

Login via API/UI → obtain a JWT Token (check response header).

Open Swagger UI.

Click 🔒 Authorize → Paste your JWT token.

🎉 Now you can test secured APIs directly from Swagger!

**🐳 Docker Images Used**

🟢 Spring Boot REST API (custom build)

🔵 Angular UI (custom build)

🟡 MongoDB (official image)

🔴 Redis (official image)

**⚡ Notes**

Application properties (DB, Redis, JWT configs) are stored in:

**kitchen-sink-rest/src/main/resources/application.properties**


**Currently, only one environment configuration is used (no dev/test/prod profiles).**

📌 Future Improvements

🔧 Add environment-specific configuration files

🏗️ CI/CD pipeline integration (GitHub Actions / Jenkins)

📈 Scaling services with Kubernetes

🌍 API Gateway + Service Mesh for microservice adoption


