# 📦 ComplyVault – Data Ingestion & Compliance Platform

ComplyVault is a **multi-tenant archival & compliance platform** designed to securely ingest, normalize, deduplicate, and store data while enforcing compliance and retention policies. Think of it like a **compliance vault** where messages from multiple sources (e.g., Email, Slack) are processed, audited, and made searchable for regulatory and business needs.

---

## 🚀 Project Status

### ✅ Completed Features
1. **Ingestion & Validation**
   - JSON messages from Email and Slack.
   - Validation against defined schemas (rejects invalid payloads).
   - Stable unique message IDs for traceability.

2. **Normalization & Deduplication**
   - Canonical schema unifies different sources.
   - Deduplication ensures no duplicate messages are stored.

3. **Retention & Audit Logs**
   - Immutable storage of original + normalized messages.
   - Retention API for channel-specific policies.
   - Full lifecycle audit trail (Ingested → Normalized → Policy Evaluated).

4. **Compliance Policy Engine**
   - Regex-based policies for rule enforcement.
   - Violations flagged and stored for review.

5. **Flagging & Data Exposure**
   - Exposes flagged data through secure APIs.
   - Stores review info for auditors.

6. **Search Functionality**
   - **Keyword** and **full-text search** across canonical schema fields.

---

### ⏳ Pending (Bonus Requirements)
- **Disposition / Deletion** once retention expires (with audit logging).
- **Data Export** from immutable store (single/multiple documents).
- **Automation / End-to-End Integration**
  - CI/CD pipelines.
  - End-to-end test automation across services.

---

## 🏗️ Architecture

```
                ┌──────────────┐
                │   Ingestion   │
                │ (Email/Slack) │
                └──────┬───────┘
                       │
             ┌─────────▼─────────┐
             │   Validation &     │
             │ Normalization      │
             └─────────┬─────────┘
                       │
             ┌─────────▼─────────┐
             │ Deduplication      │
             └─────────┬─────────┘
                       │
             ┌─────────▼─────────┐
             │ Compliance Engine  │
             └─────────┬─────────┘
                       │
        ┌──────────────▼───────────────┐
        │   Retention & Audit Logging   │
        └──────────────┬───────────────┘
                       │
            ┌──────────▼─────────┐
            │   Exposed API      │
            │ (Flagged/All Data) │
            └──────────┬─────────┘
                       │
             ┌─────────▼─────────┐
             │ Search (Keyword & │
             │ Full-text)        │
             └───────────────────┘
```

---

## ⚙️ Tech Stack

- **Language & Framework:** Java 21, Spring Boot 3.x
- **Datastore:** MongoDB (immutable storage, retention, audit logs)
- **Search Engine:** Elasticsearch / MongoDB Atlas Search
- **Messaging:** Apache Kafka (for ingestion pipeline)
- **Build:** Maven
- **Deployment:** Docker, Kubernetes (Helm, ConfigMaps, Ingress)
- **CI/CD:** GitHub Actions / Jenkins (for automation)

---

## 🔧 Setup & Installation

### Prerequisites
- Java 17+ (Java 21 recommended)
- Maven 3.9+
- Docker & Docker Compose
- MongoDB
- Elasticsearch
- Apache Kafka

### Steps
```bash
# Clone repo
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>

# Build
mvn clean install

# Run locally
mvn spring-boot:run

# Run with Docker
docker-compose up --build
```

---

## 📡 API Endpoints

### Ingestion
```http
POST /ingest/email   # Ingest Email JSON
POST /ingest/slack   # Ingest Slack JSON
```

### Search
```http
GET /search?keyword=hello
GET /search/fulltext?q=confidential
```

### Flagged Data
```http
GET /api/violations
```

### Retention
```http
POST /api/retention
{
  "channel": "email",
  "retentionPeriod": "7y"
}
```

---

## 📊 Example Audit Log
```json
{
  "logId": "1234-5678",
  "timestamp": "2025-08-23T10:15:30Z",
  "service": "ingestion-service",
  "stage": "validation",
  "status": "passed",
  "details": {
    "totalPoliciesChecked": 10,
    "violationsFound": 0
  }
}
```

---

## ✅ Testing
- **Unit Tests:** JUnit + Mockito
- **Integration Tests:** SpringBootTest + Testcontainers
- **Automation:** End-to-end testing (pending)

---

## 📌 Roadmap
- [ ] Implement retention-based deletion (bonus).
- [ ] Add export APIs.
- [ ] Add full automation (CI/CD + integration tests).
- [ ] Monitoring & metrics (Prometheus + Grafana).
- [ ] Role-based access control for APIs.

---

## 👨‍💻 Contributors
- Team 1 - DASH-V  
  - Vivek  
  - Dharini  
  - Akash  
  - Stuti  
  - Hasini  

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
