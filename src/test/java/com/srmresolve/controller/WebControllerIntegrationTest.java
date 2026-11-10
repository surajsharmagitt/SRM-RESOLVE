package com.srmresolve.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Spring MVC Controllers & Thymeleaf Template Integration Tests")
class WebControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. Unauthenticated request to /student/dashboard should redirect to /student-login")
    void testUnauthenticatedRedirect() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student-login"));
    }

    @Test
    @DisplayName("2. Auth Login page should render cleanly")
    void testLoginPageRender() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SRM-RESOLVE")))
                .andExpect(content().string(containsString("Sign In")));
    }

    @Test
    @WithMockUser(username = "suraj_sharma@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("3. Student Dashboard should render for ROLE_STUDENT with category chips and tickets")
    void testStudentDashboard() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Suraj Sharma")))
                .andExpect(content().string(containsString("File New Grievance")))
                .andExpect(content().string(containsString("Enhance with AI")));
    }

    @Test
    @WithMockUser(username = "water.staff1@srmap.edu.in", roles = {"DEPT_STAFF"})
    @DisplayName("4. Department Hub should render for ROLE_DEPT_STAFF with split-pane workspace")
    void testDepartmentHub() throws Exception {
        mockMvc.perform(get("/department/hub"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Department Hub")))
                .andExpect(content().string(containsString("Water, RO Plant")));
    }

    @Test
    @WithMockUser(username = "water.head@srmap.edu.in", roles = {"DEPT_HEAD"})
    @DisplayName("5. Department Head Overview should render for ROLE_DEPT_HEAD with SLA performance cards")
    void testDepartmentHeadDashboard() throws Exception {
        mockMvc.perform(get("/department/head"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Head Dashboard")))
                .andExpect(content().string(containsString("Level 1 SLA Escalations")));
    }

    @Test
    @WithMockUser(username = "vc@srmap.edu.in", roles = {"EXECUTIVE"})
    @DisplayName("6. Executive VC Dashboard should render for ROLE_EXECUTIVE with Anomaly Radar")
    void testVcDashboard() throws Exception {
        mockMvc.perform(get("/executive/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Executive Leadership Anomaly Radar")))
                .andExpect(content().string(containsString("Incident Grouping Impact")))
                .andExpect(content().string(containsString("Department SLA Compliance")));
    }

    @Test
    @DisplayName("7. AI Enhancer API should accept raw text and return structured statement without hallucinating")
    void testAiEnhanceApi() throws Exception {
        String json = """
            {
                "categoryDisplayName": "Water Supply Cut",
                "buildingBlock": "Ganga Tower",
                "roomOrLocation": "Room 412",
                "rawStudentText": "no water since 7am taps are completely dry"
            }
            """;

        mockMvc.perform(post("/api/enhance-ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enhancedText").value(containsString("Ganga Tower")))
                .andExpect(jsonPath("$.enhancedText").value(containsString("Room 412")))
                .andExpect(jsonPath("$.enhancedText").value(containsString("dry.")));
    }

    @Test
    @DisplayName("8. Welcome page should render CampusFix styling without demo banner")
    void testWelcomePage() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Campus Infrastructure")))
                .andExpect(content().string(containsString("Student Login")));
    }

    @Test
    @DisplayName("9. Student Login page should render CampusFix login form")
    void testStudentLoginPage() throws Exception {
        mockMvc.perform(get("/student-login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Student Login")))
                .andExpect(content().string(containsString("Send OTP")));
    }

    @Test
    @WithMockUser(username = "suraj_sharma@srmap.edu.in", roles = {"STUDENT"})
    @DisplayName("10. Student complete profile onboarding should render")
    void testCompleteProfilePage() throws Exception {
        mockMvc.perform(get("/student/complete-profile"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Institutional Student Profile")))
                .andExpect(content().string(containsString("SRM Registration Number")));
    }
}
