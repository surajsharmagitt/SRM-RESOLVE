-- =====================================================
-- SRM-RESOLVE: Seed Data
-- Inserts in strict FK dependency order
-- All timestamps and intervals aligned to Asia/Kolkata (IST)
-- Zero emdashes used throughout
-- =====================================================

-- ========== 1. Departments ==========
INSERT INTO departments (dept_id, dept_code, dept_name, contact_email, monthly_alert_threshold) VALUES
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

-- ========== 2. Student Directory (Authorized roster) ==========
INSERT INTO student_directory (reg_no, full_name, official_email, admission_year, current_study_year, department_code, section, is_active) VALUES
('AP25110070893', 'Suraj Sharma', 'suraj_sharma@srmap.edu.in', 2025, 1, 'CSE', 'F', TRUE),
('AP25110070100', 'Ananya Reddy', 'ananya_reddy@srmap.edu.in', 2025, 1, 'CSE', 'A', TRUE),
('AP24110060050', 'Rahul Verma', 'rahul_verma@srmap.edu.in', 2024, 2, 'ECE', 'B', TRUE),
('AP23110050200', 'Priya Patel', 'priya_patel@srmap.edu.in', 2023, 3, 'ME', 'C', TRUE),
('AP25110070500', 'Vikram Singh', 'vikram_singh@srmap.edu.in', 2025, 1, 'CSE', 'D', TRUE),
('AP25110070999', 'Kavya Nair', 'kavya_n@srmap.edu.in', 2025, 1, 'CSE', 'E', TRUE);

-- ========== 3. Users ==========
-- Verified BCrypt hashes:
-- 'password123' = $2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy
-- 'admin123'    = $2a$10$HOmWAwafrAfrvWhi9RVMTuKIH0RYza69BEmwwdqUEEvlVbwIosVv2
-- 'staff123'    = $2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.

-- Admin user (user_id = 1)
INSERT INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type) VALUES
(1, 'ADMIN001', 'Dr. Vinayak Kalluri (Registrar & Admin)', 'admin@srmap.edu.in', '$2a$10$HOmWAwafrAfrvWhi9RVMTuKIH0RYza69BEmwwdqUEEvlVbwIosVv2', 'ROLE_ADMIN', 'VERIFIED', NULL, 'NA');

-- Executive / VC (user_id = 2)
INSERT INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type) VALUES
(2, 'EXEC001', 'Prof. Ch. Satish Kumar (Vice Chancellor)', 'vc@srmap.edu.in', '$2a$10$HOmWAwafrAfrvWhi9RVMTuKIH0RYza69BEmwwdqUEEvlVbwIosVv2', 'ROLE_EXECUTIVE', 'VERIFIED', NULL, 'NA');

-- Department Heads (Heads 1 to 10: user_id = 3 to 12)
INSERT INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(3, 'DH_HOSTEL', 'Mr. Anup Singh Suryavanshi (Director - CLM)', 'hostel.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HOSTEL_MAINT', 'STAFF_QUARTERS', 1),
(4, 'DH_WATER', 'Mr. Suresh Babu (Lead - Water Services)', 'water.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'WATER_PLUMBING', 'STAFF_QUARTERS', 2),
(5, 'DH_MESS', 'Mrs. Lakshmi Devi (Annapoorna Mess Head)', 'mess.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'MESS_COMMITTEE', 'STAFF_QUARTERS', 3),
(6, 'DH_HOUSE', 'Mr. Naresh Yadav (Housekeeping Head)', 'housekeeping.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HOUSEKEEPING', 'STAFF_QUARTERS', 4),
(7, 'DH_ITKM', 'Dr. Satish Anamalamudi (Director - ITKM)', 'itkm.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'ITKM_HELPDESK', 'STAFF_QUARTERS', 5),
(8, 'DH_SECURITY', 'Mr. K. Veerabhadra Rao (Chief Security Officer)', 'security.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'SECURITY_EMERGENCY', 'STAFF_QUARTERS', 6),
(9, 'DH_LABS', 'Dr. T. Murali Krishna (Director of Engineering Labs)', 'labs.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'ACADEMIC_LABS', 'STAFF_QUARTERS', 7),
(10, 'DH_ESTATE', 'Mr. P. Venkateswara Rao (Estate & Grounds Officer)', 'estate.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HORTICULTURE_ESTATE', 'STAFF_QUARTERS', 8),
(11, 'DH_LAUNDRY', 'Mrs. S. Sujatha (Laundry Operations Manager)', 'laundry.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'LAUNDRY_SERVICES', 'STAFF_QUARTERS', 9),
(12, 'DH_COMMERCIAL', 'Dr. Ramesh Vaddi (Student Welfare & Commercial Outlets)', 'commercial.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'COMMERCIAL_WELFARE', 'STAFF_QUARTERS', 10);

-- Verified Students (Fixed user_ids: Suraj = 18, Ananya = 19, Rahul = 20, Priya = 21, Vikram = 22)
INSERT INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, study_year, section, residence_type, building_block, room_or_zone) VALUES
(18, 'AP25110070893', 'Suraj Sharma', 'suraj_sharma@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'CSE', 1, 'F', 'HOSTELLER', 'Ganga Tower - Block A', 'G-412'),
(19, 'AP25110070100', 'Ananya Reddy', 'ananya_reddy@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'CSE', 1, 'A', 'HOSTELLER', 'Yamuna Tower', 'Y-201'),
(20, 'AP24110060050', 'Rahul Verma', 'rahul_verma@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'ECE', 2, 'B', 'HOSTELLER', 'Vedavathi Tower', 'V-305'),
(21, 'AP23110050200', 'Priya Patel', 'priya_patel@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'ME', 3, 'C', 'DAY_SCHOLAR', 'Day Scholar (Non-Resident)', 'Campus Day Scholar'),
(22, 'AP25110070500', 'Vikram Singh', 'vikram_singh@srmap.edu.in', '$2a$10$vV.xlyQPavBD6PjDMPJXv.9lFTrr2.rIyhOCbfKFsODgrsOw2Tipy', 'ROLE_STUDENT', 'VERIFIED', 'CSE', 1, 'D', 'HOSTELLER', 'Krishna Tower', 'K-108');

-- Department Staff (user_id = 30 to 45)
INSERT INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(30, 'STAFF_H1', 'Ramesh (CLM Staff 1)', 'hostel.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOSTEL_MAINT', 'STAFF_QUARTERS', 1),
(31, 'STAFF_H2', 'Ganesh (CLM Staff 2)', 'hostel.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOSTEL_MAINT', 'STAFF_QUARTERS', 1),
(32, 'STAFF_W1', 'Mahesh (Water Staff 1)', 'water.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'WATER_PLUMBING', 'STAFF_QUARTERS', 2),
(33, 'STAFF_W2', 'Dinesh (Water Staff 2)', 'water.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'WATER_PLUMBING', 'STAFF_QUARTERS', 2),
(34, 'STAFF_M1', 'Harish (Mess Staff 1)', 'mess.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'MESS_COMMITTEE', 'STAFF_QUARTERS', 3),
(35, 'STAFF_M2', 'Kavitha (Mess Staff 2)', 'mess.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'MESS_COMMITTEE', 'STAFF_QUARTERS', 3),
(36, 'STAFF_HK1', 'Rajesh (Housekeeping Staff 1)', 'housekeeping.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOUSEKEEPING', 'STAFF_QUARTERS', 4),
(37, 'STAFF_HK2', 'Sunitha (Housekeeping Staff 2)', 'housekeeping.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HOUSEKEEPING', 'STAFF_QUARTERS', 4),
(38, 'STAFF_IT1', 'Venkat (ITKM Staff 1)', 'itkm.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ITKM_HELPDESK', 'STAFF_QUARTERS', 5),
(39, 'STAFF_IT2', 'Deepa (ITKM Staff 2)', 'itkm.staff2@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ITKM_HELPDESK', 'STAFF_QUARTERS', 5),
(40, 'STAFF_SEC1', 'Rama Rao (Quick Response Officer 1)', 'security.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'SECURITY_EMERGENCY', 'STAFF_QUARTERS', 6),
(41, 'STAFF_LAB1', 'Raghu (Lab Instrumentation Tech 1)', 'labs.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ACADEMIC_LABS', 'STAFF_QUARTERS', 7),
(42, 'STAFF_EST1', 'Appa Rao (Grounds Supervisor 1)', 'estate.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HORTICULTURE_ESTATE', 'STAFF_QUARTERS', 8),
(43, 'STAFF_LND1', 'Srinivas (Laundry Service Staff 1)', 'laundry.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'LAUNDRY_SERVICES', 'STAFF_QUARTERS', 9),
(44, 'STAFF_COM1', 'Kishore (Commercial Inspector 1)', 'commercial.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'COMMERCIAL_WELFARE', 'STAFF_QUARTERS', 10);

-- Institutional Safety & Academic Department Heads (user_id = 51 to 56)
INSERT INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(51, 'DH_ICC', 'Prof. Poovammal E (Presiding Officer - ICC)', 'icc.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'INTERNAL_COMPLAINTS_COMMITTEE', 'STAFF_QUARTERS', 11),
(52, 'DH_RAGGING', 'Prof. Ch. Satish Kumar (Chair - Anti-Ragging Committee)', 'antiragging.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'ANTI_RAGGING_CELL', 'STAFF_QUARTERS', 12),
(53, 'DH_DSA', 'Ms. B. Revathi (Associate Director - Student Affairs)', 'dsa.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'STUDENT_AFFAIRS_SGRC', 'STAFF_QUARTERS', 13),
(54, 'DH_COUNSELING', 'Dr. K. Sarada (Lead - Student Counseling Centre)', 'counseling.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'HEALTH_COUNSELING', 'STAFF_QUARTERS', 14),
(55, 'DH_COE', 'Prof. Sheela Singh (Controller of Examinations)', 'coe.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'EXAMINATION_COE', 'STAFF_QUARTERS', 15),
(56, 'DH_FINANCE', 'Mr. M. Sridhar (Finance Officer)', 'finance.head@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_HEAD', 'VERIFIED', 'FINANCE_ACCOUNTS', 'STAFF_QUARTERS', 16);

-- Additional Specialized Staff (user_id = 61 to 66)
INSERT INTO users (user_id, reg_or_staff_id, full_name, email, password_hash, role, verification_status, department_code, residence_type, assigned_dept_id) VALUES
(61, 'STAFF_ICC1', 'Dr. Jeena Anne K (ICC Convenor & Counselor)', 'icc.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'INTERNAL_COMPLAINTS_COMMITTEE', 'STAFF_QUARTERS', 11),
(62, 'STAFF_RAG1', 'Mr. T. Subba Rao (Anti-Ragging Squad Officer)', 'antiragging.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'ANTI_RAGGING_CELL', 'STAFF_QUARTERS', 12),
(63, 'STAFF_DSA1', 'Mr. K. Sai Kumar (Student Welfare Officer)', 'dsa.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'STUDENT_AFFAIRS_SGRC', 'STAFF_QUARTERS', 13),
(64, 'STAFF_COUN1', 'Ms. P. Swathi (Resident Psychologist)', 'counseling.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'HEALTH_COUNSELING', 'STAFF_QUARTERS', 14),
(65, 'STAFF_COE1', 'Mr. V. Krishna Mohan (Assistant Controller of Exams)', 'coe.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'EXAMINATION_COE', 'STAFF_QUARTERS', 15),
(66, 'STAFF_FIN1', 'Mrs. N. Suhasini (Student Billing & Accounts Officer)', 'finance.staff1@srmap.edu.in', '$2a$10$aVVflfClHDPaz5BFacMVu.p0wiXIN2GX0dz2N1K/kWJkKBgQpqOS.', 'ROLE_DEPT_STAFF', 'VERIFIED', 'FINANCE_ACCOUNTS', 'STAFF_QUARTERS', 16);

-- ========== 4. Grievance Categories (23 Total) ==========
INSERT INTO grievance_categories (category_id, category_code, display_name, icon_class, is_shared_utility, default_priority, monthly_threshold_limit) VALUES
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

-- ========== 5. Routing Rules ==========
INSERT INTO routing_rules (category_id, location_scope, target_dept_id) VALUES
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

-- ========== 6. SLA Policies ==========
INSERT INTO sla_policies (category_id, priority, ack_deadline_minutes, resolution_deadline_minutes) VALUES
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

-- ========== 7. Sample Master Incident ==========
INSERT INTO master_incidents (category_id, assigned_dept_id, building_block, incident_title, normalized_tokens, incident_status, affected_student_count) VALUES
(2, 2, 'Ganga Tower - Block A', 'Water supply cut off in Ganga Tower - Block A', 'water supply cut off ganga block a', 'OPEN', 3);

-- ========== 8. Sample Grievance Tickets ==========
-- Ticket 1: Individual AC complaint (Suraj = user_id 18)
INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
('SRM-2026-000001', 18, 1, 1, NULL, 'HOSTEL', 'Ganga Tower - Block A', 'G-412', 'My room AC is not cooling properly. Temperature stays at 30 degrees even after 2 hours.', 'MEDIUM', 'SUBMITTED',
 DATE_ADD(NOW(), INTERVAL 240 MINUTE), DATE_ADD(NOW(), INTERVAL 1440 MINUTE));

-- Ticket 2: Water outage linked to master incident (student 18 = Suraj)
INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
('SRM-2026-000002', 18, 2, 2, 1, 'HOSTEL', 'Ganga Tower - Block A', 'G-412', 'No water supply since morning in Ganga Tower Block A 4th floor.', 'HIGH', 'ACKNOWLEDGED',
 DATE_ADD(NOW(), INTERVAL 60 MINUTE), DATE_ADD(NOW(), INTERVAL 360 MINUTE));

-- Ticket 3: Water outage linked to same master incident (student 22 = Vikram, same building)
INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
('SRM-2026-000003', 22, 2, 2, 1, 'HOSTEL', 'Ganga Tower - Block A', 'G-415', 'Water not coming in my room since early morning.', 'HIGH', 'SUBMITTED',
 DATE_ADD(NOW(), INTERVAL 60 MINUTE), DATE_ADD(NOW(), INTERVAL 360 MINUTE));

-- Ticket 4: WiFi issue (student 19 = Ananya)
INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at) VALUES
('SRM-2026-000004', 19, 6, 5, NULL, 'HOSTEL', 'Yamuna Tower', 'Y-201', 'WiFi keeps disconnecting every 10 minutes. Cannot attend online classes.', 'HIGH', 'IN_PROGRESS',
 DATE_ADD(NOW(), INTERVAL 60 MINUTE), DATE_ADD(NOW(), INTERVAL 360 MINUTE));

-- Ticket 5: Mess food quality (student 20 = Rahul)
INSERT INTO grievance_tickets (ticket_reference, student_user_id, category_id, assigned_dept_id, master_incident_id, location_scope, building_block, room_or_location, raw_description, priority, status, ack_deadline_at, resolution_deadline_at, acknowledged_at, resolved_by_dept_at) VALUES
('SRM-2026-000005', 20, 4, 3, NULL, 'MESS_HALL', 'Annapoorna Mess Complex', 'Counter 3', 'Found hair in today lunch dal. Very unhygienic.', 'MEDIUM', 'AWAITING_STUDENT_CONFIRMATION',
 DATE_ADD(NOW(), INTERVAL 240 MINUTE), DATE_ADD(NOW(), INTERVAL 1440 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE));

-- ========== 9. Sample Status History ==========
INSERT INTO complaint_status_history (ticket_id, from_status, to_status, changed_by_user_id, action_note) VALUES
(2, 'SUBMITTED', 'ACKNOWLEDGED', 32, 'Assigned to water plumbing team for Ganga Tower - Block A'),
(4, 'SUBMITTED', 'ACKNOWLEDGED', 38, 'ITKM team investigating Yamuna Tower WiFi'),
(4, 'ACKNOWLEDGED', 'IN_PROGRESS', 38, 'Technician dispatched to check router'),
(5, 'SUBMITTED', 'ACKNOWLEDGED', 34, 'Mess committee notified about hygiene issue'),
(5, 'ACKNOWLEDGED', 'IN_PROGRESS', 34, 'Kitchen staff instructed on hygiene protocols'),
(5, 'IN_PROGRESS', 'AWAITING_STUDENT_CONFIRMATION', 34, 'Kitchen deep-cleaned and staff re-trained. Please confirm if resolved.');

-- ========== 10. Sample Ticket Messages ==========
INSERT INTO ticket_messages (ticket_id, sender_user_id, sender_role, message_body, is_system_alert) VALUES
(2, 32, 'ROLE_DEPT_STAFF', 'We have identified the issue with the water pump for Ganga Tower Block A. Repair team is on the way.', FALSE),
(2, 18, 'ROLE_STUDENT', 'Thank you for the quick response. How long will it take?', FALSE),
(2, 32, 'ROLE_DEPT_STAFF', 'Estimated 2 hours for repair. We will update you.', FALSE),
(4, 38, 'ROLE_DEPT_STAFF', 'We are checking the Yamuna Tower access point router configuration. Please try restarting your device once.', FALSE),
(4, 19, 'ROLE_STUDENT', 'I already tried restarting. The issue persists.', FALSE),
(5, 34, 'ROLE_DEPT_STAFF', 'We sincerely apologize for the hygiene issue. The kitchen has been deep cleaned and staff re-trained on protocols.', FALSE),
(5, 34, 'ROLE_DEPT_STAFF', 'We have marked this as resolved. Please confirm if you are satisfied with the resolution.', FALSE);
