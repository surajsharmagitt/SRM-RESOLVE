-- =====================================================
-- SRM-RESOLVE: Essential Production Seed Data
-- Idempotent, Safe for Repeated Startups
-- Contains ONLY institutional reference data:
--   1. Departments
--   2. Student Directory (Authorized enrollment roster)
--   3. Grievance Categories
--   4. Routing Rules
--   5. SLA Policies
--
-- Demonstration user accounts and sample test tickets
-- have been separated into data-demo.sql and are seeded
-- ONLY when app.demo-mode=true.
-- =====================================================

-- ========== 1. Departments ==========
INSERT IGNORE INTO departments (dept_id, dept_code, dept_name, contact_email, monthly_alert_threshold) VALUES
(1, 'HOSTEL_MAINT', 'Campus Life & Maintenance (CLM)', 'director.clm@srmap.edu.in', 100),
(2, 'WATER_PLUMBING', 'Water, RO Plant & Plumbing Services', 'water.plumbing@srmap.edu.in', 80),
(3, 'MESS_COMMITTEE', 'Annapoorna Mess & Dining Committee', 'mess.committee@srmap.edu.in', 100),
(4, 'HOUSEKEEPING', 'Housekeeping & Campus Sanitation', 'housekeeping@srmap.edu.in', 80),
(5, 'ITKM_HELPDESK', 'IT & Knowledge Management (ITKM) Helpdesk', 'itkm.helpdesk@srmap.edu.in', 60),
(6, 'SECURITY_EMERGENCY', 'Campus Security & Quick Response Emergency Team', 'security.emergency@srmap.edu.in', 20),
(7, 'ACADEMIC_LABS', 'Academic Labs & Scientific Instrumentation', 'labs.engg@srmap.edu.in', 40),
(8, 'HORTICULTURE_ESTATE', 'Campus Horticulture, Roads & Grounds', 'estate.grounds@srmap.edu.in', 50),
(9, 'LAUNDRY_SERVICES', 'Campus Laundry & Student Care', 'laundry.services@srmap.edu.in', 60),
(10, 'COMMERCIAL_WELFARE', 'Commercial Outlets & Student Welfare', 'commercial.welfare@srmap.edu.in', 30),
(11, 'INTERNAL_COMPLAINTS_COMMITTEE', 'Internal Complaints Committee (ICC) & Anti-Harassment Cell', 'icc@srmap.edu.in', 20),
(12, 'ANTI_RAGGING_CELL', 'Anti-Ragging Committee & Squad', 'antiragging@srmap.edu.in', 20),
(13, 'STUDENT_AFFAIRS_SGRC', 'Directorate of Student Affairs (DSA) & SGRC', 'grievances@srmap.edu.in', 40),
(14, 'HEALTH_COUNSELING', 'Health Centre, Wellness & Student Counseling', 'counseling@srmap.edu.in', 30),
(15, 'EXAMINATION_COE', 'Office of Controller of Examinations (CoE)', 'coe@srmap.edu.in', 50),
(16, 'FINANCE_ACCOUNTS', 'Directorate of Finance & Accounts', 'finance@srmap.edu.in', 40);

-- ========== 2. Student Directory (Authorized enrollment roster) ==========
INSERT IGNORE INTO student_directory (reg_no, full_name, official_email, admission_year, current_study_year, department_code, section, is_active) VALUES
('AP25110070893', 'Suraj Sharma', 'suraj_sharma@srmap.edu.in', 2025, 1, 'CSE', 'F', TRUE),
('AP25110070100', 'Ananya Reddy', 'ananya_reddy@srmap.edu.in', 2025, 1, 'CSE', 'A', TRUE),
('AP24110060050', 'Rahul Verma', 'rahul_verma@srmap.edu.in', 2024, 2, 'ECE', 'B', TRUE),
('AP23110050200', 'Priya Patel', 'priya_patel@srmap.edu.in', 2023, 3, 'ME', 'C', TRUE),
('AP25110070500', 'Vikram Singh', 'vikram_singh@srmap.edu.in', 2025, 1, 'CSE', 'D', TRUE),
('AP25110070999', 'Kavya Nair', 'kavya_n@srmap.edu.in', 2025, 1, 'CSE', 'E', TRUE);

-- ========== 3. Grievance Categories (23 Total) ==========
INSERT IGNORE INTO grievance_categories (category_id, category_code, display_name, icon_class, is_shared_utility, default_priority, monthly_threshold_limit) VALUES
(1, 'AC_COOLING', 'AC Not Cooling', 'bi-thermometer-snow', FALSE, 'MEDIUM', 100),
(2, 'WATER_OUTAGE', 'Water Supply Cut', 'bi-droplet-half', TRUE, 'HIGH', 80),
(3, 'HOT_COLD_WATER', 'Hot/Cold Water Dispenser', 'bi-cup-hot', TRUE, 'MEDIUM', 80),
(4, 'MESS_FOOD_QUALITY', 'Mess Food Quality', 'bi-egg-fried', TRUE, 'MEDIUM', 100),
(5, 'ROOM_CLEANING', 'Room Cleaning', 'bi-house-heart', FALSE, 'LOW', 80),
(6, 'WIFI_NETWORK', 'Wi-Fi / Network Issue', 'bi-wifi-off', TRUE, 'HIGH', 60),
(7, 'ELECTRICAL_REPAIR', 'Electrical Repair', 'bi-lightning-charge', FALSE, 'HIGH', 80),
(8, 'CLASSROOM_AV', 'Classroom AV Equipment', 'bi-display', TRUE, 'MEDIUM', 60),
(9, 'SNAKE_WILDLIFE_SOS', 'Snake / Wildlife Threat (SOS)', 'bi-shield-exclamation', TRUE, 'CRITICAL', 20),
(10, 'FIRE_SAFETY_SOS', 'Fire, Smoke & Gas Leak (SOS)', 'bi-fire', TRUE, 'CRITICAL', 20),
(11, 'MEDICAL_EMERGENCY_SOS', 'Medical Emergency (SOS)', 'bi-heart-pulse-fill', TRUE, 'CRITICAL', 20),
(12, 'LAB_INSTRUMENTATION', 'Academic Lab Equipment & Voltmeter', 'bi-cpu', FALSE, 'MEDIUM', 40),
(13, 'FURNITURE_CARPENTRY', 'Broken Furniture & Carpentry', 'bi-inboxes', FALSE, 'LOW', 50),
(14, 'HORTICULTURE_ROADS', 'Campus Roads, Leaves & Grounds', 'bi-tree', TRUE, 'LOW', 50),
(15, 'LAUNDRY_CLOTHES', 'Hostel Laundry & Clothes Washing', 'bi-basket2', FALSE, 'LOW', 60),
(16, 'VENDOR_OVERPRICING', 'Canteen / Vendor Overpricing & MRP', 'bi-shop', FALSE, 'HIGH', 30),
(17, 'GENERAL_CAMPUS_ISSUE', 'General Campus Infrastructure Concern', 'bi-chat-square-text', FALSE, 'MEDIUM', 80),
(18, 'CYBER_HARASSMENT', 'Online Harassment & Cyber Stalking (Confidential)', 'bi-shield-shaded', FALSE, 'CRITICAL', 20),
(19, 'ANTI_RAGGING', 'Ragging & Senior Intimidation (Zero-Tolerance)', 'bi-shield-x', FALSE, 'CRITICAL', 20),
(20, 'EXAM_EVALUATION', 'Examinations, Results & Grade Evaluation', 'bi-journal-check', FALSE, 'HIGH', 40),
(21, 'FEE_FINANCE', 'Tuition Fees, Scholarship & Finance Office', 'bi-cash-coin', FALSE, 'HIGH', 40),
(22, 'STUDENT_COUNSELING', 'Student Wellness & Confidential Counseling', 'bi-heart-pulse', FALSE, 'CRITICAL', 20),
(23, 'STUDENT_DISPUTES', 'Student Affairs & Hostel Mediation', 'bi-people', FALSE, 'MEDIUM', 30);

-- ========== 4. Routing Rules ==========
INSERT IGNORE INTO routing_rules (category_id, location_scope, target_dept_id) VALUES
-- 1. AC_COOLING -> HOSTEL_MAINT
(1, 'HOSTEL', 1),
(1, 'ACADEMIC_BLOCK', 1),
(1, 'ANY', 1),
-- 2. WATER_OUTAGE -> WATER_PLUMBING
(2, 'HOSTEL', 2),
(2, 'ACADEMIC_BLOCK', 2),
(2, 'ANY', 2),
-- 3. HOT_COLD_WATER -> WATER_PLUMBING
(3, 'HOSTEL', 2),
(3, 'MESS_HALL', 2),
(3, 'ANY', 2),
-- 4. MESS_FOOD_QUALITY -> MESS_COMMITTEE
(4, 'MESS_HALL', 3),
(4, 'ANY', 3),
-- 5. ROOM_CLEANING -> HOUSEKEEPING
(5, 'HOSTEL', 4),
(5, 'ANY', 4),
-- 6. WIFI_NETWORK -> ITKM_HELPDESK
(6, 'HOSTEL', 5),
(6, 'ACADEMIC_BLOCK', 5),
(6, 'ANY', 5),
-- 7. ELECTRICAL_REPAIR -> HOSTEL_MAINT
(7, 'HOSTEL', 1),
(7, 'ACADEMIC_BLOCK', 1),
(7, 'ANY', 1),
-- 8. CLASSROOM_AV -> ITKM_HELPDESK
(8, 'ACADEMIC_BLOCK', 5),
(8, 'ANY', 5),
-- 9. SNAKE_WILDLIFE_SOS -> SECURITY_EMERGENCY
(9, 'ANY', 6),
-- 10. FIRE_SAFETY_SOS -> SECURITY_EMERGENCY
(10, 'ANY', 6),
-- 11. MEDICAL_EMERGENCY_SOS -> SECURITY_EMERGENCY
(11, 'ANY', 6),
-- 12. LAB_INSTRUMENTATION -> ACADEMIC_LABS
(12, 'ACADEMIC_BLOCK', 7),
(12, 'ANY', 7),
-- 13. FURNITURE_CARPENTRY -> HOSTEL_MAINT
(13, 'HOSTEL', 1),
(13, 'ACADEMIC_BLOCK', 1),
(13, 'ANY', 1),
-- 14. HORTICULTURE_ROADS -> HORTICULTURE_ESTATE
(14, 'ANY', 8),
-- 15. LAUNDRY_CLOTHES -> LAUNDRY_SERVICES
(15, 'HOSTEL', 9),
(15, 'ANY', 9),
-- 16. VENDOR_OVERPRICING -> COMMERCIAL_WELFARE
(16, 'MESS_HALL', 10),
(16, 'ANY', 10),
-- 17. GENERAL_CAMPUS_ISSUE -> HOSTEL_MAINT
(17, 'ANY', 1),
-- 18. CYBER_HARASSMENT -> INTERNAL_COMPLAINTS_COMMITTEE
(18, 'ANY', 11),
-- 19. ANTI_RAGGING -> ANTI_RAGGING_CELL
(19, 'ANY', 12),
-- 20. EXAM_EVALUATION -> EXAMINATION_COE
(20, 'ANY', 15),
-- 21. FEE_FINANCE -> FINANCE_ACCOUNTS
(21, 'ANY', 16),
-- 22. STUDENT_COUNSELING -> HEALTH_COUNSELING
(22, 'ANY', 14),
-- 23. STUDENT_DISPUTES -> STUDENT_AFFAIRS_SGRC
(23, 'HOSTEL', 13),
(23, 'ANY', 13);

-- ========== 5. SLA Policies ==========
INSERT IGNORE INTO sla_policies (category_id, priority, ack_deadline_minutes, resolution_deadline_minutes) VALUES
-- 1. AC_COOLING
(1, 'LOW', 480, 2880), (1, 'MEDIUM', 240, 1440), (1, 'HIGH', 120, 720), (1, 'CRITICAL', 60, 360),
-- 2. WATER_OUTAGE
(2, 'LOW', 240, 1440), (2, 'MEDIUM', 120, 720), (2, 'HIGH', 60, 360), (2, 'CRITICAL', 30, 180),
-- 3. HOT_COLD_WATER
(3, 'LOW', 480, 2880), (3, 'MEDIUM', 240, 1440), (3, 'HIGH', 120, 720), (3, 'CRITICAL', 60, 360),
-- 4. MESS_FOOD_QUALITY
(4, 'LOW', 480, 2880), (4, 'MEDIUM', 240, 1440), (4, 'HIGH', 120, 720), (4, 'CRITICAL', 60, 360),
-- 5. ROOM_CLEANING
(5, 'LOW', 480, 2880), (5, 'MEDIUM', 240, 1440), (5, 'HIGH', 120, 720), (5, 'CRITICAL', 60, 360),
-- 6. WIFI_NETWORK
(6, 'LOW', 240, 1440), (6, 'MEDIUM', 120, 720), (6, 'HIGH', 60, 360), (6, 'CRITICAL', 30, 180),
-- 7. ELECTRICAL_REPAIR
(7, 'LOW', 240, 1440), (7, 'MEDIUM', 120, 720), (7, 'HIGH', 60, 360), (7, 'CRITICAL', 30, 180),
-- 8. CLASSROOM_AV
(8, 'LOW', 480, 2880), (8, 'MEDIUM', 240, 1440), (8, 'HIGH', 120, 720), (8, 'CRITICAL', 60, 360),
-- 9. SNAKE_WILDLIFE_SOS
(9, 'LOW', 120, 480), (9, 'MEDIUM', 60, 240), (9, 'HIGH', 30, 120), (9, 'CRITICAL', 15, 60),
-- 10. FIRE_SAFETY_SOS
(10, 'LOW', 120, 480), (10, 'MEDIUM', 60, 240), (10, 'HIGH', 30, 120), (10, 'CRITICAL', 10, 30),
-- 11. MEDICAL_EMERGENCY_SOS
(11, 'LOW', 120, 480), (11, 'MEDIUM', 60, 240), (11, 'HIGH', 30, 120), (11, 'CRITICAL', 10, 30),
-- 12. LAB_INSTRUMENTATION
(12, 'LOW', 480, 2880), (12, 'MEDIUM', 240, 1440), (12, 'HIGH', 120, 720), (12, 'CRITICAL', 30, 180),
-- 13. FURNITURE_CARPENTRY
(13, 'LOW', 480, 2880), (13, 'MEDIUM', 240, 1440), (13, 'HIGH', 120, 720), (13, 'CRITICAL', 30, 180),
-- 14. HORTICULTURE_ROADS
(14, 'LOW', 480, 2880), (14, 'MEDIUM', 240, 1440), (14, 'HIGH', 120, 720), (14, 'CRITICAL', 30, 180),
-- 15. LAUNDRY_CLOTHES
(15, 'LOW', 480, 2880), (15, 'MEDIUM', 240, 1440), (15, 'HIGH', 120, 720), (15, 'CRITICAL', 30, 180),
-- 16. VENDOR_OVERPRICING
(16, 'LOW', 480, 2880), (16, 'MEDIUM', 240, 1440), (16, 'HIGH', 120, 720), (16, 'CRITICAL', 30, 180),
-- 17. GENERAL_CAMPUS_ISSUE
(17, 'LOW', 480, 2880), (17, 'MEDIUM', 240, 1440), (17, 'HIGH', 120, 720), (17, 'CRITICAL', 30, 180),
-- 18. CYBER_HARASSMENT (Confidential ICC Safety Response)
(18, 'LOW', 120, 720), (18, 'MEDIUM', 60, 480), (18, 'HIGH', 30, 240), (18, 'CRITICAL', 15, 120),
-- 19. ANTI_RAGGING (Zero-Tolerance Response)
(19, 'LOW', 120, 480), (19, 'MEDIUM', 60, 240), (19, 'HIGH', 30, 120), (19, 'CRITICAL', 15, 60),
-- 20. EXAM_EVALUATION
(20, 'LOW', 480, 2880), (20, 'MEDIUM', 240, 1440), (20, 'HIGH', 120, 720), (20, 'CRITICAL', 60, 360),
-- 21. FEE_FINANCE
(21, 'LOW', 480, 2880), (21, 'MEDIUM', 240, 1440), (21, 'HIGH', 120, 720), (21, 'CRITICAL', 60, 360),
-- 22. STUDENT_COUNSELING (Wellness Response)
(22, 'LOW', 120, 480), (22, 'MEDIUM', 60, 240), (22, 'HIGH', 30, 120), (22, 'CRITICAL', 15, 60),
-- 23. STUDENT_DISPUTES
(23, 'LOW', 480, 2880), (23, 'MEDIUM', 240, 1440), (23, 'HIGH', 120, 720), (23, 'CRITICAL', 60, 360);
