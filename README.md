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
   - Covers all **20 campus departments** and **31 grievance categories** (lost ID cards, mess dining, water RO, hostel maintenance, Wi-Fi/ITKM, fee refund queries, hospital/wellness, labs, library, security, and safety cells).
   - Quick-tap grievance grid plus natural-language grievance keyword matching that routes tickets automatically to the responsible campus department, assigning priority and SLA deadlines.
   - Confidential priority routing for emergency safety cells (ICC, Anti-Ragging, Student Counseling).

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
    └── test/                   # JUnit 5 & MockMvc automated test suite (148 tests)
```

---

## Classroom Demonstration Credentials & Portals

> **Evaluation Quick Access:** For instant evaluation and classroom demonstrations, passkey **`123456`** is enabled on the student portal (`/student-login`) with a one-click **⚡ Quick Fill** button. Official institutional email domains (`@srmap.edu.in` and `@srmist.edu.in`) are strictly validated.

| Role | Portal Route | Account Name | Email | Auth Method (Evaluation) |
| :--- | :--- | :--- | :--- | :--- |
| **Student (Demo)** | `/student-login` | Suraj Sharma (Demo Student) | `suraj_sharma@srmap.edu.in` | Passkey `123456` or Email OTP |
| **Student (Demo)** | `/student-login` | Ananya Reddy (Demo Student) | `ananya_reddy@srmap.edu.in` | Passkey `123456` or Email OTP |
| **Water & Plumbing Staff** | `/authority-login` | Water Plumbing Staff 1 (Demo) | `water.staff1@srmap.edu.in` | `staff123` |
| **Hostel / CLM Staff** | `/authority-login` | Hostel Maintenance Staff 1 (Demo) | `hostel.staff1@srmap.edu.in` | `staff123` |
| **Department Head (Water)** | `/authority-login` | Water & Plumbing Lead (Demo) | `water.head@srmap.edu.in` | `staff123` |
| **Department Head (CLM)** | `/authority-login` | Hostel & CLM Lead (Demo) | `hostel.head@srmap.edu.in` | `staff123` |
| **Vice-Chancellor / Executive** | `/admin-login` | Office of Vice Chancellor (Demo) | `vc@srmap.edu.in` | `admin123` |
| **Administrator** | `/admin-login` | Campus Administrator (Demo) | `admin@srmap.edu.in` | `admin123` |

---

## Local Setup & Verification

### Prerequisites
- OpenJDK 21+
- Apache Maven 3.9+
- MySQL 8.0+ running on port `3306`

### Run Automated Test Suite (148 Tests)
```bash
mvn clean test
```

### Build & Run Locally for Classroom Demonstration
To enable local demonstration accounts (`data-demo.sql`) and evaluation passkey (`123456`), launch with `APP_DEMO_MODE=true`:
```bash
mvn clean package -DskipTests
APP_DEMO_MODE=true java -Duser.timezone=Asia/Kolkata -jar target/srm-resolve-1.0.0.jar
```
Or via JVM argument:
```bash
java -Duser.timezone=Asia/Kolkata -Dapp.demo-mode=true -jar target/srm-resolve-1.0.0.jar
```
Open [http://localhost:8080/](http://localhost:8080/) in your browser.

---

## Cloud Deployment & Environmental Limitations

### 1. Render Free Tier Ephemeral Storage Warning
- The Render application currently runs inside a single container embedding MariaDB alongside the Java 21 runtime.
- **Data Loss Risk:** On the Render free tier, web service container filesystems are **ephemeral**. When the container spins down due to inactivity, crashes, restarts, or is replaced following a Git commit redeployment, the `/var/lib/mysql` storage resets to the build image baseline. Any complaints, user registrations, status updates, or chat messages created during that cloud container session will be lost upon container replacement.
- **Classroom Demonstration Guarantee:** For evaluators and classroom demonstrations, run against a local MySQL database (`localhost:3306`) as described above, where database state is fully persistent across application restarts.
- **Permanent Cloud Persistence (Optional):** For zero data loss in a live production environment, connect to an external managed MySQL database (e.g., AWS RDS, GCP Cloud SQL, or Render Managed MySQL) by configuring `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. The container startup script (`docker-entrypoint.sh`) automatically detects external database URLs and bypasses the container's internal MariaDB instance.

### 2. Production vs. Demonstration Security Modes
- **Production Default:** `app.demo-mode=false` (enforced via `application.properties` and Render environment).
- **Public Protection Guarantees:**
  - Demonstration accounts with publicly known passwords (`admin123`, `staff123`, `password123`) in `data-demo.sql` are **never** seeded into the database in production.
  - The universal OTP bypass (`123456`) is strictly disabled on the server; students must authenticate via genuine 6-digit email verification codes.
  - Registration and password-reset OTP codes are never logged to console or displayed in web pages.
  - State-changing REST endpoints (`/api/tickets/{id}/messages`, simulator, notifications) enforce strict CSRF protection.
  - Department heads and staff are server-side restricted to their assigned department's tickets across all queue, messaging, and search endpoints.
