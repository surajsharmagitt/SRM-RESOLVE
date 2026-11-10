# SRM-RESOLVE

**Smart Campus Infrastructure Grievance Redressal, Incident Grouping & Automated SLA Escalation System**  
Built for **SRM University-AP (SRMUAP)** using **Java 21 LTS**, **Spring Boot 3.4**, and **MySQL 8.0 / MariaDB**.

- **Live Deployment (Render Cloud):** [https://srm-resolve.onrender.com/](https://srm-resolve.onrender.com/)
- **GitHub Repository:** [https://github.com/surajsharmagitt/SRM-RESOLVE](https://github.com/surajsharmagitt/SRM-RESOLVE)

---

## Key Capabilities

1. **Passwordless Student Onboarding & Persistent Residence Mapping:**
   - Authenticates students via institutional email (`@srmap.edu.in` / `@srmist.edu.in`) with 6-digit BCrypt-hashed OTP delivery via Gmail SMTP (plus evaluation passkey `123456` and 30-second Resend OTP cooldown).
   - First-time students complete `/student/complete-profile` to link their Name, Registration Number, Hostel Tower (`Vedavathi`, `Ganga`, `Yamuna`, `Krishna`, `Kaveri`, etc.), Room Number, Department, and Section directly to their email in MySQL.
   - Returning logins load the saved student profile and tickets automatically across any browser or device.

2. **Zero-Friction Issue Routing (`CampusIntelligenceEngine`):**
   - Quick-tap grievance grid plus free-text issue prediction that routes tickets automatically to the appropriate campus department and sets priority and SLA deadlines.
   - Confidential priority routing for emergency safety cells (ICC, Anti-Ragging, Counseling).

3. **Parent-Child Incident Grouping (`IncidentGroupingEngine`):**
   - Groups concurrent shared-utility complaints in the same residential tower within a 6-hour window using Jaccard token similarity into a single `master_incident`, surfacing a sanitized block banner with one-tap **"I'm Affected Too"** linking.

4. **Department Operations Hub & Two-Way Resolution Handshake:**
   - Mobile-first split-pane workspace (`/department/hub`) featuring **Hostel Tower filter chips**, **Status filters**, **1-click "Assign to Me"**, **Quick ETA presets** (`+30m`, `+2h`, `+6h`, `+24h`), and live chat updates.
   - Technicians mark issues as `AWAITING_STUDENT_CONFIRMATION`; tickets only transition to `CLOSED` once the student explicitly confirms resolution.

5. **Autonomous Multi-Tier SLA Escalation & Executive Radar:**
   - `@Scheduled` cron worker evaluates `ack_deadline_at` and `resolution_deadline_at` in `Asia/Kolkata` (IST), escalating overdue tickets (`Level 0 -> Level 1 Dept Head -> Level 2 Executive`).
   - Executive Dashboard (`/executive/dashboard`) queries 4 dedicated SQL analytics views for anomaly radar, SLA compliance, and hostel block heatmaps.

---

## Architecture & Tech Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Backend** | Java 21 LTS, Spring Boot 3.4.1 | Spring Web MVC, Spring Security 6, Spring Mail, Spring Scheduling |
| **Persistence** | Explicit SQL (No JPA/Hibernate) | `JdbcTemplate`, `NamedParameterJdbcTemplate`, and custom `RowMapper` classes |
| **Database** | MySQL 8.0 / MariaDB 10.11 | InnoDB, UTF8MB4, 12 tables, foreign keys, 4 SQL analytics views |
| **Frontend** | Thymeleaf 3.1, Bootstrap 5.3 | Responsive server-rendered HTML5 templates, Vanilla JS, Chart.js |
| **Deployment** | Docker, Render Cloud | Multi-stage Dockerfile (`maven:3.9.6-eclipse-temurin-21` + JRE 21 & MariaDB) |
| **Timezone** | `Asia/Kolkata` (IST) | Enforced across JVM, Spring Jackson, and SQL queries |

---

## Project Structure

```text
srm-resolve/
├── Dockerfile                  # Multi-stage Maven build + JRE 21 & MariaDB runtime
├── docker-entrypoint.sh        # Self-initializing MariaDB + memory-tuned JVM startup
├── render.yaml                 # Render Cloud Blueprint specification
├── pom.xml                     # Maven configuration (Java 21, Spring Boot 3.4.1, MySQL, H2)
└── src/
    ├── main/
    │   ├── java/com/srmresolve/
    │   │   ├── config/         # Spring Security 6 RBAC & global model attributes
    │   │   ├── controller/     # Home, Student, Department, DeptHead, Executive, Auth, API
    │   │   ├── dao/            # Explicit SQL DAOs & RowMappers (JdbcTemplate)
    │   │   ├── dto/            # Request/response DTOs
    │   │   ├── model/          # Domain entities, enums, and SQL view projections
    │   │   ├── service/        # OTP onboarding, SLA scheduler, grouping, handshake
    │   │   └── validator/      # Registration format validators
    │   └── resources/
    │       ├── application.properties
    │       ├── schema.sql      # Relational DDL schema & analytics views
    │       ├── data.sql        # Institutional departments, categories, and roster seed data
    │       ├── static/css/     # Stylesheets
    │       └── templates/      # Thymeleaf HTML views
    └── test/                   # JUnit 5 & MockMvc automated test suite (46 tests)
```

---

## Test Credentials & Portals

| Role | Portal Route | Email | Auth Method |
| :--- | :--- | :--- | :--- |
| **Student** | `/student-login` | Any `@srmap.edu.in` email | Email OTP or Passkey `123456` |
| **Water & Plumbing Staff** | `/authority-login` | `water.staff1@srmap.edu.in` | `staff123` |
| **Hostel / CLM Staff** | `/authority-login` | `hostel.staff1@srmap.edu.in` | `staff123` |
| **Department Head (Water)** | `/authority-login` | `water.head@srmap.edu.in` | `staff123` |
| **Department Head (CLM)** | `/authority-login` | `hostel.head@srmap.edu.in` | `staff123` |
| **Vice-Chancellor / Executive** | `/admin-login` | `vc@srmap.edu.in` | `admin123` |

---

## Local Setup & Verification

### Prerequisites
- OpenJDK 21+
- Apache Maven 3.9+
- MySQL 8.0+ on port `3306`

### Run Automated Test Suite (46 Tests)
```bash
mvn clean test
```

### Build & Run Locally
```bash
mvn clean package -DskipTests
java -Duser.timezone=Asia/Kolkata -jar target/srm-resolve-1.0.0.jar
```
Open [http://localhost:8080/](http://localhost:8080/) in your browser.

---

## Cloud Deployment (Render)

`render.yaml` and `Dockerfile` provide turnkey container deployment:
- Stage 1 builds `srm-resolve-1.0.0.jar` using `maven:3.9.6-eclipse-temurin-21`.
- Stage 2 starts embedded MariaDB cleanly via `docker-entrypoint.sh` (when no external `SPRING_DATASOURCE_URL` is provided) and runs the JVM with heap tuned (`-Xmx320m -Xms128m`) for container stability.
- Health checks probe `/welcome`.
