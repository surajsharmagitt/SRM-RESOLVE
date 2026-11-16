package com.srmresolve.service;

import com.srmresolve.dto.IssuePredictionResult;
import com.srmresolve.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SRM RESOLVE Acceptance Test Suite: 80 Test Cases
 * - 60 Specification Test Cases from SRM_RESOLVE_RESEARCH_FIX_SPEC.md
 * - 20 Additional Contrasting, Edge & Negative Context Cases
 */
@DisplayName("SRM-AP Campus Intelligence Engine 80-Case Acceptance Test Suite")
class CampusIntelligenceAcceptanceTest {

    private CampusIntelligenceEngine engine;
    private User testStudent;

    @BeforeEach
    void setUp() {
        engine = new CampusIntelligenceEngine();
        testStudent = new User();
        testStudent.setFullName("Suraj Sharma");
        testStudent.setRegOrStaffId("AP25110070893");
        testStudent.setBuildingBlock("Ganga Tower - Block A");
        testStudent.setRoomOrZone("Room 412");
    }

    // =========================================================================
    // SECTION 1: The 60 Specification Acceptance Test Cases
    // =========================================================================

    @Test
    @DisplayName("Case 01: Active snake moving near hostel gate -> Wildlife SOS, Emergency = YES")
    void testCase01_ActiveSnakeHostelGate() {
        IssuePredictionResult result = engine.predictIssue("A snake is moving near Ganga hostel gate right now.", testStudent);
        assertEquals(9, result.getCategoryId(), "Expected SNAKE_WILDLIFE_SOS (Cat 9)");
        assertEquals(6, result.getTargetDeptId(), "Expected SECURITY_EMERGENCY (Dept 6)");
        assertTrue(result.isEmergency(), "Active snake moving right now must be flagged EMERGENCY");
        assertFalse(result.isNoIncident());
    }

    @Test
    @DisplayName("Case 02: Lion roaming near campus entrance -> Wildlife SOS, Emergency = YES")
    void testCase02_LionRoamingEntrance() {
        IssuePredictionResult result = engine.predictIssue("I saw a lion roaming near the campus entrance.", testStudent);
        assertEquals(9, result.getCategoryId(), "Expected SNAKE_WILDLIFE_SOS (Cat 9)");
        assertEquals(6, result.getTargetDeptId(), "Expected SECURITY_EMERGENCY (Dept 6)");
        assertTrue(result.isEmergency(), "Active lion roaming must be flagged EMERGENCY");
        assertFalse(result.isNoIncident());
    }

    @Test
    @DisplayName("Case 03: Biology class studied lions -> NO INCIDENT, Emergency = NO")
    void testCase03_ClassroomBiologyLions() {
        IssuePredictionResult result = engine.predictIssue("We studied lions in biology class today.", testStudent);
        assertTrue(result.isNoIncident(), "Classroom topic must be recognized as NO INCIDENT");
        assertFalse(result.isEmergency(), "Academic topic must NOT trigger emergency");
    }

    @Test
    @DisplayName("Case 04: Projector showed video of snake -> NO INCIDENT, Emergency = NO")
    void testCase04_ProjectorSnakeVideo() {
        IssuePredictionResult result = engine.predictIssue("The projector showed a video of a snake.", testStudent);
        assertTrue(result.isNoIncident(), "Video display must be recognized as NO INCIDENT");
        assertFalse(result.isEmergency(), "Video must NOT trigger emergency");
    }

    @Test
    @DisplayName("Case 05: I saw a lion (unverified/vague) -> REVIEW, Emergency = NOT AUTOMATIC")
    void testCase05_VagueLionMention() {
        IssuePredictionResult result = engine.predictIssue("I saw a lion.", testStudent);
        assertEquals(9, result.getCategoryId());
        assertEquals(6, result.getTargetDeptId());
        assertTrue(result.isReviewNeeded(), "Vague wildlife mention without active motion must require review");
        assertFalse(result.isEmergency(), "Vague mention must not automatically trigger emergency dispatch");
    }

    @Test
    @DisplayName("Case 06: Snake poster hanging in classroom -> NO INCIDENT, Emergency = NO")
    void testCase06_SnakePosterClassroom() {
        IssuePredictionResult result = engine.predictIssue("Snake poster is hanging in our classroom.", testStudent);
        assertTrue(result.isNoIncident(), "Poster mention must be recognized as NO INCIDENT");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 07: Smoke coming from electrical panel -> Fire SOS, Emergency = YES")
    void testCase07_SmokeElectricalPanel() {
        IssuePredictionResult result = engine.predictIssue("Smoke is coming from the hostel electrical panel now.", testStudent);
        assertEquals(10, result.getCategoryId(), "Expected FIRE_SAFETY_SOS (Cat 10)");
        assertEquals(6, result.getTargetDeptId(), "Expected SECURITY_EMERGENCY (Dept 6)");
        assertTrue(result.isEmergency(), "Active smoke must be flagged EMERGENCY");
    }

    @Test
    @DisplayName("Case 08: Attended fire safety lecture -> NO INCIDENT, Emergency = NO")
    void testCase08_FireSafetyLecture() {
        IssuePredictionResult result = engine.predictIssue("We attended a fire safety lecture yesterday.", testStudent);
        assertTrue(result.isNoIncident(), "Lecture mention must be recognized as NO INCIDENT");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 09: Fire extinguisher near exit empty -> Equipment maintenance, Emergency = NO (Review)")
    void testCase09_FireExtinguisherEmpty() {
        IssuePredictionResult result = engine.predictIssue("Fire extinguisher near the exit is empty.", testStudent);
        assertEquals(10, result.getCategoryId());
        assertEquals(6, result.getTargetDeptId());
        assertFalse(result.isEmergency(), "Empty extinguisher is equipment maintenance, not active flame emergency");
    }

    @Test
    @DisplayName("Case 10: Phone cannot connect to Instagram on Wi-Fi -> Wi-Fi / ITKM, Emergency = NO")
    void testCase10_InstagramWifiConnection() {
        IssuePredictionResult result = engine.predictIssue("My phone cannot connect to Instagram on hostel Wi-Fi.", testStudent);
        assertEquals(6, result.getCategoryId(), "Expected WI_FI_NETWORK (Cat 6)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency(), "Social app Wi-Fi issue must NOT trigger harassment emergency");
    }

    @Test
    @DisplayName("Case 11: Blackmail with private photos on Instagram -> Cyber Harassment ICC, Confidential/Emergency")
    void testCase11_InstagramBlackmailPrivatePhotos() {
        IssuePredictionResult result = engine.predictIssue("Someone is blackmailing me with private photos on Instagram.", testStudent);
        assertEquals(18, result.getCategoryId(), "Expected CYBER_HARASSMENT (Cat 18)");
        assertEquals(11, result.getTargetDeptId(), "Expected INTERNAL_COMPLAINTS_COMMITTEE (Dept 11)");
        assertTrue(result.isEmergency(), "Blackmail with private photos must trigger confidential rapid protection");
    }

    @Test
    @DisplayName("Case 12: Help accessing WhatsApp on campus network -> ITKM network, Emergency = NO")
    void testCase12_WhatsappCampusNetwork() {
        IssuePredictionResult result = engine.predictIssue("I need help accessing WhatsApp on the campus network.", testStudent);
        assertEquals(6, result.getCategoryId(), "Expected WI_FI_NETWORK (Cat 6)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 13: Senior forcing humiliating intro sessions -> Ragging, Emergency = YES")
    void testCase13_SeniorForcedIntroRagging() {
        IssuePredictionResult result = engine.predictIssue("A senior is forcing us to attend humiliating intro sessions.", testStudent);
        assertEquals(19, result.getCategoryId(), "Expected RAGGING_INTIMIDATION (Cat 19)");
        assertEquals(12, result.getTargetDeptId(), "Expected ANTI_RAGGING_CELL (Dept 12)");
        assertTrue(result.isEmergency(), "Senior ragging/intimidation must trigger Emergency safety workflow");
    }

    @Test
    @DisplayName("Case 14: Roommate threatened to hurt me tonight -> Interpersonal Threat, Emergency = YES")
    void testCase14_RoommatePhysicalThreat() {
        IssuePredictionResult result = engine.predictIssue("My roommate threatened to hurt me tonight.", testStudent);
        assertEquals(23, result.getCategoryId(), "Expected STUDENT_DISPUTES (Cat 23)");
        assertEquals(13, result.getTargetDeptId(), "Expected STUDENT_AFFAIRS_SGRC (Dept 13)");
        assertTrue(result.isEmergency(), "Physical threat to student must be flagged Emergency");
    }

    @Test
    @DisplayName("Case 15: Roommate refuses to share cupboard space -> Routine mediation, Emergency = NO")
    void testCase15_RoommateCupboardDispute() {
        IssuePredictionResult result = engine.predictIssue("My roommate refuses to share cupboard space.", testStudent);
        assertEquals(23, result.getCategoryId(), "Expected STUDENT_DISPUTES (Cat 23)");
        assertEquals(13, result.getTargetDeptId(), "Expected STUDENT_AFFAIRS_SGRC (Dept 13)");
        assertFalse(result.isEmergency(), "Routine cupboard dispute is not an emergency");
    }

    @Test
    @DisplayName("Case 16: Lost student ID card yesterday -> Lost ID Card (Cat 24, Dept 5), Emergency = NO")
    void testCase16_LostStudentIdCard() {
        IssuePredictionResult result = engine.predictIssue("I lost my student ID card yesterday.", testStudent);
        assertEquals(24, result.getCategoryId(), "Expected LOST_OR_DAMAGED_ID_CARD (Cat 24)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 17: ID card won't scan at library gate -> Lost/Card Access (Cat 24, Dept 5), Emergency = NO")
    void testCase17_IdCardWontScanLibrary() {
        IssuePredictionResult result = engine.predictIssue("My ID card won't scan at the library gate.", testStudent);
        assertEquals(24, result.getCategoryId(), "Expected LOST_OR_DAMAGED_ID_CARD (Cat 24)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 18: Library book marked returned prematurely -> Library Services (Cat 26, Dept 17), Emergency = NO")
    void testCase18_LibraryBookStatusDiscrepancy() {
        IssuePredictionResult result = engine.predictIssue("My library book has been marked returned even though I still have it.", testStudent);
        assertEquals(26, result.getCategoryId(), "Expected LIBRARY_SERVICES (Cat 26)");
        assertEquals(17, result.getTargetDeptId(), "Expected LIBRARY_SERVICES (Dept 17)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 19: Central library books missing -> Library Services (Cat 26, Dept 17), Emergency = NO")
    void testCase19_CentralLibraryBooksMissing() {
        IssuePredictionResult result = engine.predictIssue("The central library books I need are missing.", testStudent);
        assertEquals(26, result.getCategoryId(), "Expected LIBRARY_SERVICES (Cat 26)");
        assertEquals(17, result.getTargetDeptId(), "Expected LIBRARY_SERVICES (Dept 17)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 20: Library internet keeps disconnecting -> ITKM / Wi-Fi (Cat 6, Dept 5)")
    void testCase20_LibraryInternetDisconnecting() {
        IssuePredictionResult result = engine.predictIssue("The library internet keeps disconnecting.", testStudent);
        assertEquals(6, result.getCategoryId(), "Expected WI_FI_NETWORK (Cat 6)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 21: Scholarship credited incorrectly -> Finance (Cat 21, Dept 16), Emergency = NO")
    void testCase21_ScholarshipCreditedIncorrectly() {
        IssuePredictionResult result = engine.predictIssue("My scholarship was credited incorrectly.", testStudent);
        assertEquals(21, result.getCategoryId(), "Expected FEE_FINANCE (Cat 21)");
        assertEquals(16, result.getTargetDeptId(), "Expected FINANCE_ACCOUNTS (Dept 16)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 22: Fee deducted twice from bank account -> Finance (Cat 21, Dept 16), Emergency = NO")
    void testCase22_FeeDeductedTwice() {
        IssuePredictionResult result = engine.predictIssue("The fee was deducted twice from my bank account.", testStudent);
        assertEquals(21, result.getCategoryId(), "Expected FEE_FINANCE (Cat 21)");
        assertEquals(16, result.getTargetDeptId(), "Expected FINANCE_ACCOUNTS (Dept 16)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 23: Need a fee receipt for payment -> Finance (Cat 21, Dept 16), Emergency = NO")
    void testCase23_NeedFeeReceipt() {
        IssuePredictionResult result = engine.predictIssue("I need a fee receipt for my payment.", testStudent);
        assertEquals(21, result.getCategoryId(), "Expected FEE_FINANCE (Cat 21)");
        assertEquals(16, result.getTargetDeptId(), "Expected FINANCE_ACCOUNTS (Dept 16)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 24: Found receipt on road near finance office -> REVIEW found property, no forced Finance")
    void testCase24_FoundReceiptOnRoad() {
        IssuePredictionResult result = engine.predictIssue("I found a receipt on the road near the finance office.", testStudent);
        assertTrue(result.isReviewNeeded(), "Found property must require review, not force Finance");
        assertEquals(31, result.getCategoryId(), "Expected GENERAL_STUDENT_SERVICES triage (Cat 31)");
        assertEquals(13, result.getTargetDeptId(), "Expected STUDENT_AFFAIRS_SGRC triage (Dept 13)");
    }

    @Test
    @DisplayName("Case 25: CGPA wrong after results released -> CoE Examination (Cat 20, Dept 15)")
    void testCase25_CgpaResultsWrong() {
        IssuePredictionResult result = engine.predictIssue("My CGPA is wrong even after results were released.", testStudent);
        assertEquals(20, result.getCategoryId(), "Expected EXAM_EVALUATION (Cat 20)");
        assertEquals(15, result.getTargetDeptId(), "Expected EXAMINATION_COE (Dept 15)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 26: Attendance marked absent although present -> Academic Affairs (Cat 29, Dept 20)")
    void testCase26_AttendanceMarkedAbsent() {
        IssuePredictionResult result = engine.predictIssue("Attendance marked absent even though I was present.", testStudent);
        assertEquals(29, result.getCategoryId(), "Expected ACADEMIC_ATTENDANCE_FACULTY (Cat 29)");
        assertEquals(20, result.getTargetDeptId(), "Expected ACADEMIC_AFFAIRS (Dept 20)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 27: Professor has not uploaded course materials -> Academic Affairs (Cat 29, Dept 20)")
    void testCase27_CourseMaterialsNotUploaded() {
        IssuePredictionResult result = engine.predictIssue("The professor has not uploaded course materials.", testStudent);
        assertEquals(29, result.getCategoryId(), "Expected ACADEMIC_ATTENDANCE_FACULTY (Cat 29)");
        assertEquals(20, result.getTargetDeptId(), "Expected ACADEMIC_AFFAIRS (Dept 20)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 28: Placement interview slots unfairly assigned -> CRCS Career Services (Cat 28, Dept 19)")
    void testCase28_PlacementSlotsUnfair() {
        IssuePredictionResult result = engine.predictIssue("Placement interview slots were unfairly assigned.", testStudent);
        assertEquals(28, result.getCategoryId(), "Expected PLACEMENT_INTERNSHIP (Cat 28)");
        assertEquals(19, result.getTargetDeptId(), "Expected CRCS_CAREER_SERVICES (Dept 19)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 29: Internship approval pending with CRCS -> CRCS Career Services (Cat 28, Dept 19)")
    void testCase29_InternshipApprovalCrcs() {
        IssuePredictionResult result = engine.predictIssue("Internship approval is pending with CRCS.", testStudent);
        assertEquals(28, result.getCategoryId(), "Expected PLACEMENT_INTERNSHIP (Cat 28)");
        assertEquals(19, result.getTargetDeptId(), "Expected CRCS_CAREER_SERVICES (Dept 19)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 30: University bus never came to pickup point -> Transport Services (Cat 27, Dept 18)")
    void testCase30_BusNeverCamePickup() {
        IssuePredictionResult result = engine.predictIssue("University bus never came to my pickup point.", testStudent);
        assertEquals(27, result.getCategoryId(), "Expected TRANSPORT_SERVICES (Cat 27)");
        assertEquals(18, result.getTargetDeptId(), "Expected TRANSPORT_SERVICES (Dept 18)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 31: Transport fee receipt missing -> Fee inquiry review (Cat 21, Dept 16)")
    void testCase31_TransportFeeReceiptMissing() {
        IssuePredictionResult result = engine.predictIssue("Transport fee receipt is missing.", testStudent);
        assertEquals(21, result.getCategoryId());
        assertEquals(16, result.getTargetDeptId());
        assertTrue(result.isReviewNeeded(), "Transport fee receipt requires context review");
    }

    @Test
    @DisplayName("Case 32: Sick and need to visit campus clinic -> Routine Health Centre (Cat 25, Dept 14), Emergency = NO")
    void testCase32_VisitCampusClinicSick() {
        IssuePredictionResult result = engine.predictIssue("I am sick and need to visit the campus clinic today.", testStudent);
        assertEquals(25, result.getCategoryId(), "Expected ROUTINE_MEDICAL_SUPPORT (Cat 25)");
        assertEquals(14, result.getTargetDeptId(), "Expected HEALTH_COUNSELING (Dept 14)");
        assertFalse(result.isEmergency(), "Clinic visit for illness is routine, not critical ambulance emergency");
    }

    @Test
    @DisplayName("Case 33: Someone collapsed and is unconscious at mess -> Medical SOS (Cat 11, Dept 14), Emergency = YES")
    void testCase33_CollapsedUnconsciousMess() {
        IssuePredictionResult result = engine.predictIssue("Someone collapsed and is unconscious at the mess.", testStudent);
        assertEquals(11, result.getCategoryId(), "Expected MEDICAL_HEALTH_SOS (Cat 11)");
        assertEquals(14, result.getTargetDeptId(), "Expected HEALTH_COUNSELING (Dept 14)");
        assertTrue(result.isEmergency(), "Unconscious person must trigger Emergency Health SOS");
    }

    @Test
    @DisplayName("Case 34: Medical leave for minor fever -> Routine Health consultation (Cat 25, Dept 14, Review = YES)")
    void testCase34_MedicalLeaveFever() {
        IssuePredictionResult result = engine.predictIssue("I want medical leave for a minor fever.", testStudent);
        assertEquals(25, result.getCategoryId(), "Expected ROUTINE_MEDICAL_SUPPORT (Cat 25)");
        assertEquals(14, result.getTargetDeptId(), "Expected HEALTH_COUNSELING (Dept 14)");
        assertTrue(result.isReviewNeeded());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 35: Attended a medical awareness seminar -> NO INCIDENT, Emergency = NO")
    void testCase35_MedicalSeminar() {
        IssuePredictionResult result = engine.predictIssue("I attended a medical awareness seminar.", testStudent);
        assertTrue(result.isNoIncident(), "Seminar mention must be recognized as NO INCIDENT");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 36: Water hasn't come through washroom taps -> Water Supply Cut (Cat 2, Dept 2)")
    void testCase36_WashroomWaterTaps() {
        IssuePredictionResult result = engine.predictIssue("Water hasn't come through my washroom taps since morning.", testStudent);
        assertEquals(2, result.getCategoryId(), "Expected WATER_OUTAGE (Cat 2)");
        assertEquals(2, result.getTargetDeptId(), "Expected WATER_PLUMBING (Dept 2)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 37: No water in Ganga fourth-floor bathrooms -> Water Supply Cut (Cat 2, Dept 2)")
    void testCase37_GangaBathroomsNoWater() {
        IssuePredictionResult result = engine.predictIssue("There is no water in the Ganga fourth-floor bathrooms.", testStudent);
        assertEquals(2, result.getCategoryId(), "Expected WATER_OUTAGE (Cat 2)");
        assertEquals(2, result.getTargetDeptId(), "Expected WATER_PLUMBING (Dept 2)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 38: Drinking water cooler leaking in mess -> Water Dispenser (Cat 3, Dept 2)")
    void testCase38_DrinkingWaterCoolerLeaking() {
        IssuePredictionResult result = engine.predictIssue("Drinking water cooler is leaking in the mess.", testStudent);
        assertEquals(3, result.getCategoryId(), "Expected HOT_COLD_WATER (Cat 3)");
        assertEquals(2, result.getTargetDeptId(), "Expected WATER_PLUMBING (Dept 2)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 39: Room AC makes loud noise and stops cooling -> CLM AC (Cat 1, Dept 1)")
    void testCase39_RoomAcLoudNoiseCooling() {
        IssuePredictionResult result = engine.predictIssue("My room AC makes a loud noise and stops cooling.", testStudent);
        assertEquals(1, result.getCategoryId(), "Expected AC_COOLING (Cat 1)");
        assertEquals(1, result.getTargetDeptId(), "Expected HOSTEL_MAINT (Dept 1)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 40: Lab multimeter is broken -> Academic Labs (Cat 12, Dept 7)")
    void testCase40_LabMultimeterBroken() {
        IssuePredictionResult result = engine.predictIssue("The lab multimeter is broken.", testStudent);
        assertEquals(12, result.getCategoryId(), "Expected LAB_INSTRUMENTATION (Cat 12)");
        assertEquals(7, result.getTargetDeptId(), "Expected ACADEMIC_LABS (Dept 7)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 41: Projector in lecture hall 202 not working -> Classroom AV (Cat 8, Dept 5)")
    void testCase41_ProjectorLectureHall202() {
        IssuePredictionResult result = engine.predictIssue("The projector in lecture hall 202 is not working.", testStudent);
        assertEquals(8, result.getCategoryId(), "Expected CLASSROOM_AV (Cat 8)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 42: Toilet near library smells and is filthy -> Housekeeping (Cat 5, Dept 4)")
    void testCase42_ToiletNearLibrarySmells() {
        IssuePredictionResult result = engine.predictIssue("The toilet near the library smells and is filthy.", testStudent);
        assertEquals(5, result.getCategoryId(), "Expected HOUSEKEEPING_CLEANING (Cat 5)");
        assertEquals(4, result.getTargetDeptId(), "Expected HOUSEKEEPING_FACILITIES (Dept 4)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 43: Food has insects in hostel mess -> Mess Food Quality (Cat 4, Dept 3)")
    void testCase43_FoodInsectsHostelMess() {
        IssuePredictionResult result = engine.predictIssue("Food has insects in the hostel mess.", testStudent);
        assertEquals(4, result.getCategoryId(), "Expected MESS_FOOD_QUALITY (Cat 4)");
        assertEquals(3, result.getTargetDeptId(), "Expected MESS_COMMITTEE (Dept 3)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 44: Laundry shirt was lost -> Laundry Services (Cat 15, Dept 9)")
    void testCase44_LaundryShirtLost() {
        IssuePredictionResult result = engine.predictIssue("My laundry shirt was lost.", testStudent);
        assertEquals(15, result.getCategoryId(), "Expected LAUNDRY_CLOTHES (Cat 15)");
        assertEquals(9, result.getTargetDeptId(), "Expected LAUNDRY_SERVICES (Dept 9)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 45: Vendor charged more than MRP for stationery -> Commercial (Cat 16, Dept 10)")
    void testCase45_VendorOverpricingMrp() {
        IssuePredictionResult result = engine.predictIssue("A vendor charged more than the MRP for stationery.", testStudent);
        assertEquals(16, result.getCategoryId(), "Expected VENDOR_OVERPRICING (Cat 16)");
        assertEquals(10, result.getTargetDeptId(), "Expected COMMERCIAL_WELFARE (Dept 10)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 46: Campus road near north gate has pothole -> Roads & Grounds (Cat 14, Dept 8)")
    void testCase46_CampusRoadPothole() {
        IssuePredictionResult result = engine.predictIssue("The campus road near the north gate has a pothole.", testStudent);
        assertEquals(14, result.getCategoryId(), "Expected HORTICULTURE_ROADS (Cat 14)");
        assertEquals(8, result.getTargetDeptId(), "Expected HORTICULTURE_ESTATE (Dept 8)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 47: Nobody has replied to my application -> Ambiguous review, Triage DSA (Cat 31, Dept 13)")
    void testCase47_NobodyRepliedApplication() {
        IssuePredictionResult result = engine.predictIssue("Nobody has replied to my application.", testStudent);
        assertTrue(result.isReviewNeeded(), "Ambiguous grievance text must flag reviewNeeded");
        assertEquals(31, result.getCategoryId(), "Expected GENERAL_STUDENT_SERVICES triage (Cat 31)");
        assertEquals(13, result.getTargetDeptId(), "Expected STUDENT_AFFAIRS_SGRC triage (Dept 13)");
    }

    @Test
    @DisplayName("Case 48: Please fix this immediately -> Ambiguous review, no forced CLM")
    void testCase48_PleaseFixImmediately() {
        IssuePredictionResult result = engine.predictIssue("Please fix this immediately.", testStudent);
        assertTrue(result.isReviewNeeded(), "Vague imperative must flag reviewNeeded");
        assertEquals(31, result.getCategoryId());
        assertEquals(13, result.getTargetDeptId());
    }

    @Test
    @DisplayName("Case 49: Hostel fee payment failed due to bad internet -> Multi-intent review (Cat 21, Dept 16)")
    void testCase49_PaymentFailedBadInternet() {
        IssuePredictionResult result = engine.predictIssue("My hostel fee payment failed due to bad internet.", testStudent);
        assertEquals(21, result.getCategoryId(), "Fee payment failure routed to Finance");
        assertEquals(16, result.getTargetDeptId(), "Finance & Accounts");
        assertTrue(result.isReviewNeeded(), "Dual-intent payment/network requires review");
    }

    @Test
    @DisplayName("Case 50: Receipt for a water bill is wrong -> Utility bill review (Cat 21, Dept 16)")
    void testCase50_ReceiptWaterBillWrong() {
        IssuePredictionResult result = engine.predictIssue("The receipt for a water bill is wrong.", testStudent);
        assertEquals(21, result.getCategoryId());
        assertEquals(16, result.getTargetDeptId());
        assertTrue(result.isReviewNeeded());
    }

    @Test
    @DisplayName("Case 51: Broken bed in room dangerous to sleep on -> Furniture (Cat 13, Dept 1), Emergency = NO")
    void testCase51_BrokenBedDangerous() {
        IssuePredictionResult result = engine.predictIssue("A broken bed in my room is dangerous to sleep on.", testStudent);
        assertEquals(13, result.getCategoryId(), "Expected FURNITURE_CARPENTRY (Cat 13)");
        assertEquals(1, result.getTargetDeptId(), "Expected HOSTEL_MAINT (Dept 1)");
        assertFalse(result.isEmergency(), "Broken furniture is High priority, not an immediate critical emergency");
    }

    @Test
    @DisplayName("Case 52: Electrical shock from bathroom switch -> Electrical hazard, Emergency = YES")
    void testCase52_ElectricalShockSwitch() {
        IssuePredictionResult result = engine.predictIssue("There is an electrical shock from the bathroom switch.", testStudent);
        assertEquals(7, result.getCategoryId(), "Expected ELECTRICAL_REPAIR (Cat 7)");
        assertEquals(1, result.getTargetDeptId(), "Expected HOSTEL_MAINT (Dept 1)");
        assertTrue(result.isEmergency(), "Electrical shock hazard must be flagged Emergency");
    }

    @Test
    @DisplayName("Case 53: Writing a story about a fire in hostel -> NO INCIDENT, Emergency = NO")
    void testCase53_StoryAboutFire() {
        IssuePredictionResult result = engine.predictIssue("I am writing a story about a fire in a hostel.", testStudent);
        assertTrue(result.isNoIncident(), "Creative writing mention must be recognized as NO INCIDENT");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 54: Lecturer lectures clash with lab timetable -> Academic Affairs (Cat 29, Dept 20)")
    void testCase54_TimetableClash() {
        IssuePredictionResult result = engine.predictIssue("My lecturer's lectures clash with my lab timetable.", testStudent);
        assertEquals(29, result.getCategoryId(), "Expected ACADEMIC_ATTENDANCE_FACULTY (Cat 29)");
        assertEquals(20, result.getTargetDeptId(), "Expected ACADEMIC_AFFAIRS (Dept 20)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 55: Lost my phone on the bus -> Transport review (Cat 27, Dept 18)")
    void testCase55_LostPhoneOnBus() {
        IssuePredictionResult result = engine.predictIssue("I lost my phone on the bus.", testStudent);
        assertEquals(27, result.getCategoryId(), "Expected TRANSPORT_SERVICES (Cat 27)");
        assertEquals(18, result.getTargetDeptId(), "Expected TRANSPORT_SERVICES (Dept 18)");
        assertTrue(result.isReviewNeeded(), "Lost property on bus requires intake review");
    }

    @Test
    @DisplayName("Case 56: Contact DSA about repeated hostel bullying -> Safety Review / Emergency = YES")
    void testCase56_RepeatedHostelBullying() {
        IssuePredictionResult result = engine.predictIssue("Please contact the director of student affairs about my repeated hostel bullying.", testStudent);
        assertEquals(23, result.getCategoryId(), "Expected STUDENT_DISPUTES / Student Safety (Cat 23)");
        assertEquals(13, result.getTargetDeptId(), "Expected STUDENT_AFFAIRS_SGRC (Dept 13)");
        assertTrue(result.isEmergency(), "Repeated hostel bullying must trigger urgent safety escalation");
    }

    @Test
    @DisplayName("Case 57: Student portal isn't loading to access fees -> ITKM portal failure (Cat 6, Dept 5)")
    void testCase57_StudentPortalNotLoading() {
        IssuePredictionResult result = engine.predictIssue("My student portal isn't loading, and I can't access my fees.", testStudent);
        assertEquals(6, result.getCategoryId(), "Expected WI_FI_NETWORK (Cat 6)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 58: Student posted private photo without consent -> ICC Harassment, Emergency = YES")
    void testCase58_PrivatePhotoWithoutConsent() {
        IssuePredictionResult result = engine.predictIssue("A student posted my private photo without consent.", testStudent);
        assertEquals(18, result.getCategoryId(), "Expected CYBER_HARASSMENT (Cat 18)");
        assertEquals(11, result.getTargetDeptId(), "Expected INTERNAL_COMPLAINTS_COMMITTEE (Dept 11)");
        assertTrue(result.isEmergency(), "Non-consensual image sharing must trigger confidential emergency protection");
    }

    @Test
    @DisplayName("Case 59: Heard animal near hostel but couldn't see it -> Safety Review, Emergency = NO")
    void testCase59_HeardAnimalNearHostel() {
        IssuePredictionResult result = engine.predictIssue("I heard an animal near hostel but I couldn't see it.", testStudent);
        assertEquals(9, result.getCategoryId());
        assertEquals(6, result.getTargetDeptId());
        assertTrue(result.isReviewNeeded(), "Unconfirmed animal noise requires review without panic");
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 60: Wi-Fi isn't working in examination block -> ITKM Wi-Fi (Cat 6, Dept 5), not CoE")
    void testCase60_WifiInExaminationBlock() {
        IssuePredictionResult result = engine.predictIssue("The Wi-Fi isn't working in the examination block.", testStudent);
        assertEquals(6, result.getCategoryId(), "Expected WI_FI_NETWORK (Cat 6)");
        assertEquals(5, result.getTargetDeptId(), "Expected ITKM_HELPDESK (Dept 5)");
        assertFalse(result.isEmergency(), "Wi-Fi in examination block is network maintenance, not CoE grade issue");
    }

    // =========================================================================
    // SECTION 2: 20 Additional Contrasting, Edge & Negative Context Test Cases
    // =========================================================================

    @Test
    @DisplayName("Case 61: Discussed wild snakes during science lecture -> NO INCIDENT")
    void testCase61_DiscussedSnakesInLecture() {
        IssuePredictionResult result = engine.predictIssue("We discussed wild snakes during environmental science lecture.", testStudent);
        assertTrue(result.isNoIncident());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 62: Watched documentary about lions -> NO INCIDENT")
    void testCase62_WatchedDocumentaryLions() {
        IssuePredictionResult result = engine.predictIssue("I watched a documentary about lions in Africa.", testStudent);
        assertTrue(result.isNoIncident());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 63: Venomous cobra spotted crawling inside corridor -> Wildlife SOS, Emergency = YES")
    void testCase63_CobraCrawlingCorridor() {
        IssuePredictionResult result = engine.predictIssue("A venomous cobra was spotted crawling inside Ganga Tower corridor right now.", testStudent);
        assertEquals(9, result.getCategoryId());
        assertEquals(6, result.getTargetDeptId());
        assertTrue(result.isEmergency());
    }

    @Test
    @DisplayName("Case 64: Fire alarm beeping low battery -> Equipment maintenance, Emergency = NO")
    void testCase64_FireAlarmLowBattery() {
        IssuePredictionResult result = engine.predictIssue("The fire alarm in research lab is beeping low battery.", testStudent);
        assertEquals(10, result.getCategoryId());
        assertEquals(6, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 65: Flames and heavy smoke erupting from kitchen -> Fire SOS, Emergency = YES")
    void testCase65_FlamesSmokeKitchen() {
        IssuePredictionResult result = engine.predictIssue("Flames and heavy smoke erupting from canteen kitchen.", testStudent);
        assertEquals(10, result.getCategoryId());
        assertEquals(6, result.getTargetDeptId());
        assertTrue(result.isEmergency());
    }

    @Test
    @DisplayName("Case 66: Student in sports complex fainted and has chest pain -> Medical SOS, Emergency = YES")
    void testCase66_FaintedChestPain() {
        IssuePredictionResult result = engine.predictIssue("Student in sports complex fainted and has chest pain.", testStudent);
        assertEquals(11, result.getCategoryId());
        assertEquals(14, result.getTargetDeptId());
        assertTrue(result.isEmergency());
    }

    @Test
    @DisplayName("Case 67: Need doctor consultation for seasonal cold and cough -> Routine Clinic, Emergency = NO")
    void testCase67_ColdCoughConsultation() {
        IssuePredictionResult result = engine.predictIssue("Need a doctor consultation for seasonal cold and cough.", testStudent);
        assertEquals(25, result.getCategoryId());
        assertEquals(14, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 68: Movie about an earthquake and fire -> NO INCIDENT")
    void testCase68_MovieAboutFire() {
        IssuePredictionResult result = engine.predictIssue("We watched in a movie about an earthquake and fire yesterday.", testStudent);
        assertTrue(result.isNoIncident());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 69: Stalking and sending creepy messages on Telegram -> Cyber Harassment ICC, Emergency = YES")
    void testCase69_StalkingTelegramMessages() {
        IssuePredictionResult result = engine.predictIssue("Someone is stalking and sending creepy messages on Telegram.", testStudent);
        assertEquals(18, result.getCategoryId());
        assertEquals(11, result.getTargetDeptId());
        assertTrue(result.isEmergency());
    }

    @Test
    @DisplayName("Case 70: Internet speed test is very slow in hostel room -> ITKM Wi-Fi, Emergency = NO")
    void testCase70_InternetSpeedTestSlow() {
        IssuePredictionResult result = engine.predictIssue("Internet speed test is very slow in hostel room.", testStudent);
        assertEquals(6, result.getCategoryId());
        assertEquals(5, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 71: Can you help me please -> REVIEW ambiguous, Triage DSA")
    void testCase71_CanYouHelpMePlease() {
        IssuePredictionResult result = engine.predictIssue("Can you help me please?", testStudent);
        assertTrue(result.isReviewNeeded());
        assertEquals(31, result.getCategoryId());
        assertEquals(13, result.getTargetDeptId());
    }

    @Test
    @DisplayName("Case 72: Urgent assistance required -> REVIEW ambiguous, Triage DSA")
    void testCase72_UrgentAssistanceRequired() {
        IssuePredictionResult result = engine.predictIssue("Urgent assistance required.", testStudent);
        assertTrue(result.isReviewNeeded());
        assertEquals(31, result.getCategoryId());
        assertEquals(13, result.getTargetDeptId());
    }

    @Test
    @DisplayName("Case 73: Smart card not working at turnstile gate -> Lost/Card Access (Cat 24, Dept 5)")
    void testCase73_SmartCardNotWorkingGate() {
        IssuePredictionResult result = engine.predictIssue("My smart card not working at turnstile gate.", testStudent);
        assertEquals(24, result.getCategoryId());
        assertEquals(5, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 74: Central library fine charged for returned textbook -> Library Services (Cat 26, Dept 17)")
    void testCase74_LibraryFineDiscrepancy() {
        IssuePredictionResult result = engine.predictIssue("Central library fine charged for borrowed book.", testStudent);
        assertEquals(26, result.getCategoryId());
        assertEquals(17, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 75: College bus broke down near highway junction -> Transport (Cat 27, Dept 18)")
    void testCase75_BusBrokeDown() {
        IssuePredictionResult result = engine.predictIssue("College bus broke down near highway junction.", testStudent);
        assertEquals(27, result.getCategoryId());
        assertEquals(18, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 76: Placement drive registration link not opening on portal -> CRCS Placement (Cat 28, Dept 19)")
    void testCase76_PlacementDriveLink() {
        IssuePredictionResult result = engine.predictIssue("Placement drive registration link not opening on portal.", testStudent);
        assertEquals(28, result.getCategoryId());
        assertEquals(19, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 77: Midsem marksheet has missing grades -> CoE Examination (Cat 20, Dept 15)")
    void testCase77_MidsemMarksheetGrades() {
        IssuePredictionResult result = engine.predictIssue("Midsem marksheet has missing grade for math.", testStudent);
        assertEquals(20, result.getCategoryId());
        assertEquals(15, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 78: Pipe leaking heavily in 3rd floor washroom -> Water Plumbing (Cat 2, Dept 2)")
    void testCase78_PipeLeakingWashroom() {
        IssuePredictionResult result = engine.predictIssue("Water pipe leaking heavily in 3rd floor washroom.", testStudent);
        assertEquals(2, result.getCategoryId());
        assertEquals(2, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 79: Stale dal and uncooked rice served in mess -> Mess Food (Cat 4, Dept 3)")
    void testCase79_StaleDalMess() {
        IssuePredictionResult result = engine.predictIssue("Stale dal and uncooked rice served in hostel dining hall.", testStudent);
        assertEquals(4, result.getCategoryId());
        assertEquals(3, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }

    @Test
    @DisplayName("Case 80: Lab oscilloscope and rheostat are faulty -> Academic Labs (Cat 12, Dept 7)")
    void testCase80_LabOscilloscopeFaulty() {
        IssuePredictionResult result = engine.predictIssue("Physics lab oscilloscope and rheostat are faulty.", testStudent);
        assertEquals(12, result.getCategoryId());
        assertEquals(7, result.getTargetDeptId());
        assertFalse(result.isEmergency());
    }
}
