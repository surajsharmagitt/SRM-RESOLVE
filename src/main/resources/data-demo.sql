-- =====================================================
-- SRM-RESOLVE: Classroom Demonstration Seed Data
-- Loaded ONLY when app.demo-mode=true
-- Contains local demonstration accounts (with demo credentials)
-- and sample tickets/messages for classroom evaluation.
-- All real university official names replaced with generic demo roles.
-- =====================================================

-- ========== 1. Demonstration User Accounts ==========
-- Verified BCrypt hashes:
-- 'password123' = $2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy
-- 'admin123'    = $2a$10$HOmWAwafrAfrvWhi9RVMTuKIH0RYza69BEmwwdqUEEvlVbwIosVv2
-- 'staff123'    = $2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.

-- Admin user (user_id = 1)
INSERT IGNORE INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type) VALUES
(1, 'ADMIN001', 'Campus Administrator (Demo Account)', 'admin@srmap.edu.in', '$2a$10$HOmWAwafrAfrvWhi9RVMTuKIH0RYza69BEmwwdqUEEvlVbwIosVv2', 'ROLE_ADMIN', 'VERIFIED', NULL, 'NA');

-- Executive / VC (user_id = 2)
INSERT IGNORE INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type) VALUES
(2, 'EXEC001', 'Office of Vice Chancellor (Demo Account)', 'vc@srmap.edu.in', '$2a$10$HOmWAwafrAfrvWhi9RVMTuKIH0RYza69BEmwwdqUEEvlVbwIosVv2', 'ROLE_EXECUTIVE', 'VERIFIED', NULL, 'NA');

-- Department Heads (user_id = 3 to 12)
INSERT IGNORE INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(3, 'DH_HOSTEL', 'Hostel & CLM Lead (Demo)', 'hostel.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HOSTEL_MAINT', 'STAFF_QUARTERS', 1),
(4, 'DH_WATER', 'Water & Plumbing Lead (Demo)', 'water.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'WATER_PLUMBING', 'STAFF_QUARTERS', 2),
(5, 'DH_MESS', 'Mess Committee Lead (Demo)', 'mess.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'MESS_COMMITTEE', 'STAFF_QUARTERS', 3),
(6, 'DH_HOUSE', 'Housekeeping Lead (Demo)', 'housekeeping.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HOUSEKEEPING', 'STAFF_QUARTERS', 4),
(7, 'DH_ITKM', 'ITKM Helpdesk Lead (Demo)', 'itkm.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'ITKM_HELPDESK', 'STAFF_QUARTERS', 5),
(8, 'DH_SECURITY', 'Campus Security Lead (Demo)', 'security.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'SECURITY_EMERGENCY', 'STAFF_QUARTERS', 6),
(9, 'DH_LABS', 'Academic Labs Lead (Demo)', 'labs.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'ACADEMIC_LABS', 'STAFF_QUARTERS', 7),
(10, 'DH_ESTATE', 'Estate & Grounds Lead (Demo)', 'estate.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HORTICULTURE_ESTATE', 'STAFF_QUARTERS', 8),
(11, 'DH_LAUNDRY', 'Laundry Services Lead (Demo)', 'laundry.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'LAUNDRY_SERVICES', 'STAFF_QUARTERS', 9),
(12, 'DH_COMMERCIAL', 'Student Welfare Lead (Demo)', 'commercial.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'COMMERCIAL_WELFARE', 'STAFF_QUARTERS', 10);

-- Verified Students (Fixed user_ids: Suraj = 18, Ananya = 19, Rahul = 20, Priya = 21, Vikram = 22)
INSERT IGNORE INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, study_year, section, residence_type, building_block, room_or_zone) VALUES
(18, 'AP25110070893', 'Suraj Sharma (Demo Student)', 'suraj_sharma@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'CSE', 1, 'F', 'HOSTELLER', 'Ganga Tower - Block A', 'G-412'),
(19, 'AP25110070100', 'Ananya Reddy (Demo Student)', 'ananya_reddy@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'CSE', 1, 'A', 'HOSTELLER', 'Yamuna Tower', 'Y-201'),
(20, 'AP24110060050', 'Rahul Verma (Demo Student)', 'rahul_verma@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'ECE', 2, 'B', 'HOSTELLER', 'Vedavathi Tower', 'V-305'),
(21, 'AP23110050200', 'Priya Patel (Demo Student)', 'priya_patel@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'ME', 3, 'C', 'DAY_SCHOLAR', 'Day Scholar (Non-Resident)', 'Campus Day Scholar'),
(22, 'AP25110070500', 'Vikram Singh (Demo Student)', 'vikram_singh@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'CSE', 1, 'D', 'HOSTELLER', 'Krishna Tower', 'K-108');

-- Department Staff (user_id = 30 to 44)
INSERT IGNORE INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(30, 'STAFF_H1', 'Hostel Maintenance Staff 1 (Demo)', 'hostel.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOSTEL_MAINT', 'STAFF_QUARTERS', 1),
(31, 'STAFF_H2', 'Hostel Maintenance Staff 2 (Demo)', 'hostel.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOSTEL_MAINT', 'STAFF_QUARTERS', 1),
(32, 'STAFF_W1', 'Water Plumbing Staff 1 (Demo)', 'water.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'WATER_PLUMBING', 'STAFF_QUARTERS', 2),
(33, 'STAFF_W2', 'Water Plumbing Staff 2 (Demo)', 'water.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'WATER_PLUMBING', 'STAFF_QUARTERS', 2),
(34, 'STAFF_M1', 'Mess Operations Staff 1 (Demo)', 'mess.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'MESS_COMMITTEE', 'STAFF_QUARTERS', 3),
(35, 'STAFF_M2', 'Mess Operations Staff 2 (Demo)', 'mess.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'MESS_COMMITTEE', 'STAFF_QUARTERS', 3),
(36, 'STAFF_HK1', 'Housekeeping Staff 1 (Demo)', 'housekeeping.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOUSEKEEPING', 'STAFF_QUARTERS', 4),
(37, 'STAFF_HK2', 'Housekeeping Staff 2 (Demo)', 'housekeeping.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOUSEKEEPING', 'STAFF_QUARTERS', 4),
(38, 'STAFF_IT1', 'ITKM Support Staff 1 (Demo)', 'itkm.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ITKM_HELPDESK', 'STAFF_QUARTERS', 5),
(39, 'STAFF_IT2', 'ITKM Support Staff 2 (Demo)', 'itkm.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ITKM_HELPDESK', 'STAFF_QUARTERS', 5),
(40, 'STAFF_SEC1', 'Quick Response Officer 1 (Demo)', 'security.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'SECURITY_EMERGENCY', 'STAFF_QUARTERS', 6),
(41, 'STAFF_LAB1', 'Lab Instrumentation Tech 1 (Demo)', 'labs.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ACADEMIC_LABS', 'STAFF_QUARTERS', 7),
(42, 'STAFF_EST1', 'Grounds Supervisor 1 (Demo)', 'estate.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HORTICULTURE_ESTATE', 'STAFF_QUARTERS', 8),
(43, 'STAFF_LND1', 'Laundry Service Staff 1 (Demo)', 'laundry.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'LAUNDRY_SERVICES', 'STAFF_QUARTERS', 9),
(44, 'STAFF_COM1', 'Commercial Inspector 1 (Demo)', 'commercial.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'COMMERCIAL_WELFARE', 'STAFF_QUARTERS', 10);

-- Institutional Safety & Academic Department Heads (user_id = 51 to 56)
INSERT IGNORE INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(51, 'DH_ICC', 'ICC Presiding Officer (Demo)', 'icc.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'INTERNAL_COMPLAINTS_COMMITTEE', 'STAFF_QUARTERS', 11),
(52, 'DH_RAGGING', 'Anti-Ragging Squad Chair (Demo)', 'antiragging.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'ANTI_RAGGING_CELL', 'STAFF_QUARTERS', 12),
(53, 'DH_DSA', 'Student Affairs Lead (Demo)', 'dsa.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'STUDENT_AFFAIRS_SGRC', 'STAFF_QUARTERS', 13),
(54, 'DH_COUNSELING', 'Student Counseling Lead (Demo)', 'counseling.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HEALTH_COUNSELING', 'STAFF_QUARTERS', 14),
(55, 'DH_COE', 'Controller of Examinations (Demo)', 'coe.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'EXAMINATION_COE', 'STAFF_QUARTERS', 15),
(56, 'DH_FINANCE', 'Finance Officer (Demo)', 'finance.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'FINANCE_ACCOUNTS', 'STAFF_QUARTERS', 16);

-- Additional Specialized Staff (user_id = 61 to 66)
INSERT IGNORE INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(61, 'STAFF_ICC1', 'ICC Convenor & Counselor (Demo)', 'icc.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'INTERNAL_COMPLAINTS_COMMITTEE', 'STAFF_QUARTERS', 11),
(62, 'STAFF_RAG1', 'Anti-Ragging Squad Officer (Demo)', 'antiragging.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ANTI_RAGGING_CELL', 'STAFF_QUARTERS', 12),
(63, 'STAFF_DSA1', 'Student Welfare Officer (Demo)', 'dsa.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'STUDENT_AFFAIRS_SGRC', 'STAFF_QUARTERS', 13),
(64, 'STAFF_COUN1', 'Resident Psychologist (Demo)', 'counseling.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HEALTH_COUNSELING', 'STAFF_QUARTERS', 14),
(65, 'STAFF_COE1', 'Assistant Controller of Exams (Demo)', 'coe.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'EXAMINATION_COE', 'STAFF_QUARTERS', 15),
(66, 'STAFF_FIN1', 'Student Billing Officer (Demo)', 'finance.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'FINANCE_ACCOUNTS', 'STAFF_QUARTERS', 16);

-- ========== 2. Sample Master Incident (Idempotent with master_incident_id = 1) ==========
INSERT IGNORE INTO master_incidents (master_incident_id, category_id, assigned_dept_id, building_block, incident_title, normalized_tokens, incident_status, affected_student_count) VALUES
(1, 2, 2, 'Ganga Tower - Block A', 'Water supply cut off in Ganga Tower - Block A', 'water supply cut off ganga block a', 'OPEN', 3);

-- ========== 3. Sample Grievance Tickets (Idempotent with fixed ticket_ids 1 to 5) ==========
-- Ticket 1: Individual AC complaint (Suraj = user_id 18)
INSERT IGNORE INTO grievance_tickets (ticket_id, ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
(1, 'SRM-2026-000001', 18, 1, 1, NULL, 'HOSTEL', 'Ganga Tower - Block A', 'G-412', 'My room AC is not cooling properly. Temperature stays at 30 degrees even after 2 hours.', 'MEDIUM', 'SUBMITTED',
 DATE_ADD(NOW(), INTERVAL 240 MINUTE), DATE_ADD(NOW(), INTERVAL 1440 MINUTE));

-- Ticket 2: Water outage linked to master incident (student 18 = Suraj)
INSERT IGNORE INTO grievance_tickets (ticket_id, ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
(2, 'SRM-2026-000002', 18, 2, 2, 1, 'HOSTEL', 'Ganga Tower - Block A', 'G-412', 'No water supply since morning in Ganga Tower Block A 4th floor.', 'HIGH', 'ACKNOWLEDGED',
 DATE_ADD(NOW(), INTERVAL 60 MINUTE), DATE_ADD(NOW(), INTERVAL 360 MINUTE));

-- Ticket 3: Water outage linked to same master incident (student 22 = Vikram, same building)
INSERT IGNORE INTO grievance_tickets (ticket_id, ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
(3, 'SRM-2026-000003', 22, 2, 2, 1, 'HOSTEL', 'Ganga Tower - Block A', 'G-415', 'Water not coming in my room since early morning.', 'HIGH', 'SUBMITTED',
 DATE_ADD(NOW(), INTERVAL 60 MINUTE), DATE_ADD(NOW(), INTERVAL 360 MINUTE));

-- Ticket 4: WiFi issue (student 19 = Ananya)
INSERT IGNORE INTO grievance_tickets (ticket_id, ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
(4, 'SRM-2026-000004', 19, 6, 5, NULL, 'HOSTEL', 'Yamuna Tower', 'Y-201', 'WiFi keeps disconnecting every 10 minutes. Cannot attend online classes.', 'HIGH', 'IN_PROGRESS',
 DATE_ADD(NOW(), INTERVAL 60 MINUTE), DATE_ADD(NOW(), INTERVAL 360 MINUTE));

-- Ticket 5: Mess food quality (student 20 = Rahul)
INSERT IGNORE INTO grievance_tickets (ticket_id, ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at, acknowledged_at, resolved_by_dept_at) VALUES
(5, 'SRM-2026-000005', 20, 4, 3, NULL, 'MESS_HALL', 'Annapoorna Mess Complex', 'Counter 3', 'Found hair in today lunch dal. Very unhygienic.', 'MEDIUM', 'AWAITING_STUDENT_CONFIRMATION',
 DATE_ADD(NOW(), INTERVAL 240 MINUTE), DATE_ADD(NOW(), INTERVAL 1440 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE));

-- ========== 4. Sample Status History (Idempotent with fixed history_ids 1 to 6) ==========
INSERT IGNORE INTO complaint_status_history (history_id, ticket_id, from_status, to_status, changed_by_user_id, action_note) VALUES
(1, 2, 'SUBMITTED', 'ACKNOWLEDGED', 32, 'Assigned to water plumbing team for Ganga Tower - Block A'),
(2, 4, 'SUBMITTED', 'ACKNOWLEDGED', 38, 'ITKM team investigating Yamuna Tower WiFi'),
(3, 4, 'ACKNOWLEDGED', 'IN_PROGRESS', 38, 'Technician dispatched to check router'),
(4, 5, 'SUBMITTED', 'ACKNOWLEDGED', 34, 'Mess committee notified about hygiene issue'),
(5, 5, 'ACKNOWLEDGED', 'IN_PROGRESS', 34, 'Kitchen staff instructed on hygiene protocols'),
(6, 5, 'IN_PROGRESS', 'AWAITING_STUDENT_CONFIRMATION', 34, 'Kitchen deep-cleaned and staff re-trained. Please confirm if resolved.');

-- ========== 5. Sample Ticket Messages (Idempotent with fixed message_ids 1 to 7) ==========
INSERT IGNORE INTO ticket_messages (message_id, ticket_id, sender_user_id, sender_role, message_body, is_system_alert) VALUES
(1, 2, 32, 'ROLE_DEPT_STAFF', 'We have identified the issue with the water pump for Ganga Tower Block A. Repair team is on the way.', FALSE),
(2, 2, 18, 'ROLE_STUDENT', 'Thank you for the quick response. How long will it take?', FALSE),
(3, 2, 32, 'ROLE_DEPT_STAFF', 'Estimated 2 hours for repair. We will update you.', FALSE),
(4, 4, 38, 'ROLE_DEPT_STAFF', 'We are checking the Yamuna Tower access point router configuration. Please try restarting your device once.', FALSE),
(5, 4, 19, 'ROLE_STUDENT', 'I already tried restarting. The issue persists.', FALSE),
(6, 5, 34, 'ROLE_DEPT_STAFF', 'We sincerely apologize for the hygiene issue. The kitchen has been deep cleaned and staff re-trained on protocols.', FALSE),
(7, 5, 34, 'ROLE_DEPT_STAFF', 'We have marked this as resolved. Please confirm if you are satisfied with the resolution.', FALSE);
