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
 * Implements context-aware, explainable Java classification:
 * - Stage 1: Text normalization & location entity extraction (distinguishes location context from problem intent).
 * - Stage 2: Non-incident, hypothetical, classroom, or historical mention detection.
 * - Stage 3: Specific phrase, context, and multi-intent disambiguation.
 * - Stage 4: Prioritized domain routing (IDs, Library, Transport, CRCS, CoE, Finance, Safety, Utilities).
 * - Stage 5: Explainable uncertainty fallback (Student Affairs triage; never silently defaults to CLM).
 */
@Service
public class CampusIntelligenceEngine {

    private static final Logger log = LoggerFactory.getLogger(CampusIntelligenceEngine.class);

    // Official SRM-AP Verified Emergency Contact Numbers
    public static final String HEALTH_CENTRE_24X7 = "0863-2343052";
    public static final String STUDENT_AFFAIRS_HELPLINE = "9154947156";
    public static final String SECURITY_CONTROL_ROOM = "0863-2343000";
    public static final String NATIONAL_ANTI_RAGGING_HELPLINE = "1800-180-5522";

    // Non-Incident / Academic / Historical / Hypothetical Patterns
    private static final Pattern NON_INCIDENT_PATTERN = Pattern.compile(
            "\\b(studied.*in (biology|class|lecture)|video of a (snake|lion)|(movie|documentary) (about|on)|watched a (movie|documentary)|in a (movie|documentary)|" +
            "writing a story about|poster is hanging|attended a (medical|fire|safety).*lecture|" +
            "attended a (medical|fire|safety).*seminar|attended a.*orientation|" +
            "discussed.*in.*(class|lecture|orientation)|lecture (about|on).*(snake|venom|lion|fire))\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Context Patterns
    private static final Pattern LOST_ID_PATTERN = Pattern.compile(
            "\\b(lost (my |a )?(student )?id card|id card (won'?t|does not|cannot) scan|" +
            "lost my id\\b|replacement id card|damaged id card|id card replacement|smart card not working)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HARASSMENT_SENSITIVE_PATTERN = Pattern.compile(
            "\\b(blackmail|blackmailing|private photo|leaked photo|private photos|nude|nudes|stalking|stalker|" +
            "threatened online|threatening me online|without consent|creepy message|unwanted message|cyberbullying|eve teasing|sexual harassment|posh|molestation)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NETWORK_CONNECTIVITY_PATTERN = Pattern.compile(
            "\\b(connect to (instagram|whatsapp|telegram|facebook|portal)|accessing (whatsapp|instagram|portal)|" +
            "wi-?fi|internet|network|disconnecting|router|slow internet|no internet|hotspot|speed test|dns)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ACTIVE_WILDLIFE_PATTERN = Pattern.compile(
            "\\b(snake|cobra|viper|krait|reptile|lion|leopard|wild dog|scorpion|centipede)\\b.*\\b(moving|roaming|crawling|spotted|near|gate|entrance|hostel|mess|room|campus|road|outside|right now)\\b|" +
            "\\b(moving|roaming|crawling|spotted|near|outside|right now)\\b.*\\b(snake|cobra|viper|krait|reptile|lion|leopard|wild dog|scorpion)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ACTIVE_FIRE_PATTERN = Pattern.compile(
            "\\b(smoke is coming|flames|fire in|burning smell|gas leak|cylinder leak|short circuit spark)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MEDICAL_EMERGENCY_PATTERN = Pattern.compile(
            "\\b(unconscious|collapsed|fainted|severe bleeding|bleeding heavily|chest pain|heart attack|seizure|breathing difficulty|asthma attack)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ROUTINE_MEDICAL_PATTERN = Pattern.compile(
            "\\b(sick|fever|cough|cold|headache|campus clinic|visit the clinic|health centre|consult doctor|prescription|medical attention)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RAGGING_PATTERN = Pattern.compile(
            "\\b(ragging|ragged|senior(s)? forcing|forced by senior|forced intro|humiliating intro|intro session|coercion by senior|called to senior room)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FINANCE_PATTERN = Pattern.compile(
            "\\b(scholarship( was credited| amount| not arrived)?|tuition fee|fee payment|deducted twice|fee was deducted|" +
            "payment failed|double payment|need a fee receipt|refund of caution|fee receipt for my payment)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EXAM_COE_PATTERN = Pattern.compile(
            "\\b(cgpa|gpa|marksheet|grade(s)? (is|are) (wrong|incorrect)|midsem|endsem|hall ticket|revaluation|results were released|exam evaluation|backlog paper)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ACADEMIC_AFFAIRS_PATTERN = Pattern.compile(
            "\\b(attendance marked absent|attendance|professor has not uploaded|course materials|lecturer.*clash.*timetable|timetable clash|faculty grievance)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CRCS_PLACEMENT_PATTERN = Pattern.compile(
            "\\b(placement interview|crcs|internship approval|placement slot|placement drive|campus placement)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TRANSPORT_PATTERN = Pattern.compile(
            "\\b(university bus|college bus|bus pickup|bus timetable|bus route|bus broke down|commuter bus)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LIBRARY_PATTERN = Pattern.compile(
            "\\b(library book|books i need are missing|marked returned even though|central library books|library fine|borrowed book)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern WATER_OUTAGE_PATTERN = Pattern.compile(
            "\\b(water hasn't come|no water|water supply|washroom tap|bathroom water|pipe leaking|water cut)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern WATER_COOLER_PATTERN = Pattern.compile(
            "\\b(water cooler|water dispenser|drinking water|cold water dispenser|hot water dispenser|ro water)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern AC_COOLING_PATTERN = Pattern.compile(
            "\\b(room ac|air conditioner|ac not cooling|ac makes a loud noise|ac remote|temperature stays at|ac dripping)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MESS_FOOD_PATTERN = Pattern.compile(
            "\\b(mess food|food quality|insects? in.*(mess|food|dal)|stale (food|dal|curry)|spoiled food|mess hygiene|hair in.*food|uncooked rice)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HOUSEKEEPING_PATTERN = Pattern.compile(
            "\\b(toilet.*(smells|filthy|dirty|choked)|room cleaning|washroom (dirty|cleaning)|corridor cleaning|dustbin full)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ELECTRICAL_PATTERN = Pattern.compile(
            "\\b(electrical shock|shock from|sparking switch|power cut in room|light not working|fan not working|switchboard)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern FURNITURE_PATTERN = Pattern.compile(
            "\\b(broken bed|bed.*dangerous|broken chair|broken table|cupboard|almirah|door lock|window latch|broken glass)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LAB_INSTRUMENTATION_PATTERN = Pattern.compile(
            "\\b(voltmeter|ammeter|multimeter|oscilloscope|breadboard|function generator|lab apparatus|rheostat)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CLASSROOM_AV_PATTERN = Pattern.compile(
            "\\b(projector in (lecture hall|classroom)|smart board|podium mic|classroom audio|hdmi cable in classroom)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LAUNDRY_PATTERN = Pattern.compile(
            "\\b(laundry shirt|washing machine|dryer|dhobi|hostel laundry|laundry clothes)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COMMERCIAL_PATTERN = Pattern.compile(
            "\\b(vendor charged more than.*mrp|canteen overpricing|stationery overpricing|canteen vendor)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HORTICULTURE_PATTERN = Pattern.compile(
            "\\b(pothole|campus road|fallen tree|leaves on road|muddy pathway)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Primary entry point: analyzes student input text with contextual intelligence.
     */
    public IssuePredictionResult predictIssue(String rawText, User student) {
        IssuePredictionResult result = new IssuePredictionResult();
        String text = (rawText != null) ? rawText.trim() : "";
        String lower = text.toLowerCase(Locale.ROOT);

        // 1. Entity Extraction: Location Context
        String detectedLocation = extractLocationFromText(lower);
        if (detectedLocation != null) {
            result.setBuildingBlock(detectedLocation);
        } else if (student != null && student.getBuildingBlock() != null && !student.getBuildingBlock().isBlank()) {
            result.setBuildingBlock(student.getBuildingBlock());
        } else {
            result.setBuildingBlock("Campus General Premises");
        }

        String detectedRoom = extractRoomFromText(text);
        if (detectedRoom != null) {
            result.setRoomOrLocation(detectedRoom);
        } else if (student != null && student.getRoomOrZone() != null && !student.getRoomOrZone().isBlank()) {
            result.setRoomOrLocation(student.getRoomOrZone());
        } else {
            result.setRoomOrLocation("General Area");
        }

        // Empty or whitespace-only input
        if (lower.isBlank()) {
            setAmbiguousFallback(result, "Please type a brief description of the issue to see suggested routing.");
            return result;
        }

        // 2. Stage 2: Non-Incident / Academic / Hypothetical Mentions
        if (NON_INCIDENT_PATTERN.matcher(lower).find()) {
            result.setNoIncident(true);
            result.setReviewNeeded(true);
            result.setEmergency(false);
            result.setConfidenceScore(0.0);
            result.setIntelligenceReasoning("Informational or academic context detected. No active campus grievance identified.");
            setTriageFallbackCategory(result);
            return result;
        }

        // 3. Stage 3: Specific Multi-Intent & Context Disambiguation

        // Case A: Network vs Harassment on Social Apps (Instagram/WhatsApp)
        if (lower.contains("instagram") || lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("portal")) {
            if (HARASSMENT_SENSITIVE_PATTERN.matcher(lower).find()) {
                return buildCyberHarassmentResult(result, "Sensitive online report detected. Routed confidentially to the Internal Complaints Committee (ICC).");
            }
            if (lower.contains("connect") || lower.contains("wi-fi") || lower.contains("wifi") ||
                lower.contains("internet") || lower.contains("accessing") || lower.contains("disconnect") || lower.contains("loading")) {
                return buildWifiResult(result, "Campus network connectivity issue detected. Assigned to ITKM Helpdesk.");
            }
        }

        // Case B: Harassment / Extortion / Private photo without app mention
        if (HARASSMENT_SENSITIVE_PATTERN.matcher(lower).find()) {
            return buildCyberHarassmentResult(result, "Sensitive personal safety report identified. Routed confidentially to ICC / Women's Cell.");
        }

        // Case C: Anti-Ragging
        if (RAGGING_PATTERN.matcher(lower).find()) {
            return buildAntiRaggingResult(result, "Zero-tolerance senior intimidation/ragging reported. Prioritized for Anti-Ragging Squad.");
        }

        // Case D: Threatening Roommate vs Routine Roommate dispute
        if (lower.contains("roommate") || lower.contains("room mate")) {
            if (lower.contains("threaten") || lower.contains("hurt") || lower.contains("harm") || lower.contains("violence")) {
                return buildUrgentDisputeResult(result, "Interpersonal safety threat reported. Escalated immediately to Student Affairs & Warden.");
            }
            return buildRoutineDisputeResult(result, "Roommate/hostel living mediation request. Assigned to Student Affairs & Hostel Office.");
        }
        if (lower.contains("hostel bullying") || lower.contains("repeated hostel bullying")) {
            return buildUrgentDisputeResult(result, "Hostel bullying reported. Escalated for Student Affairs and Anti-Ragging intervention.");
        }

        // Case E: Wildlife Safety (Active Danger vs General Reference)
        if (lower.contains("snake") || lower.contains("cobra") || lower.contains("viper") || lower.contains("lion") || lower.contains("leopard")) {
            if (ACTIVE_WILDLIFE_PATTERN.matcher(lower).find() || lower.contains("right now") || lower.contains("moving") || lower.contains("roaming")) {
                return buildActiveWildlifeResult(result, "Active dangerous wildlife reported on campus. Emergency Security team alerted.");
            }
            // General or vague reference e.g., "I saw a lion."
            result.setCategoryId(9);
            result.setCategoryCode("SNAKE_WILDLIFE_SOS");
            result.setCategoryDisplayName("Snake / Wildlife Threat (SOS)");
            result.setCategoryIcon("bi-shield-exclamation");
            result.setTargetDeptId(6);
            result.setTargetDeptCode("SECURITY_EMERGENCY");
            result.setTargetDeptName("Campus Security & Quick Response Emergency Team");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false); // Not automatically critical emergency without active location/danger confirmation
            result.setReviewNeeded(true);
            result.setEmergencyActionNotice("⚠️ WILDLIFE CAUTION: If you are in immediate danger or see active wildlife, call Security immediately: " + SECURITY_CONTROL_ROOM);
            result.setIntelligenceReasoning("Wildlife mention without confirmed active location. Please verify location details.");
            return result;
        }
        if (lower.contains("heard an animal") || (lower.contains("animal") && lower.contains("couldn't see"))) {
            result.setCategoryId(9);
            result.setCategoryCode("SNAKE_WILDLIFE_SOS");
            result.setCategoryDisplayName("Snake / Wildlife Threat (SOS)");
            result.setCategoryIcon("bi-shield-exclamation");
            result.setTargetDeptId(6);
            result.setTargetDeptCode("SECURITY_EMERGENCY");
            result.setTargetDeptName("Campus Security & Quick Response Emergency Team");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setReviewNeeded(true);
            result.setIntelligenceReasoning("Unverified animal noise reported. Campus Security alerted for inspection.");
            return result;
        }

        // Case F: Fire / Smoke / Electrical Hazard
        if (ACTIVE_FIRE_PATTERN.matcher(lower).find()) {
            return buildActiveFireResult(result, "Active smoke / fire hazard reported. Prioritized for Emergency Security Response.");
        }
        if (lower.contains("fire extinguisher") || lower.contains("fire alarm")) {
            result.setCategoryId(10);
            result.setCategoryCode("FIRE_SAFETY_SOS");
            result.setCategoryDisplayName("Fire, Smoke & Gas Leak (SOS)");
            result.setCategoryIcon("bi-fire");
            result.setTargetDeptId(6);
            result.setTargetDeptCode("SECURITY_EMERGENCY");
            result.setTargetDeptName("Campus Security & Quick Response Emergency Team");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Fire safety equipment maintenance logged. Assigned to Security & Safety.");
            return result;
        }

        // Case G: Electrical Shock (Immediate Safety Hazard) vs Routine Electrical
        if (lower.contains("shock") || lower.contains("electrical shock")) {
            result.setCategoryId(7);
            result.setCategoryCode("ELECTRICAL_REPAIR");
            result.setCategoryDisplayName("Electrical Repair");
            result.setCategoryIcon("bi-lightning-charge");
            result.setTargetDeptId(1);
            result.setTargetDeptCode("HOSTEL_MAINT");
            result.setTargetDeptName("Campus Life & Maintenance (CLM)");
            result.setPriority(Priority.CRITICAL);
            result.setEmergency(true);
            result.setEmergencyActionNotice("⚡ ELECTRICAL HAZARD ALERT: Do not touch the switch/appliance. Electrician emergency team notified.");
            result.setIntelligenceReasoning("Electric shock hazard detected. Immediate priority dispatch requested.");
            return result;
        }

        // Case H: Medical (Emergency vs Routine Support)
        if (MEDICAL_EMERGENCY_PATTERN.matcher(lower).find()) {
            return buildMedicalEmergencyResult(result, "Critical medical situation reported. Health Centre emergency line: " + HEALTH_CENTRE_24X7);
        }
        if (lower.contains("medical leave") || lower.contains("leave for a minor fever") || lower.contains("fever leave")) {
            result.setCategoryId(25);
            result.setCategoryCode("ROUTINE_MEDICAL_SUPPORT");
            result.setCategoryDisplayName("Routine Health Centre & Clinic Consultation");
            result.setCategoryIcon("bi-capsule");
            result.setTargetDeptId(14);
            result.setTargetDeptCode("HEALTH_COUNSELING");
            result.setTargetDeptName("Health Centre, Wellness & Student Counseling");
            result.setPriority(Priority.LOW);
            result.setEmergency(false);
            result.setReviewNeeded(true);
            result.setIntelligenceReasoning("Medical leave / minor consultation requested. Routed to Health Centre.");
            return result;
        }
        if (ROUTINE_MEDICAL_PATTERN.matcher(lower).find()) {
            result.setCategoryId(25);
            result.setCategoryCode("ROUTINE_MEDICAL_SUPPORT");
            result.setCategoryDisplayName("Routine Health Centre & Clinic Consultation");
            result.setCategoryIcon("bi-capsule");
            result.setTargetDeptId(14);
            result.setTargetDeptCode("HEALTH_COUNSELING");
            result.setTargetDeptName("Health Centre, Wellness & Student Counseling");
            result.setPriority(Priority.LOW);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Routine healthcare clinic visit requested. Health Centre: " + HEALTH_CENTRE_24X7);
            return result;
        }

        // Case I: ID Card Lost / Replacement / RFID Scan
        if (LOST_ID_PATTERN.matcher(lower).find() || (lower.contains("lost") && lower.contains("id card"))) {
            result.setCategoryId(24);
            result.setCategoryCode("LOST_OR_DAMAGED_ID_CARD");
            result.setCategoryDisplayName("Lost / Replacement Student ID Card");
            result.setCategoryIcon("bi-person-badge");
            result.setTargetDeptId(5);
            result.setTargetDeptCode("ITKM_HELPDESK");
            result.setTargetDeptName("IT & Knowledge Management (ITKM) Helpdesk");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Student ID card replacement / RFID scanning issue identified. Routed to ITKM.");
            return result;
        }

        // Case J: Location as Context vs Core Intent Disambiguation
        // "toilet near the library smells" -> Housekeeping (Library is location)
        if ((lower.contains("toilet") || lower.contains("washroom")) && (lower.contains("smell") || lower.contains("filthy") || lower.contains("dirty"))) {
            return buildHousekeepingResult(result, "Campus sanitation service requested. Assigned to Housekeeping.");
        }
        // "library internet keeps disconnecting" -> Wi-Fi (Library is location)
        if (lower.contains("library") && (lower.contains("internet") || lower.contains("wi-fi") || lower.contains("wifi") || lower.contains("disconnect"))) {
            return buildWifiResult(result, "Central Library Wi-Fi connection issue recognized. Assigned to ITKM Helpdesk.");
        }
        // "wi-fi in examination block" -> Wi-Fi (Exam block is location)
        if (lower.contains("examination block") && (lower.contains("wi-fi") || lower.contains("wifi") || lower.contains("internet"))) {
            return buildWifiResult(result, "Examination block Wi-Fi issue recognized. Assigned to ITKM Helpdesk.");
        }

        // Case K: Central Library Books & Materials
        if (LIBRARY_PATTERN.matcher(lower).find()) {
            result.setCategoryId(26);
            result.setCategoryCode("LIBRARY_SERVICES");
            result.setCategoryDisplayName("Central Library, Books & Reading Hall");
            result.setCategoryIcon("bi-book-half");
            result.setTargetDeptId(17);
            result.setTargetDeptCode("LIBRARY_SERVICES");
            result.setTargetDeptName("Central Library & Learning Resources");
            result.setPriority(Priority.LOW);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Central Library book circulation / catalog concern recognized. Assigned to Library team.");
            return result;
        }

        // Case L: Disambiguate Finance vs Other (e.g. "found a receipt on road", "water bill receipt", "fee payment failed due to internet")
        if (lower.contains("found a receipt") || lower.contains("receipt on the road")) {
            result.setReviewNeeded(true);
            result.setIntelligenceReasoning("Found property reported near office. Assigned to General Student Services / Lost & Found.");
            setTriageFallbackCategory(result);
            return result;
        }
        if (lower.contains("transport fee receipt")) {
            result.setCategoryId(21);
            result.setCategoryCode("FEE_FINANCE");
            result.setCategoryDisplayName("Tuition Fees, Scholarship & Finance Office");
            result.setCategoryIcon("bi-cash-coin");
            result.setTargetDeptId(16);
            result.setTargetDeptCode("FINANCE_ACCOUNTS");
            result.setTargetDeptName("Directorate of Finance & Accounts");
            result.setPriority(Priority.MEDIUM);
            result.setReviewNeeded(true);
            result.setIntelligenceReasoning("Fee receipt inquiry recognized. Transport or Finance selection may apply.");
            return result;
        }
        if (lower.contains("receipt for a water bill") || lower.contains("water bill")) {
            result.setCategoryId(21);
            result.setCategoryCode("FEE_FINANCE");
            result.setCategoryDisplayName("Tuition Fees, Scholarship & Finance Office");
            result.setCategoryIcon("bi-cash-coin");
            result.setTargetDeptId(16);
            result.setTargetDeptCode("FINANCE_ACCOUNTS");
            result.setTargetDeptName("Directorate of Finance & Accounts");
            result.setPriority(Priority.MEDIUM);
            result.setReviewNeeded(true);
            result.setIntelligenceReasoning("Utility billing inquiry recognized. Finance / Accounts assessment required.");
            return result;
        }
        if (lower.contains("payment failed due to") && lower.contains("internet")) {
            result.setCategoryId(21);
            result.setCategoryCode("FEE_FINANCE");
            result.setCategoryDisplayName("Tuition Fees, Scholarship & Finance Office");
            result.setCategoryIcon("bi-cash-coin");
            result.setTargetDeptId(16);
            result.setTargetDeptCode("FINANCE_ACCOUNTS");
            result.setTargetDeptName("Directorate of Finance & Accounts");
            result.setPriority(Priority.HIGH);
            result.setReviewNeeded(true);
            result.setIntelligenceReasoning("Payment gateway interruption reported. Routed to Finance & Accounts.");
            return result;
        }
        if (FINANCE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(21);
            result.setCategoryCode("FEE_FINANCE");
            result.setCategoryDisplayName("Tuition Fees, Scholarship & Finance Office");
            result.setCategoryIcon("bi-cash-coin");
            result.setTargetDeptId(16);
            result.setTargetDeptCode("FINANCE_ACCOUNTS");
            result.setTargetDeptName("Directorate of Finance & Accounts");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Tuition / Scholarship / Finance matter recognized. Routed to Directorate of Finance.");
            return result;
        }

        // Case M: Examinations, CGPA & CoE
        if (EXAM_COE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(20);
            result.setCategoryCode("EXAM_EVALUATION");
            result.setCategoryDisplayName("Examinations, Results & Grade Evaluation");
            result.setCategoryIcon("bi-journal-check");
            result.setTargetDeptId(15);
            result.setTargetDeptCode("EXAMINATION_COE");
            result.setTargetDeptName("Office of Controller of Examinations (CoE)");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Academic examination / result grade evaluation grievance identified. Routed to CoE.");
            return result;
        }

        // Case N: Academic Affairs / Attendance / Timetable
        if (ACADEMIC_AFFAIRS_PATTERN.matcher(lower).find()) {
            result.setCategoryId(29);
            result.setCategoryCode("ACADEMIC_ATTENDANCE_FACULTY");
            result.setCategoryDisplayName("Academic Grievances, Attendance & Course Materials");
            result.setCategoryIcon("bi-mortarboard");
            result.setTargetDeptId(20);
            result.setTargetDeptCode("ACADEMIC_AFFAIRS");
            result.setTargetDeptName("Academic Affairs & Teaching Coordination");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Faculty coordination / attendance discrepancy reported. Routed to Academic Affairs.");
            return result;
        }

        // Case O: CRCS / Placements / Internships
        if (CRCS_PLACEMENT_PATTERN.matcher(lower).find()) {
            result.setCategoryId(28);
            result.setCategoryCode("PLACEMENT_INTERNSHIP");
            result.setCategoryDisplayName("CRCS Placement Drives & Internship Approvals");
            result.setCategoryIcon("bi-briefcase");
            result.setTargetDeptId(19);
            result.setTargetDeptCode("CRCS_CAREER_SERVICES");
            result.setTargetDeptName("Career Services, Placements & Internships (CRCS)");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Placement interview / internship concern reported. Routed to Career Services (CRCS).");
            return result;
        }

        // Case P: Transport Services & Lost Property on Bus
        if (lower.contains("lost") && lower.contains("bus")) {
            result.setCategoryId(27);
            result.setCategoryCode("TRANSPORT_SERVICES");
            result.setCategoryDisplayName("Campus Bus Schedules & Commuter Transport");
            result.setCategoryIcon("bi-bus-front");
            result.setTargetDeptId(18);
            result.setTargetDeptCode("TRANSPORT_SERVICES");
            result.setTargetDeptName("Campus Transport & Commuter Services");
            result.setPriority(Priority.MEDIUM);
            result.setReviewNeeded(true);
            result.setIntelligenceReasoning("Lost property on university bus. Transport desk notified.");
            return result;
        }
        if (TRANSPORT_PATTERN.matcher(lower).find()) {
            result.setCategoryId(27);
            result.setCategoryCode("TRANSPORT_SERVICES");
            result.setCategoryDisplayName("Campus Bus Schedules & Commuter Transport");
            result.setCategoryIcon("bi-bus-front");
            result.setTargetDeptId(18);
            result.setTargetDeptCode("TRANSPORT_SERVICES");
            result.setTargetDeptName("Campus Transport & Commuter Services");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("University commuter transport issue identified. Routed to Transport Office.");
            return result;
        }

        // Case Q: Water & Plumbing
        if (WATER_OUTAGE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(2);
            result.setCategoryCode("WATER_OUTAGE");
            result.setCategoryDisplayName("Water Supply Cut");
            result.setCategoryIcon("bi-droplet-half");
            result.setTargetDeptId(2);
            result.setTargetDeptCode("WATER_PLUMBING");
            result.setTargetDeptName("Water, RO Plant & Plumbing Services");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Washroom / pipeline water outage recognized. Routed to Water & Plumbing Services.");
            return result;
        }
        if (WATER_COOLER_PATTERN.matcher(lower).find()) {
            result.setCategoryId(3);
            result.setCategoryCode("HOT_COLD_WATER");
            result.setCategoryDisplayName("Hot/Cold Water Dispenser");
            result.setCategoryIcon("bi-cup-hot");
            result.setTargetDeptId(2);
            result.setTargetDeptCode("WATER_PLUMBING");
            result.setTargetDeptName("Water, RO Plant & Plumbing Services");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Drinking water dispenser issue recognized. Assigned to Plumbing Services.");
            return result;
        }

        // Case R: AC & Cooling
        if (AC_COOLING_PATTERN.matcher(lower).find()) {
            result.setCategoryId(1);
            result.setCategoryCode("AC_COOLING");
            result.setCategoryDisplayName("AC Not Cooling");
            result.setCategoryIcon("bi-thermometer-snow");
            result.setTargetDeptId(1);
            result.setTargetDeptCode("HOSTEL_MAINT");
            result.setTargetDeptName("Campus Life & Maintenance (CLM)");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Air conditioning defect recognized. Routed to CLM HVAC technicians.");
            return result;
        }

        // Case S: Wi-Fi & Network
        if (NETWORK_CONNECTIVITY_PATTERN.matcher(lower).find()) {
            return buildWifiResult(result, "Campus Wi-Fi / network connection issue recognized. Assigned to ITKM Helpdesk.");
        }

        // Case T: Mess & Food Quality
        if (MESS_FOOD_PATTERN.matcher(lower).find() || lower.contains("mess") && (lower.contains("food") || lower.contains("hygiene"))) {
            result.setCategoryId(4);
            result.setCategoryCode("MESS_FOOD_QUALITY");
            result.setCategoryDisplayName("Mess Food Quality");
            result.setCategoryIcon("bi-egg-fried");
            result.setTargetDeptId(3);
            result.setTargetDeptCode("MESS_COMMITTEE");
            result.setTargetDeptName("Annapoorna Mess & Dining Committee");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Mess dining or food hygiene concern recognized. Assigned to Mess Committee.");
            return result;
        }

        // Case U: Housekeeping
        if (HOUSEKEEPING_PATTERN.matcher(lower).find()) {
            return buildHousekeepingResult(result, "Sanitation / housekeeping service requested. Assigned to Housekeeping.");
        }

        // Case V: Electrical Repair
        if (ELECTRICAL_PATTERN.matcher(lower).find()) {
            result.setCategoryId(7);
            result.setCategoryCode("ELECTRICAL_REPAIR");
            result.setCategoryDisplayName("Electrical Repair");
            result.setCategoryIcon("bi-lightning-charge");
            result.setTargetDeptId(1);
            result.setTargetDeptCode("HOSTEL_MAINT");
            result.setTargetDeptName("Campus Life & Maintenance (CLM)");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Electrical fixture maintenance requested. Assigned to CLM Electrician team.");
            return result;
        }

        // Case W: Furniture & Carpentry
        if (FURNITURE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(13);
            result.setCategoryCode("FURNITURE_CARPENTRY");
            result.setCategoryDisplayName("Broken Furniture & Carpentry");
            result.setCategoryIcon("bi-inboxes");
            result.setTargetDeptId(1);
            result.setTargetDeptCode("HOSTEL_MAINT");
            result.setTargetDeptName("Campus Life & Maintenance (CLM)");
            result.setPriority(lower.contains("dangerous") ? Priority.HIGH : Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Furniture repair requested. Assigned to CLM Carpentry & Civil team.");
            return result;
        }

        // Case X: Academic Labs
        if (LAB_INSTRUMENTATION_PATTERN.matcher(lower).find()) {
            result.setCategoryId(12);
            result.setCategoryCode("LAB_INSTRUMENTATION");
            result.setCategoryDisplayName("Academic Lab Equipment & Voltmeter");
            result.setCategoryIcon("bi-cpu");
            result.setTargetDeptId(7);
            result.setTargetDeptCode("ACADEMIC_LABS");
            result.setTargetDeptName("Academic Labs & Scientific Instrumentation");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Laboratory apparatus defect recognized. Assigned to Engineering Labs staff.");
            return result;
        }

        // Case Y: Classroom AV
        if (CLASSROOM_AV_PATTERN.matcher(lower).find()) {
            result.setCategoryId(8);
            result.setCategoryCode("CLASSROOM_AV");
            result.setCategoryDisplayName("Classroom AV Equipment");
            result.setCategoryIcon("bi-display");
            result.setTargetDeptId(5);
            result.setTargetDeptCode("ITKM_HELPDESK");
            result.setTargetDeptName("IT & Knowledge Management (ITKM) Helpdesk");
            result.setPriority(Priority.MEDIUM);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Classroom multimedia projector/audio issue recognized. Assigned to ITKM.");
            return result;
        }

        // Case Z: Laundry
        if (LAUNDRY_PATTERN.matcher(lower).find()) {
            result.setCategoryId(15);
            result.setCategoryCode("LAUNDRY_CLOTHES");
            result.setCategoryDisplayName("Hostel Laundry & Clothes Washing");
            result.setCategoryIcon("bi-basket2");
            result.setTargetDeptId(9);
            result.setTargetDeptCode("LAUNDRY_SERVICES");
            result.setTargetDeptName("Campus Laundry & Student Care");
            result.setPriority(Priority.LOW);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Laundry operations concern recognized. Assigned to Laundry Services.");
            return result;
        }

        // Case AA: Commercial Outlets & MRP
        if (COMMERCIAL_PATTERN.matcher(lower).find()) {
            result.setCategoryId(16);
            result.setCategoryCode("VENDOR_OVERPRICING");
            result.setCategoryDisplayName("Canteen / Vendor Overpricing & MRP");
            result.setCategoryIcon("bi-shop");
            result.setTargetDeptId(10);
            result.setTargetDeptCode("COMMERCIAL_WELFARE");
            result.setTargetDeptName("Commercial Outlets & Student Welfare");
            result.setPriority(Priority.HIGH);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Commercial vendor overpricing concern recognized. Assigned to Commercial Oversight.");
            return result;
        }

        // Case BB: Horticulture & Roads
        if (HORTICULTURE_PATTERN.matcher(lower).find()) {
            result.setCategoryId(14);
            result.setCategoryCode("HORTICULTURE_ROADS");
            result.setCategoryDisplayName("Campus Roads, Leaves & Grounds");
            result.setCategoryIcon("bi-tree");
            result.setTargetDeptId(8);
            result.setTargetDeptCode("HORTICULTURE_ESTATE");
            result.setTargetDeptName("Campus Horticulture, Roads & Grounds");
            result.setPriority(Priority.LOW);
            result.setEmergency(false);
            result.setIntelligenceReasoning("Campus roadway / horticulture maintenance recognized. Assigned to Grounds Office.");
            return result;
        }

        // 4. Stage 5: Explainable Uncertainty Fallback (Never defaults silently to CLM)
        setAmbiguousFallback(result, "Category not definitively recognized. Please choose your grievance category from the dropdown menu, or leave selected for Student Affairs triage.");
        return result;
    }

    private void setAmbiguousFallback(IssuePredictionResult result, String reason) {
        result.setReviewNeeded(true);
        result.setEmergency(false);
        result.setConfidenceScore(0.0);
        result.setIntelligenceReasoning(reason);
        setTriageFallbackCategory(result);
    }

    private void setTriageFallbackCategory(IssuePredictionResult result) {
        result.setCategoryId(31);
        result.setCategoryCode("GENERAL_STUDENT_SERVICES");
        result.setCategoryDisplayName("General Student Affairs & SGRC Triage");
        result.setCategoryIcon("bi-inbox");
        result.setTargetDeptId(13);
        result.setTargetDeptCode("STUDENT_AFFAIRS_SGRC");
        result.setTargetDeptName("Directorate of Student Affairs (DSA) & SGRC");
        result.setPriority(Priority.MEDIUM);
        result.setLocationScope(LocationScope.ANY);
        result.setAckMinutes(240);
        result.setResolutionMinutes(1440);
    }

    private IssuePredictionResult buildCyberHarassmentResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(18);
        result.setCategoryCode("CYBER_HARASSMENT");
        result.setCategoryDisplayName("Online Harassment & Cyber Stalking (Confidential)");
        result.setCategoryIcon("bi-shield-shaded");
        result.setTargetDeptId(11);
        result.setTargetDeptCode("INTERNAL_COMPLAINTS_COMMITTEE");
        result.setTargetDeptName("Internal Complaints Committee (ICC) & Anti-Harassment Cell");
        result.setPriority(Priority.CRITICAL);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(true);
        result.setEmergencyActionNotice("🔒 CONFIDENTIAL REPORT: Routed to Internal Complaints Committee (ICC). For immediate confidential support, contact: icc@srmap.edu.in / Student Affairs Helpline: " + STUDENT_AFFAIRS_HELPLINE);
        result.setAckMinutes(15);
        result.setResolutionMinutes(120);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildAntiRaggingResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(19);
        result.setCategoryCode("ANTI_RAGGING");
        result.setCategoryDisplayName("Ragging & Senior Intimidation (Zero-Tolerance)");
        result.setCategoryIcon("bi-shield-x");
        result.setTargetDeptId(12);
        result.setTargetDeptCode("ANTI_RAGGING_CELL");
        result.setTargetDeptName("Anti-Ragging Committee & Squad");
        result.setPriority(Priority.CRITICAL);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(true);
        result.setEmergencyActionNotice("🚨 ZERO-TOLERANCE ANTI-RAGGING: Routed to Anti-Ragging Committee. National 24/7 Helpline: " + NATIONAL_ANTI_RAGGING_HELPLINE + " or Student Affairs: " + STUDENT_AFFAIRS_HELPLINE);
        result.setAckMinutes(15);
        result.setResolutionMinutes(60);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildUrgentDisputeResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(23);
        result.setCategoryCode("STUDENT_DISPUTES");
        result.setCategoryDisplayName("Student Affairs & Hostel Mediation");
        result.setCategoryIcon("bi-people");
        result.setTargetDeptId(13);
        result.setTargetDeptCode("STUDENT_AFFAIRS_SGRC");
        result.setTargetDeptName("Directorate of Student Affairs (DSA) & SGRC");
        result.setPriority(Priority.CRITICAL);
        result.setLocationScope(LocationScope.HOSTEL);
        result.setEmergency(true);
        result.setEmergencyActionNotice("🚨 SAFETY ALERT: Physical safety threat noted. Contact Hostel Warden or Student Affairs immediately: " + STUDENT_AFFAIRS_HELPLINE);
        result.setAckMinutes(30);
        result.setResolutionMinutes(180);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildRoutineDisputeResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(23);
        result.setCategoryCode("STUDENT_DISPUTES");
        result.setCategoryDisplayName("Student Affairs & Hostel Mediation");
        result.setCategoryIcon("bi-people");
        result.setTargetDeptId(13);
        result.setTargetDeptCode("STUDENT_AFFAIRS_SGRC");
        result.setTargetDeptName("Directorate of Student Affairs (DSA) & SGRC");
        result.setPriority(Priority.LOW);
        result.setLocationScope(LocationScope.HOSTEL);
        result.setEmergency(false);
        result.setAckMinutes(480);
        result.setResolutionMinutes(2880);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildActiveWildlifeResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(9);
        result.setCategoryCode("SNAKE_WILDLIFE_SOS");
        result.setCategoryDisplayName("Snake / Wildlife Threat (SOS)");
        result.setCategoryIcon("bi-shield-exclamation");
        result.setTargetDeptId(6);
        result.setTargetDeptCode("SECURITY_EMERGENCY");
        result.setTargetDeptName("Campus Security & Quick Response Emergency Team");
        result.setPriority(Priority.CRITICAL);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(true);
        result.setEmergencyActionNotice("🚨 WILDLIFE EMERGENCY: Stay back from the area. Contact Campus Security immediately: " + SECURITY_CONTROL_ROOM + " / " + STUDENT_AFFAIRS_HELPLINE);
        result.setAckMinutes(15);
        result.setResolutionMinutes(60);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildActiveFireResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(10);
        result.setCategoryCode("FIRE_SAFETY_SOS");
        result.setCategoryDisplayName("Fire, Smoke & Gas Leak (SOS)");
        result.setCategoryIcon("bi-fire");
        result.setTargetDeptId(6);
        result.setTargetDeptCode("SECURITY_EMERGENCY");
        result.setTargetDeptName("Campus Security & Quick Response Emergency Team");
        result.setPriority(Priority.CRITICAL);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(true);
        result.setEmergencyActionNotice("🚨 FIRE / SMOKE ALERT: Evacuate the area immediately. Contact Security Control Room: " + SECURITY_CONTROL_ROOM);
        result.setAckMinutes(10);
        result.setResolutionMinutes(30);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildMedicalEmergencyResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(11);
        result.setCategoryCode("MEDICAL_EMERGENCY_SOS");
        result.setCategoryDisplayName("Medical Emergency (SOS)");
        result.setCategoryIcon("bi-heart-pulse-fill");
        result.setTargetDeptId(14);
        result.setTargetDeptCode("HEALTH_COUNSELING");
        result.setTargetDeptName("Health Centre, Wellness & Student Counseling");
        result.setPriority(Priority.CRITICAL);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(true);
        result.setEmergencyActionNotice("🚨 MEDICAL EMERGENCY: Call Campus Health Centre 24x7 Emergency Line: " + HEALTH_CENTRE_24X7 + " or Security: " + SECURITY_CONTROL_ROOM);
        result.setAckMinutes(10);
        result.setResolutionMinutes(30);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildWifiResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(6);
        result.setCategoryCode("WIFI_NETWORK");
        result.setCategoryDisplayName("Wi-Fi / Network Issue");
        result.setCategoryIcon("bi-wifi-off");
        result.setTargetDeptId(5);
        result.setTargetDeptCode("ITKM_HELPDESK");
        result.setTargetDeptName("IT & Knowledge Management (ITKM) Helpdesk");
        result.setPriority(Priority.HIGH);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(false);
        result.setAckMinutes(60);
        result.setResolutionMinutes(360);
        result.setIntelligenceReasoning(reason);
        return result;
    }

    private IssuePredictionResult buildHousekeepingResult(IssuePredictionResult result, String reason) {
        result.setCategoryId(5);
        result.setCategoryCode("ROOM_CLEANING");
        result.setCategoryDisplayName("Room Cleaning");
        result.setCategoryIcon("bi-house-heart");
        result.setTargetDeptId(4);
        result.setTargetDeptCode("HOUSEKEEPING");
        result.setTargetDeptName("Housekeeping & Campus Sanitation");
        result.setPriority(Priority.LOW);
        result.setLocationScope(LocationScope.ANY);
        result.setEmergency(false);
        result.setAckMinutes(480);
        result.setResolutionMinutes(2880);
        result.setIntelligenceReasoning(reason);
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
        if (lower.contains("examination block") || lower.contains("exam block")) {
            return "Examination Block";
        }
        if (lower.contains("sports") || lower.contains("gym") || lower.contains("ground")) {
            return "University Sports Complex & Gym";
        }
        if (lower.contains("road") || lower.contains("pathway") || lower.contains("street")) {
            return "Campus Main Roadway & Grounds";
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
