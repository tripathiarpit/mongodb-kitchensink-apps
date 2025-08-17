Full-stack application demonstrating MongoDB integration with Spring Boot REST API and Angular frontend

🚀 Features

Spring Boot REST API with MongoDB and Redis caching
Angular frontend for user interface
Dockerized development environment
Single configuration setup

📋 Prerequisites

Docker
Docker Compose
Git

⚡ Quick Start


# Clone the repository
git clone https://github.com/tripathiarpit/mongodb-kitchensink-apps.git

# Navigate to project directory
cd mongodb-kitchensink-apps

# Start all services
docker-compose up


┌─────────────────┐    ┌─────────────────┐
│   Angular UI    │    │  Spring Boot    │
│ (Port: 4200)    │◄──►│   REST API      │
└─────────────────┘    │ (Port: 8080)    │
                       └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │    MongoDB      │    │     Redis       │
                       │  (Port: 27017)  │    │  (Port: 6379)   │
                       └─────────────────┘    └─────────────────┘



Service    Technology    Port        Description

ui          Angular      4200        Frontend application

api         Spring Boot  8080        REST API server

mongodb      MongoDB     27017        Primary database
redis        Redis       6379        Caching layer


mongodb-kitchensink-apps/
├── 📁 kitchen-sink-rest/           # Spring Boot REST API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── 📁 kitchensink-user-app/        # Angular frontend
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── 📄 docker-compose.yml           # Docker orchestration
└── 📄 README.md                    # This file


🛠️ Development
Available Scripts


# Start all services
docker-compose up

# Start in detached mode
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs [service-name]

# Rebuild images
docker-compose up --build


Environment Configuration
Currently uses a single environment file located in the REST API project. Environment-specific configurations will be added in future releases.
🔧 Configuration
The application uses default configuration with:

MongoDB connection on port 27017
Redis connection on port 6379
Spring Boot API on port 8080
Angular UI on port 4200


🤝 Contributing

Fork the repository
Create your feature branch (git checkout -b feature/AmazingFeature)
Commit your changes (git commit -m 'Add some AmazingFeature')
Push to the branch (git push origin feature/AmazingFeature)
Open a Pull Request

📜 License
This project is licensed under the MIT License - see the LICENSE file for details.
👥 Authors

Arpit Tripathi - @tripathiarpit
