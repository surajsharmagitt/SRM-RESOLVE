package com.srmresolve.service;

import com.srmresolve.dao.GrievanceTicketDao;
import com.srmresolve.dao.TicketLifecycleDao;
import com.srmresolve.model.GrievanceTicket;
import com.srmresolve.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Two-Way Resolution Handshake & Lifecycle Tests")
class ResolutionHandshakeTest {

    private GrievanceTicketDao ticketDao;
    private TicketLifecycleDao lifecycleDao;
    private ResolutionHandshakeService handshakeService;

    @BeforeEach
    void setUp() {
        ticketDao = Mockito.mock(GrievanceTicketDao.class);
        lifecycleDao = Mockito.mock(TicketLifecycleDao.class);
        handshakeService = new ResolutionHandshakeService(ticketDao, lifecycleDao);
    }

    @Test
    @DisplayName("1. Acknowledge: SUBMITTED -> ACKNOWLEDGED records timestamp and assigned staff")
    void testAcknowledgeTicket() {
        GrievanceTicket ticket = new GrievanceTicket();
        ticket.setTicketId(101L);
        ticket.setStatus(TicketStatus.SUBMITTED);
        when(ticketDao.findById(101L)).thenReturn(Optional.of(ticket));

        handshakeService.acknowledgeTicket(101L, 10L, "Staff assigned");

        verify(ticketDao).recordAcknowledgment(eq(101L), eq(10L), any(LocalDateTime.class));
        verify(lifecycleDao).insertStatusHistory(eq(101L), eq("SUBMITTED"), eq("ACKNOWLEDGED"), eq(10L), anyString());
    }

    @Test
    @DisplayName("2. Student Confirmation: AWAITING_STUDENT_CONFIRMATION -> CLOSED when student confirms fixed")
    void testConfirmFixedByStudent() {
        GrievanceTicket ticket = new GrievanceTicket();
        ticket.setTicketId(102L);
        ticket.setStudentUserId(18L);
        ticket.setStatus(TicketStatus.AWAITING_STUDENT_CONFIRMATION);
        when(ticketDao.findById(102L)).thenReturn(Optional.of(ticket));

        handshakeService.confirmFixedByStudent(102L, 18L, "Working great now!");

        verify(ticketDao).recordClosureByStudent(eq(102L), any(LocalDateTime.class));
        verify(lifecycleDao).insertStatusHistory(eq(102L), eq("AWAITING_STUDENT_CONFIRMATION"), eq("CLOSED"), eq(18L), anyString());
    }

    @Test
    @DisplayName("3. Security Isolation: Non-owning student cannot confirm or reopen another student's ticket")
    void testNonOwningStudentCannotConfirm() {
        GrievanceTicket ticket = new GrievanceTicket();
        ticket.setTicketId(103L);
        ticket.setStudentUserId(18L); // Owning student
        ticket.setStatus(TicketStatus.AWAITING_STUDENT_CONFIRMATION);
        when(ticketDao.findById(103L)).thenReturn(Optional.of(ticket));

        // Attempt by student 99L (not owner)
        assertThrows(SecurityException.class, () ->
                handshakeService.confirmFixedByStudent(103L, 99L, "Hack attempt"));

        assertThrows(SecurityException.class, () ->
                handshakeService.rejectFixAndReopen(103L, 99L, "Hack attempt"));
    }

    @Test
    @DisplayName("4. Two-Way Reopen: Student rejection transitions to REOPENED with rejection reason")
    void testRejectFixAndReopen() {
        GrievanceTicket ticket = new GrievanceTicket();
        ticket.setTicketId(104L);
        ticket.setStudentUserId(18L);
        ticket.setStatus(TicketStatus.AWAITING_STUDENT_CONFIRMATION);
        ticket.setReopenCount(0);
        when(ticketDao.findById(104L)).thenReturn(Optional.of(ticket));

        handshakeService.rejectFixAndReopen(104L, 18L, "Water pressure still too low.");

        verify(ticketDao).recordReopenedByStudent(eq(104L), any(LocalDateTime.class));
        verify(lifecycleDao).insertStatusHistory(eq(104L), eq("AWAITING_STUDENT_CONFIRMATION"), eq("REOPENED"), eq(18L), anyString());
        verify(lifecycleDao).insertMessage(eq(104L), eq(18L), eq("ROLE_STUDENT"), contains("Still Not Fixed"), eq(false));
    }
}

