-- =====================================================
-- SRM-RESOLVE: Complete MySQL Schema
-- Tables created in strict foreign-key dependency order
-- =====================================================

-- Drop views first
DROP VIEW IF EXISTS v_department_satisfaction;
DROP VIEW IF EXISTS v_location_category_heatmap;
DROP VIEW IF EXISTS v_department_sla_performance;
DROP VIEW IF EXISTS v_category_monthly_anomaly_radar;

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS in_app_notifications;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS escalation_logs;
DROP TABLE IF EXISTS ticket_attachments;
DROP TABLE IF EXISTS ticket_messages;
DROP TABLE IF EXISTS complaint_status_history;
DROP TABLE IF EXISTS grievance_tickets;
DROP TABLE IF EXISTS master_incidents;
DROP TABLE IF EXISTS sla_policies;
DROP TABLE IF EXISTS routing_rules;
DROP TABLE IF EXISTS grievance_categories;
DROP TABLE IF EXISTS email_otp_verifications;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS student_directory;
DROP TABLE IF EXISTS departments;

-- ========== Table 1: departments ==========
CREATE TABLE departments (
    dept_id INT AUTO_INCREMENT PRIMARY KEY,
    dept_code VARCHAR(50) UNIQUE NOT NULL,
    dept_name VARCHAR(120) NOT NULL,
    contact_email VARCHAR(120) NOT NULL,
    monthly_alert_threshold INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 2: student_directory ==========
CREATE TABLE student_directory (
    reg_no VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    official_email VARCHAR(120) UNIQUE NOT NULL,
    admission_year INT NOT NULL,
    current_study_year INT NOT NULL,
    department_code VARCHAR(50) NOT NULL,
    section VARCHAR(10) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 3: users ==========
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reg_or_staff_id VARCHAR(30) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ROLE_STUDENT', 'ROLE_DEPT_STAFF', 'ROLE_DEPT_HEAD', 'ROLE_EXECUTIVE', 'ROLE_ADMIN') NOT NULL,
    verification_status ENUM('VERIFIED', 'PENDING_VERIFICATION', 'REJECTED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    department_code VARCHAR(50) NULL,
    study_year INT NULL,
    section VARCHAR(10) NULL,
    residence_type ENUM('HOSTELLER', 'DAY_SCHOLAR', 'STAFF_QUARTERS', 'NA') NOT NULL DEFAULT 'NA',
    building_block VARCHAR(60) NULL,
    room_or_zone VARCHAR(30) NULL,
    assigned_dept_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_dept FOREIGN KEY (assigned_dept_id) REFERENCES departments(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 4: email_otp_verifications ==========
CREATE TABLE email_otp_verifications (
    otp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL,
    reg_no VARCHAR(20) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    purpose ENUM('REGISTRATION', 'PASSWORD_RESET') NOT NULL DEFAULT 'REGISTRATION',
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    resend_available_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_lookup (email, reg_no, is_used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 5: grievance_categories ==========
CREATE TABLE grievance_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(40) UNIQUE NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    icon_class VARCHAR(50) NOT NULL,
    is_shared_utility BOOLEAN NOT NULL DEFAULT FALSE,
    default_priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    monthly_threshold_limit INT NOT NULL DEFAULT 100
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 6: routing_rules ==========
CREATE TABLE routing_rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    location_scope ENUM('HOSTEL', 'ACADEMIC_BLOCK', 'MESS_HALL', 'ANY') NOT NULL,
    target_dept_id INT NOT NULL,
    UNIQUE KEY uq_category_location (category_id, location_scope),
    CONSTRAINT fk_routing_category FOREIGN KEY (category_id) REFERENCES grievance_categories(category_id),
    CONSTRAINT fk_routing_dept FOREIGN KEY (target_dept_id) REFERENCES departments(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 7: sla_policies ==========
CREATE TABLE sla_policies (
    sla_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    ack_deadline_minutes INT NOT NULL,
    resolution_deadline_minutes INT NOT NULL,
    level1_escalate_to_role VARCHAR(30) NOT NULL DEFAULT 'ROLE_DEPT_HEAD',
    level2_escalate_to_role VARCHAR(30) NOT NULL DEFAULT 'ROLE_EXECUTIVE',
    UNIQUE KEY uq_category_priority (category_id, priority),
    CONSTRAINT fk_sla_category FOREIGN KEY (category_id) REFERENCES grievance_categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 8: master_incidents ==========
CREATE TABLE master_incidents (
    master_incident_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    assigned_dept_id INT NOT NULL,
    building_block VARCHAR(60) NOT NULL,
    incident_title VARCHAR(200) NOT NULL,
    normalized_tokens VARCHAR(500) NOT NULL,
    incident_status ENUM('OPEN', 'IN_PROGRESS', 'AWAITING_CONFIRMATION', 'RESOLVED') NOT NULL DEFAULT 'OPEN',
    affected_student_count INT NOT NULL DEFAULT 1,
    eta_timestamp TIMESTAMP NULL,
    is_synthetic_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_incident_lookup (category_id, building_block, incident_status, created_at),
    CONSTRAINT fk_master_category FOREIGN KEY (category_id) REFERENCES grievance_categories(category_id),
    CONSTRAINT fk_master_dept FOREIGN KEY (assigned_dept_id) REFERENCES departments(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 9: grievance_tickets ==========
CREATE TABLE grievance_tickets (
    ticket_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_reference VARCHAR(30) UNIQUE NOT NULL,
    student_user_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    assigned_dept_id INT NOT NULL,
    assigned_staff_user_id BIGINT NULL,
    master_incident_id BIGINT NULL,
    location_scope ENUM('HOSTEL', 'ACADEMIC_BLOCK', 'MESS_HALL', 'ANY') NOT NULL DEFAULT 'HOSTEL',
    building_block VARCHAR(60) NOT NULL,
    room_or_location VARCHAR(50) NOT NULL,
    raw_description TEXT NOT NULL,
    ai_enhanced_description TEXT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    status ENUM('SUBMITTED', 'ACKNOWLEDGED', 'IN_PROGRESS', 'AWAITING_STUDENT_CONFIRMATION', 'REOPENED', 'CLOSED') NOT NULL DEFAULT 'SUBMITTED',
    is_escalated BOOLEAN NOT NULL DEFAULT FALSE,
    escalation_level INT NOT NULL DEFAULT 0,
    ack_deadline_at TIMESTAMP NOT NULL,
    resolution_deadline_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP NULL,
    eta_given_at TIMESTAMP NULL,
    resolved_by_dept_at TIMESTAMP NULL,
    reopened_at TIMESTAMP NULL,
    reopen_count INT NOT NULL DEFAULT 0,
    closed_by_student_at TIMESTAMP NULL,
    student_rating INT NULL,
    student_feedback TEXT NULL,
    is_synthetic_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_tickets (student_user_id, created_at),
    INDEX idx_dept_status (assigned_dept_id, status),
    INDEX idx_category_date (category_id, created_at),
    INDEX idx_synthetic (is_synthetic_demo),
    CONSTRAINT fk_ticket_student FOREIGN KEY (student_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_ticket_category FOREIGN KEY (category_id) REFERENCES grievance_categories(category_id),
    CONSTRAINT fk_ticket_dept FOREIGN KEY (assigned_dept_id) REFERENCES departments(dept_id),
    CONSTRAINT fk_ticket_staff FOREIGN KEY (assigned_staff_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_ticket_master FOREIGN KEY (master_incident_id) REFERENCES master_incidents(master_incident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 10: complaint_status_history ==========
CREATE TABLE complaint_status_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    from_status VARCHAR(40) NULL,
    to_status VARCHAR(40) NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    action_note VARCHAR(255) NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_ticket FOREIGN KEY (ticket_id) REFERENCES grievance_tickets(ticket_id),
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 11: ticket_messages ==========
CREATE TABLE ticket_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    sender_user_id BIGINT NOT NULL,
    sender_role VARCHAR(30) NOT NULL,
    message_body TEXT NOT NULL,
    is_system_alert BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_ticket FOREIGN KEY (ticket_id) REFERENCES grievance_tickets(ticket_id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 12: ticket_attachments ==========
CREATE TABLE ticket_attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(80) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_ticket FOREIGN KEY (ticket_id) REFERENCES grievance_tickets(ticket_id),
    CONSTRAINT fk_attachment_user FOREIGN KEY (uploaded_by_user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 13: escalation_logs ==========
CREATE TABLE escalation_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    previous_level INT NOT NULL,
    new_level INT NOT NULL,
    escalated_to_role VARCHAR(30) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    escalated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_escalation_ticket FOREIGN KEY (ticket_id) REFERENCES grievance_tickets(ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 14: audit_logs ==========
CREATE TABLE audit_logs (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NULL,
    action_type VARCHAR(60) NOT NULL,
    target_entity VARCHAR(60) NOT NULL,
    target_id VARCHAR(60) NULL,
    details TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== Table 15: in_app_notifications ==========
CREATE TABLE in_app_notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    body TEXT NOT NULL,
    notification_type ENUM('TICKET_UPDATE', 'ESCALATION', 'RESOLUTION_REQUEST', 'RESOLUTION_CONFIRMED', 'RESOLUTION_REJECTED', 'ASSIGNMENT', 'PASSWORD_RESET', 'SYSTEM') NOT NULL,
    related_ticket_id BIGINT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_notifications (recipient_user_id, is_read, created_at),
    CONSTRAINT fk_notif_user FOREIGN KEY (recipient_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_notif_ticket FOREIGN KEY (related_ticket_id) REFERENCES grievance_tickets(ticket_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== View 1: v_category_monthly_anomaly_radar ==========
CREATE OR REPLACE VIEW v_category_monthly_anomaly_radar AS
SELECT
    c.category_id,
    c.category_code,
    c.display_name,
    c.monthly_threshold_limit,
    COUNT(t.ticket_id) AS total_student_reports,
    COUNT(DISTINCT t.master_incident_id) AS grouped_master_incidents,
    COALESCE(SUM(CASE WHEN t.master_incident_id IS NULL AND t.ticket_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS independent_incidents,
    (COUNT(DISTINCT t.master_incident_id) + COALESCE(SUM(CASE WHEN t.master_incident_id IS NULL AND t.ticket_id IS NOT NULL THEN 1 ELSE 0 END), 0)) AS total_underlying_incidents,
    COALESCE(SUM(CASE WHEN t.status = 'CLOSED' THEN 1 ELSE 0 END), 0) AS closed_reports,
    COALESCE(SUM(CASE WHEN t.is_escalated = TRUE THEN 1 ELSE 0 END), 0) AS escalated_reports,
    ROUND(AVG(CASE WHEN t.closed_by_student_at IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, t.created_at, t.closed_by_student_at) END), 1) AS avg_resolution_minutes,
    CASE WHEN COUNT(t.ticket_id) >= c.monthly_threshold_limit THEN 1 ELSE 0 END AS is_threshold_breached
FROM grievance_categories c
LEFT JOIN grievance_tickets t
    ON c.category_id = t.category_id
    AND t.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY c.category_id, c.category_code, c.display_name, c.monthly_threshold_limit;

-- ========== View 2: v_department_sla_performance ==========
-- Enhanced with SLA breach counts per Section 6.3
CREATE OR REPLACE VIEW v_department_sla_performance AS
SELECT
    d.dept_id,
    d.dept_code,
    d.dept_name,
    COUNT(t.ticket_id) AS total_assigned_tickets,
    COALESCE(SUM(CASE WHEN t.status = 'CLOSED' THEN 1 ELSE 0 END), 0) AS total_closed_tickets,
    COALESCE(SUM(CASE WHEN t.status = 'REOPENED' THEN 1 ELSE 0 END), 0) AS total_reopened_tickets,
    COALESCE(SUM(CASE WHEN t.acknowledged_at IS NOT NULL AND t.acknowledged_at <= t.ack_deadline_at THEN 1 ELSE 0 END), 0) AS ack_sla_met_count,
    COALESCE(SUM(CASE WHEN t.acknowledged_at IS NOT NULL AND t.acknowledged_at > t.ack_deadline_at THEN 1 ELSE 0 END), 0) AS ack_sla_breached_count,
    COALESCE(SUM(CASE WHEN t.closed_by_student_at IS NOT NULL AND t.closed_by_student_at <= t.resolution_deadline_at THEN 1 ELSE 0 END), 0) AS resolution_sla_met_count,
    COALESCE(SUM(CASE WHEN t.closed_by_student_at IS NOT NULL AND t.closed_by_student_at > t.resolution_deadline_at THEN 1 ELSE 0 END), 0) AS resolution_sla_breached_count,
    COALESCE(SUM(CASE WHEN t.status NOT IN ('CLOSED', 'AWAITING_STUDENT_CONFIRMATION') AND NOW() > t.resolution_deadline_at THEN 1 ELSE 0 END), 0) AS currently_overdue_count,
    COALESCE(SUM(CASE WHEN t.is_escalated = TRUE THEN 1 ELSE 0 END), 0) AS escalated_ticket_count,
    ROUND(AVG(CASE WHEN t.closed_by_student_at IS NOT NULL THEN TIMESTAMPDIFF(MINUTE, t.created_at, t.closed_by_student_at) END), 1) AS avg_closure_minutes
FROM departments d
LEFT JOIN grievance_tickets t ON d.dept_id = t.assigned_dept_id
GROUP BY d.dept_id, d.dept_code, d.dept_name;

-- ========== View 3: v_location_category_heatmap (Section 6.3) ==========
CREATE OR REPLACE VIEW v_location_category_heatmap AS
SELECT
    t.building_block,
    t.location_scope,
    c.category_id,
    c.category_code,
    c.display_name AS category_name,
    COUNT(t.ticket_id) AS ticket_count,
    COALESCE(SUM(CASE WHEN t.status NOT IN ('CLOSED') THEN 1 ELSE 0 END), 0) AS open_ticket_count,
    COALESCE(SUM(CASE WHEN t.is_escalated = TRUE THEN 1 ELSE 0 END), 0) AS escalated_count,
    COUNT(DISTINCT t.student_user_id) AS unique_affected_students
FROM grievance_tickets t
JOIN grievance_categories c ON t.category_id = c.category_id
WHERE t.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY t.building_block, t.location_scope, c.category_id, c.category_code, c.display_name;

-- ========== View 4: v_department_satisfaction ==========
CREATE OR REPLACE VIEW v_department_satisfaction AS
SELECT
    d.dept_id,
    d.dept_code,
    d.dept_name,
    COUNT(t.ticket_id) AS total_closed,
    ROUND(AVG(t.student_rating), 2) AS avg_student_rating,
    COALESCE(SUM(CASE WHEN t.student_rating >= 4 THEN 1 ELSE 0 END), 0) AS positive_ratings_count,
    COALESCE(SUM(CASE WHEN t.student_rating <= 2 AND t.student_rating IS NOT NULL THEN 1 ELSE 0 END), 0) AS negative_ratings_count,
    ROUND(AVG(TIMESTAMPDIFF(MINUTE, t.created_at, t.acknowledged_at)), 1) AS avg_first_response_minutes,
    ROUND(AVG(TIMESTAMPDIFF(MINUTE, t.created_at, t.closed_by_student_at)), 1) AS avg_turnaround_minutes,
    ROUND((COUNT(CASE WHEN t.closed_by_student_at <= t.resolution_deadline_at THEN 1 END) * 100.0) / NULLIF(COUNT(t.ticket_id), 0), 1) AS sla_resolution_compliance_pct
FROM departments d
LEFT JOIN grievance_tickets t ON d.dept_id = t.assigned_dept_id AND t.status = 'CLOSED'
GROUP BY d.dept_id, d.dept_code, d.dept_name;
