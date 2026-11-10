package com.srmresolve.service;

import com.srmresolve.dto.IssuePredictionResult;
import com.srmresolve.model.LocationScope;
import com.srmresolve.model.Priority;
import com.srmresolve.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligent Campus Issue Classification, Entity Extraction & Automatic Routing Engine.
 * Enables zero-cognitive-overhead reporting: students describe any issue in natural language,
 * and the engine predicts the responsible department, category, priority, emergency status,
 * and location scope without guesswork.
 */
@Service
public class CampusIntelligenceEngine {

    private static final Logger log = LoggerFactory.getLogger(CampusIntelligenceEngine.class);

    // 1. Sexual / Online Harassment, Stalking, POSH & Cyber Harassment (ICC)
    private static final Pattern HARASSMENT_PATTERN = Pattern.compile(
            "\\b(harass|harassing|harassed|harassment|stalking|stalker|stalked|online harassment|boy is harassing|girl is harassing|blackmail|blackmailing|nude|nudes|private photo|leaked photo|creepy message|unwanted message|cyberbullying|cyber bullying|instagram|whatsapp|telegram|eve teasing|sexual harassment|posh|inappropriate touch|molestation|icc|threatened online|threatening me)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 2. Anti-Ragging & Coercion
    private static final Pattern RAGGING_PATTERN = Pattern.compile(
            "\\b(ragging|ragged|rag|seniors forcing|senior forced|compelled by senior|bullying|bullied|intro session|forced intro|senior harassment|calling to hostel room|coercion|anti ragging|antiragging)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 3. Mental Health, Depression, Counseling
    private static final Pattern COUNSELING_PATTERN = Pattern.compile(
            "\\b(counseling|counselor|depressed|depression|anxiety|panic attack|mental health|suicidal|hopeless|lonely|can't sleep|cannot sleep|stress|wellness centre|therapy|psychologist)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 4. Examinations, Grades, Hall Tickets & CoE
    private static final Pattern EXAM_PATTERN = Pattern.compile(
            "\\b(hall ticket|admit card|exam|examination|midsem|endsem|results|result|marksheet|grade|grades|gpa|cgpa|backlog|backlogs|revaluation|paper re-check|timetable conflict|coe|controller of examination)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 5. Tuition Fees, Scholarships & Finance
    private static final Pattern FINANCE_PATTERN = Pattern.compile(
            "\\b(fee|fees|tuition fee|caution deposit|security deposit|scholarship|receipt|fee receipt|double payment|deducted twice|payment failed|account refund|challan|mess fee|hostel fee)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 6. Student Affairs & Roommate Disputes
    private static final Pattern DISPUTES_PATTERN = Pattern.compile(
            "\\b(roommate|room mate|fighting|hostel dispute|curfew|warden dispute|student council|dsa|student affairs)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 7. Emergency Keywords: Wildlife / Snakes
    private static final Pattern WILDLIFE_PATTERN = Pattern.compile(
            "\\b(snake|reptile|viper|cobra|krait|wild animal|wild dog|dog bite|monkey|scorpion|centipede)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 8. Fire, Smoke & Gas Leaks
    private static final Pattern FIRE_HAZARD_PATTERN = Pattern.compile(
            "\\b(fire|smoke|flames|burning smell|gas leak|cylinder leak|explosion|sparking|short circuit spark)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 9. Medical Emergencies
    private static final Pattern MEDICAL_PATTERN = Pattern.compile(
            "\\b(medical|ambulance|faint|fainted|unconscious|bleeding|severe blood|chest pain|heart attack|fracture|seizure|breathing difficulty|asthma attack)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 10. Academic Labs & Instrumentation
    private static final Pattern LAB_INSTRUMENTATION_PATTERN = Pattern.compile(
            "\\b(voltmeter|ammeter|multimeter|oscilloscope|galvanometer|rheostat|breadboard|function generator|lab equipment|apparatus|bunsen burner|microscope|pipette|burette|spectrometer|centrifuge|chemistry lab|physics lab|electronics lab|mechanical lab|lab reading|erratic reading)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 11. Furniture & Carpentry
    private static final Pattern FURNITURE_PATTERN = Pattern.compile(
            "\\b(chair|desk|bench|table|cupboard|almirah|bed|cot|mattress|furniture|door lock|door handle|hinge|broken chair|broken table|window latch|broken glass|drawer)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 12. Horticulture, Roads & Grounds
    private static final Pattern HORTICULTURE_PATTERN = Pattern.compile(
            "\\b(leaves|leaf|fallen tree|tree branch|pothole|muddy road|debris on road|roadway|pathway|footpath|sidewalk|lawn|grass|garden|landscape|sprinkler on road)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 13. Laundry & Clothes
    private static final Pattern LAUNDRY_PATTERN = Pattern.compile(
            "\\b(clothes|cloth|clothing|washing|washed|laundry|dryer|ironing|dhobi|stained shirt|torn cloth|washing machine|detergent|wet clothes|laundry bag)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 14. Vendor Overpricing & Commercial Outlets
    private static final Pattern VENDOR_OVERPRICING_PATTERN = Pattern.compile(
            "\\b(vendor|overpricing|overpriced|mrp|charging extra|canteen price|tuck shop|stationery shop|cafe bill|higher than mrp|scam price|extra cash|overcharged)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 15. Water & Plumbing
    private static final Pattern WATER_DISPENSER_PATTERN = Pattern.compile(
            "\\b(dispenser|ro water|cold water|hot water|water cooler|filter water|drinking water)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern WATER_OUTAGE_PATTERN = Pattern.compile(
            "\\b(no water|water cut|water supply|tap dry|taps dry|pipe leak|burst pipe|drainage|flush broken|shower|washroom water|geyser cold|basin leak|sewage)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 16. AC & HVAC
    private static final Pattern AC_PATTERN = Pattern.compile(
            "\\b(ac|air conditioner|air conditioning|cooling|not cooling|ac remote|temperature high|ac leaking|ac noise)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 17. Electrical
    private static final Pattern ELECTRICAL_PATTERN = Pattern.compile(
            "\\b(power cut|no power|blackout|tube light|bulb fused|light not working|fan not spinning|fan slow|socket|switchboard|plug point|mcb trip|electric shock)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 18. Mess & Dining
    private static final Pattern MESS_FOOD_PATTERN = Pattern.compile(
            "\\b(mess food|dinner|lunch|breakfast|dal|rice|roti|curry|caterer|undercooked|hair in food|insect in food|spoiled food|stale food|mess plate|mess hygiene|annapoorna)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 19. Housekeeping
    private static final Pattern HOUSEKEEPING_PATTERN = Pattern.compile(
            "\\b(cleaning|dirty|dust|dustbin|garbage|trash|stink|smell|broom|mop|corridor cleaning|washroom cleaning|toilet dirty)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 20. IT & Network
    private static final Pattern WIFI_IT_PATTERN = Pattern.compile(
            "\\b(wifi|wi-fi|internet|lan|ethernet|slow net|disconnecting|router|portal down|student portal|biometric|rfid|network speed)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // 21. Classroom AV
    private static final Pattern CLASSROOM_AV_PATTERN = Pattern.compile(
            "\\b(projector|smart board|touch board|podium|speaker|mic|microphone|hdmi|display screen|classroom audio)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Predicts department, category, priority, emergency flag, and location from raw text.
     */
    public IssuePredictionResult predictIssue(String rawText, User student) {
        IssuePredictionResult result = new IssuePredictionResult();
        String text = (rawText != null) ? rawText.trim() : "";
        String lower = text.toLowerCase(Locale.ROOT);

        // 1. Detect Location Entity from text
        String detectedLocation = extractLocationFromText(lower);
        if (detectedLocation != null) {
            result.setBuildingBlock(detectedLocation);
        } else if (student != null && student.getBuildingBlock() != null && !student.getBuildingBlock().isBlank()) {
            result.setBuildingBlock(student.getBuildingBlock());
        } else {
            result.setBuildingBlock("Ganga Tower - Block A");
        }

        // Room entity or fallback
        String detectedRoom = extractRoomFromText(text);
        if (detectedRoom != null) {
            result.setRoomOrLocation(detectedRoom);
        } else if (student != null && student.getRoomOrZone() != null && !student.getRoomOrZone().isBlank()) {
            result.setRoomOrLocation(student.getRoomOrZone());
        } else {
            result.setRoomOrLocation("General Campus Zone");
        }

        // 2. Intelligent Category & Department Matching (Ordered by Safety, Urgency & Specificity)

        // A. Online Harassment, Cyber Stalking, POSH & Women Safety (Internal Complaints Committee - ICC)
        if (HARASSMENT_PATTERN.matcher(lower).find()) {
            result.setCategoryId(18);
            result.setCategoryCode("CYBER_HARASSMENT");
            result.setCategoryDisplayName("🚨 Online Harassment & Cyber Stalking (Confidential)");
            result.setCategoryIcon("bi-shield-shaded");
            result.setTargetDeptId(11);
            result.setTargetDeptCode("INTERNAL_COMPLAINTS_COMMITTEE");
            result.setTargetDeptName("Internal Complaints Committee (ICC) & Anti-Harassment Cell");
            result.setPriority(Priority.CRITICAL);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(true);
            result.setEmergencyActionNotice("🔒 CONFIDENTIAL SAFE REPORT: Internal Complaints Committee (ICC) & Women's Cell alerted under UGC/POSH regulations. Direct confidential support: +91-866-2447777 / icc@srmap.edu.in.");
            result.setAckMinutes(15);
            result.setResolutionMinutes(120);
            result.setConfidenceScore(0.99);
            result.setIntelligenceReasoning("Online harassment / safety grievance identified. Routed confidentially to the Internal Complaints Committee (ICC).");
            return result;
        }

        // B. Anti-Ragging & Coercion
        if (RAGGING_PATTERN.matcher(lower).find()) {
            result.setCategoryId(19);
            result.setCategoryCode("ANTI_RAGGING");
            result.setCategoryDisplayName("🚨 Ragging & Senior Intimidation (Zero-Tolerance)");
            result.setCategoryIcon("bi-shield-x");
            result.setTargetDeptId(12);
            result.setTargetDeptCode("ANTI_RAGGING_CELL");
            result.setTargetDeptName("Anti-Ragging Committee & Squad");
            result.setPriority(Priority.CRITICAL);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(true);
            result.setEmergencyActionNotice("🚨 ZERO-TOLERANCE ANTI-RAGGING ALERT: Anti-Ragging Committee & Squad alerted immediately. National Anti-Ragging Helpline: 1800-180-5522 / antiragging@srmap.edu.in.");
            result.setAckMinutes(15);
            result.setResolutionMinutes(60);
            result.setConfidenceScore(0.98);
            result.setIntelligenceReasoning("Ragging / senior harassment reported. Instant critical dispatch to Anti-Ragging Committee.");
            return result;
        }

        // C. Mental Health & Counseling
        if (COUNSELING_PATTERN.matcher(lower).find()) {
            result.setCategoryId(22);
            result.setCategoryCode("STUDENT_COUNSELING");
            result.setCategoryDisplayName("💙 Student Wellness & Confidential Counseling");
            result.setCategoryIcon("bi-heart-pulse");
            result.setTargetDeptId(14);
            result.setTargetDeptCode("HEALTH_COUNSELING");
            result.setTargetDeptName("Health Centre, Wellness & Student Counseling");
            result.setPriority(Priority.CRITICAL);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(true);
            result.setEmergencyActionNotice("💙 CONFIDENTIAL WELLNESS SUPPORT: Student Counseling Centre & Campus Wellness Lead notified. 24/7 Student Helpline: +91-866-2447788 / counseling@srmap.edu.in.");
            result.setAckMinutes(15);
            result.setResolutionMinutes(60);
            result.setConfidenceScore(0.96);
            result.setIntelligenceReasoning("Psychological wellness concern detected. Routed to Student Counseling Centre.");
            return result;
        }

        // D. Critical Emergency: Snake / Wildlife Threat
        if (WILDLIFE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(9);
            result.setCategoryCode("SNAKE_WILDLIFE_SOS");
            result.setCategoryDisplayName("🚨 Snake / Wildlife Threat (SOS Emergency)");
            result.setCategoryIcon("bi-shield-exclamation");
            result.setTargetDeptId(6);
            result.setTargetDeptCode("SECURITY_EMERGENCY");
            result.setTargetDeptName("Campus Security & Quick Response Emergency Team");
            result.setPriority(Priority.CRITICAL);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(true);
            result.setEmergencyActionNotice("🚨 ACTIVE WILDLIFE THREAT DETECTED: Rapid emergency dispatch alerted. Campus Security Control Room notified (+91-866-2447799).");
            result.setAckMinutes(15);
            result.setResolutionMinutes(60);
            result.setConfidenceScore(0.98);
            result.setIntelligenceReasoning("Wildlife/Snake hazard identified in prompt. Elevated to CRITICAL emergency status with 15-minute response SLA.");
            return result;
        }

        // E. Critical Emergency: Fire, Smoke, Gas Leak
        if (FIRE_HAZARD_PATTERN.matcher(lower).find()) {
            result.setCategoryId(10);
            result.setCategoryCode("FIRE_SAFETY_SOS");
            result.setCategoryDisplayName("🚨 Fire, Smoke & Gas Leak Hazard (SOS)");
            result.setCategoryIcon("bi-fire");
            result.setTargetDeptId(6);
            result.setTargetDeptCode("SECURITY_EMERGENCY");
            result.setTargetDeptName("Campus Security & Quick Response Emergency Team");
            result.setPriority(Priority.CRITICAL);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(true);
            result.setEmergencyActionNotice("🚨 FIRE / SMOKE / HAZARD DETECTED: Fire marshal & emergency quick response team dispatched immediately.");
            result.setAckMinutes(10);
            result.setResolutionMinutes(30);
            result.setConfidenceScore(0.97);
            result.setIntelligenceReasoning("Life-safety fire/smoke trigger recognized. Immediate priority routing to Security & Emergency Services.");
            return result;
        }

        // F. Critical Emergency: Medical Distress / Ambulance
        if (MEDICAL_PATTERN.matcher(lower).find()) {
            result.setCategoryId(11);
            result.setCategoryCode("MEDICAL_EMERGENCY_SOS");
            result.setCategoryDisplayName("🚑 Medical Emergency / Health Centre (SOS)");
            result.setCategoryIcon("bi-heart-pulse-fill");
            result.setTargetDeptId(6);
            result.setTargetDeptCode("SECURITY_EMERGENCY");
            result.setTargetDeptName("Campus Security & 24/7 Medical Response");
            result.setPriority(Priority.CRITICAL);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(true);
            result.setEmergencyActionNotice("🚑 MEDICAL CRISIS DETECTED: 24/7 Campus Health Centre & Ambulance notified (+91-866-2447788).");
            result.setAckMinutes(10);
            result.setResolutionMinutes(30);
            result.setConfidenceScore(0.96);
            result.setIntelligenceReasoning("Medical distress event detected. Directed to Campus Medical Emergency team.");
            return result;
        }

        // G. Examinations, Results & Grade Evaluation (CoE)
        if (EXAM_PATTERN.matcher(lower).find()) {
            result.setCategoryId(20);
            result.setCategoryCode("EXAM_EVALUATION");
            result.setCategoryDisplayName("📝 Examinations, Results & Grade Evaluation");
            result.setCategoryIcon("bi-journal-check");
            result.setTargetDeptId(15);
            result.setTargetDeptCode("EXAMINATION_COE");
            result.setTargetDeptName("Office of Controller of Examinations (CoE)");
            result.setPriority(Priority.HIGH);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(false);
            result.setAckMinutes(120);
            result.setResolutionMinutes(720);
            result.setConfidenceScore(0.95);
            result.setIntelligenceReasoning("Examination / grading matter identified. Routed to Controller of Examinations.");
            return result;
        }

        // H. Tuition Fees, Scholarships & Finance
        if (FINANCE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(21);
            result.setCategoryCode("FEE_FINANCE");
            result.setCategoryDisplayName("💳 Tuition Fees, Scholarship & Finance Office");
            result.setCategoryIcon("bi-cash-coin");
            result.setTargetDeptId(16);
            result.setTargetDeptCode("FINANCE_ACCOUNTS");
            result.setTargetDeptName("Directorate of Finance & Accounts");
            result.setPriority(Priority.HIGH);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(false);
            result.setAckMinutes(120);
            result.setResolutionMinutes(720);
            result.setConfidenceScore(0.94);
            result.setIntelligenceReasoning("Financial / fee reconciliation matter identified. Assigned to Finance & Accounts Directorate.");
            return result;
        }

        // I. Student Affairs & Roommate Disputes
        if (DISPUTES_PATTERN.matcher(lower).find()) {
            result.setCategoryId(23);
            result.setCategoryCode("STUDENT_DISPUTES");
            result.setCategoryDisplayName("🤝 Student Affairs & Hostel Mediation");
            result.setCategoryIcon("bi-people");
            result.setTargetDeptId(13);
            result.setTargetDeptCode("STUDENT_AFFAIRS_SGRC");
            result.setTargetDeptName("Directorate of Student Affairs (DSA) & SGRC");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.92);
            result.setIntelligenceReasoning("Hostel roommate / student affairs dispute identified. Routed to Directorate of Student Affairs.");
            return result;
        }

        // J. Academic Labs & Instrumentation (e.g. Voltmeter not working)
        if (LAB_INSTRUMENTATION_PATTERN.matcher(lower).find()) {
            result.setCategoryId(12);
            result.setCategoryCode("LAB_INSTRUMENTATION");
            result.setCategoryDisplayName("🔬 Academic Lab Equipment & Voltmeter");
            result.setCategoryIcon("bi-cpu");
            result.setTargetDeptId(7);
            result.setTargetDeptCode("ACADEMIC_LABS");
            result.setTargetDeptName("Academic Labs & Scientific Instrumentation");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.ACADEMIC_BLOCK);
            result.setEmergency(false);
            result.setAckMinutes(120);
            result.setResolutionMinutes(720);
            result.setConfidenceScore(0.94);
            result.setIntelligenceReasoning("Engineering/Science laboratory apparatus malfunction detected. Routed directly to Academic Labs & Instrumentation team.");
            return result;
        }

        // K. Commercial Outlets, Canteen & Vendor Overpricing
        if (VENDOR_OVERPRICING_PATTERN.matcher(lower).find()) {
            result.setCategoryId(16);
            result.setCategoryCode("VENDOR_OVERPRICING");
            result.setCategoryDisplayName("🏷️ Canteen / Vendor Overpricing & MRP Violation");
            result.setCategoryIcon("bi-shop");
            result.setTargetDeptId(10);
            result.setTargetDeptCode("COMMERCIAL_WELFARE");
            result.setTargetDeptName("Commercial Outlets & Student Welfare Committee");
            result.setPriority(Priority.HIGH);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(false);
            result.setAckMinutes(120);
            result.setResolutionMinutes(720);
            result.setConfidenceScore(0.93);
            result.setIntelligenceReasoning("Campus commercial outlet / MRP violation reported. Assigned to Student Welfare & Commercial Oversight Committee.");
            return result;
        }

        // L. Laundry Services & Clothes
        if (LAUNDRY_PATTERN.matcher(lower).find()) {
            result.setCategoryId(15);
            result.setCategoryCode("LAUNDRY_CLOTHES");
            result.setCategoryDisplayName("🧺 Hostel Laundry & Clothes Washing");
            result.setCategoryIcon("bi-basket2");
            result.setTargetDeptId(9);
            result.setTargetDeptCode("LAUNDRY_SERVICES");
            result.setTargetDeptName("Campus Laundry & Student Care");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.92);
            result.setIntelligenceReasoning("Hostel laundry service issue identified. Routed to Campus Laundry Operations.");
            return result;
        }

        // M. Horticulture, Campus Roads & Grounds (e.g. Leaves on road)
        if (HORTICULTURE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(14);
            result.setCategoryCode("HORTICULTURE_ROADS");
            result.setCategoryDisplayName("🍃 Campus Roads, Leaves & Grounds Sanitation");
            result.setCategoryIcon("bi-tree");
            result.setTargetDeptId(8);
            result.setTargetDeptCode("HORTICULTURE_ESTATE");
            result.setTargetDeptName("Campus Horticulture, Roads & Grounds");
            result.setPriority(Priority.LOW);
            result.setLocationScope(LocationScope.ANY);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.91);
            result.setIntelligenceReasoning("Campus estate/roadway grounds maintenance requested. Routed to Horticulture & Estate operations.");
            return result;
        }

        // N. Broken Chair, Furniture & Carpentry
        if (FURNITURE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(13);
            result.setCategoryCode("FURNITURE_CARPENTRY");
            result.setCategoryDisplayName("🪑 Broken Chair, Desk & Furniture");
            result.setCategoryIcon("bi-inboxes");
            result.setTargetDeptId(1);
            result.setTargetDeptCode("HOSTEL_MAINT");
            result.setTargetDeptName("Campus Life & Maintenance (CLM)");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.90);
            result.setIntelligenceReasoning("Carpentry / furniture repair detected. Assigned to Campus Life & Maintenance (CLM).");
            return result;
        }

        // O. Water Dispenser (Hot/Cold)
        if (WATER_DISPENSER_PATTERN.matcher(lower).find()) {
            result.setCategoryId(3);
            result.setCategoryCode("HOT_COLD_WATER");
            result.setCategoryDisplayName("Hot/Cold Water Dispenser");
            result.setCategoryIcon("bi-cup-hot");
            result.setTargetDeptId(2);
            result.setTargetDeptCode("WATER_PLUMBING");
            result.setTargetDeptName("Water, RO Plant & Plumbing Services");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.89);
            result.setIntelligenceReasoning("Water dispenser / RO filtration issue detected. Assigned to Water & Plumbing Services.");
            return result;
        }

        // P. Water Outage / Plumbing Leaks
        if (WATER_OUTAGE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(2);
            result.setCategoryCode("WATER_OUTAGE");
            result.setCategoryDisplayName("Water Supply Cut");
            result.setCategoryIcon("bi-droplet-half");
            result.setTargetDeptId(2);
            result.setTargetDeptCode("WATER_PLUMBING");
            result.setTargetDeptName("Water, RO Plant & Plumbing Services");
            result.setPriority(Priority.HIGH);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(60);
            result.setResolutionMinutes(360);
            result.setConfidenceScore(0.92);
            result.setIntelligenceReasoning("Water supply disruption / plumbing defect detected. Routed to Water & Plumbing team.");
            return result;
        }

        // Q. Air Conditioning
        if (AC_PATTERN.matcher(lower).find()) {
            result.setCategoryId(1);
            result.setCategoryCode("AC_COOLING");
            result.setCategoryDisplayName("AC Not Cooling");
            result.setCategoryIcon("bi-thermometer-snow");
            result.setTargetDeptId(1);
            result.setTargetDeptCode("HOSTEL_MAINT");
            result.setTargetDeptName("Campus Life & Maintenance (CLM)");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.91);
            result.setIntelligenceReasoning("Air conditioning / HVAC issue detected. Assigned to Campus Life & Maintenance.");
            return result;
        }

        // R. Electrical Repair
        if (ELECTRICAL_PATTERN.matcher(lower).find()) {
            result.setCategoryId(7);
            result.setCategoryCode("ELECTRICAL_REPAIR");
            result.setCategoryDisplayName("Electrical Repair");
            result.setCategoryIcon("bi-lightning-charge");
            result.setTargetDeptId(1);
            result.setTargetDeptCode("HOSTEL_MAINT");
            result.setTargetDeptName("Campus Life & Maintenance (CLM)");
            result.setPriority(Priority.HIGH);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(60);
            result.setResolutionMinutes(360);
            result.setConfidenceScore(0.90);
            result.setIntelligenceReasoning("Electrical power / fixture failure identified. Routed to Electrical & Facilities maintenance.");
            return result;
        }

        // S. Mess Food Quality
        if (MESS_FOOD_PATTERN.matcher(lower).find()) {
            result.setCategoryId(4);
            result.setCategoryCode("MESS_FOOD_QUALITY");
            result.setCategoryDisplayName("Mess Food Quality");
            result.setCategoryIcon("bi-egg-fried");
            result.setTargetDeptId(3);
            result.setTargetDeptCode("MESS_COMMITTEE");
            result.setTargetDeptName("Annapoorna Mess & Dining Committee");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.MESS_HALL);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.91);
            result.setIntelligenceReasoning("Mess dining or food preparation concern detected. Assigned to Annapoorna Mess Committee.");
            return result;
        }

        // T. Wi-Fi & ITKM
        if (WIFI_IT_PATTERN.matcher(lower).find()) {
            result.setCategoryId(6);
            result.setCategoryCode("WIFI_NETWORK");
            result.setCategoryDisplayName("Wi-Fi / Network Issue");
            result.setCategoryIcon("bi-wifi-off");
            result.setTargetDeptId(5);
            result.setTargetDeptCode("ITKM_HELPDESK");
            result.setTargetDeptName("IT & Knowledge Management (ITKM) Helpdesk");
            result.setPriority(Priority.HIGH);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(60);
            result.setResolutionMinutes(360);
            result.setConfidenceScore(0.92);
            result.setIntelligenceReasoning("Network connectivity or IT infrastructure issue detected. Assigned to ITKM Helpdesk.");
            return result;
        }

        // U. Classroom AV
        if (CLASSROOM_AV_PATTERN.matcher(lower).find()) {
            result.setCategoryId(8);
            result.setCategoryCode("CLASSROOM_AV");
            result.setCategoryDisplayName("Classroom AV Equipment");
            result.setCategoryIcon("bi-display");
            result.setTargetDeptId(5);
            result.setTargetDeptCode("ITKM_HELPDESK");
            result.setTargetDeptName("IT & Knowledge Management (ITKM) Helpdesk");
            result.setPriority(Priority.MEDIUM);
            result.setLocationScope(LocationScope.ACADEMIC_BLOCK);
            result.setEmergency(false);
            result.setAckMinutes(240);
            result.setResolutionMinutes(1440);
            result.setConfidenceScore(0.89);
            result.setIntelligenceReasoning("Classroom multimedia/AV equipment issue recognized. Assigned to ITKM Audio-Visual team.");
            return result;
        }

        // V. Housekeeping & Sanitation
        if (HOUSEKEEPING_PATTERN.matcher(lower).find()) {
            result.setCategoryId(5);
            result.setCategoryCode("ROOM_CLEANING");
            result.setCategoryDisplayName("Room Cleaning");
            result.setCategoryIcon("bi-house-heart");
            result.setTargetDeptId(4);
            result.setTargetDeptCode("HOUSEKEEPING");
            result.setTargetDeptName("Housekeeping & Campus Sanitation");
            result.setPriority(Priority.LOW);
            result.setLocationScope(LocationScope.HOSTEL);
            result.setEmergency(false);
            result.setAckMinutes(480);
            result.setResolutionMinutes(2880);
            result.setConfidenceScore(0.88);
            result.setIntelligenceReasoning("Sanitation / cleanliness service required. Assigned to Housekeeping & Sanitation staff.");
            return result;
        }

        // W. General Campus Fallback
        result.setCategoryId(17);
        result.setCategoryCode("GENERAL_CAMPUS_ISSUE");
        result.setCategoryDisplayName("General Campus Infrastructure Concern");
        result.setCategoryIcon("bi-chat-square-text");
        result.setTargetDeptId(1);
        result.setTargetDeptCode("HOSTEL_MAINT");
        result.setTargetDeptName("Campus Life & Maintenance (CLM)");
        result.setPriority(Priority.MEDIUM);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(false);
        result.setAckMinutes(240);
        result.setResolutionMinutes(1440);
        result.setConfidenceScore(0.70);
        result.setIntelligenceReasoning("General infrastructure or amenity concern logged. Routed to Campus Life & Maintenance for assessment.");
        return result;
    }

    /**
     * Extracts named campus building or block from complaint text.
     */
    private String extractLocationFromText(String lower) {
        if (lower.contains("sr block") || lower.contains("sr academic") || lower.contains("jc bose") || lower.contains("j.c. bose") || lower.contains("bose block")) {
            return "Sir J.C. Bose Academic Block";
        }
        if (lower.contains("central library") || lower.contains("library") || lower.contains("dlrc")) {
            return "Central Library (Digital Learning Centre)";
        }
        if (lower.contains("annapoorna") || lower.contains("mess complex") || lower.contains("dining hall")) {
            return "Annapoorna Mess Complex";
        }
        if (lower.contains("north avenue") || lower.contains("cafe") || lower.contains("canteen") || lower.contains("food court")) {
            return "North Avenue Café / Food Court";
        }
        if (lower.contains("ganga a") || lower.contains("ganga block a") || lower.contains("ganga-a")) {
            return "Ganga Tower - Block A";
        }
        if (lower.contains("ganga b") || lower.contains("ganga block b") || lower.contains("ganga-b")) {
            return "Ganga Tower - Block B";
        }
        if (lower.contains("ganga")) {
            return "Ganga Tower - Block A";
        }
        if (lower.contains("yamuna")) {
            return "Yamuna Tower";
        }
        if (lower.contains("krishna")) {
            return "Krishna Tower";
        }
        if (lower.contains("vedavathi")) {
            return "Vedavathi Tower";
        }
        if (lower.contains("kaveri")) {
            return "Kaveri Tower";
        }
        if (lower.contains("godavari")) {
            return "Godavari Tower";
        }
        if (lower.contains("narmada")) {
            return "Narmada Tower";
        }
        if (lower.contains("admin block") || lower.contains("vc office")) {
            return "Admin Block";
        }
        if (lower.contains("sports") || lower.contains("gym") || lower.contains("ground")) {
            return "University Sports Complex & Gym";
        }
        if (lower.contains("road") || lower.contains("pathway") || lower.contains("street")) {
            return "Campus Main Roadway & Grounds";
        }
        if (lower.contains("lab")) {
            return "Sir J.C. Bose Academic Block (Labs)";
        }
        return null;
    }

    /**
     * Extracts room or floor designation if typed in natural language.
     */
    private String extractRoomFromText(String text) {
        Pattern roomPattern = Pattern.compile("(?i)(?:room|lab|hall|cabin|zone|counter|floor)\\s*#?\\s*([A-Za-z0-9\\-]+)");
        Matcher m = roomPattern.matcher(text);
        if (m.find()) {
            return m.group(0).trim();
        }
        return null;
    }
}
