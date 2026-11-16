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

    @Test
    @DisplayName("5. Status Workflow Integrity: Invalid transitions are rejected with IllegalStateException")
    void testInvalidStatusTransitions() {
        // Ticket in CLOSED state cannot be acknowledged
        GrievanceTicket closedTicket = new GrievanceTicket();
        closedTicket.setTicketId(201L);
        closedTicket.setStatus(TicketStatus.CLOSED);
        when(ticketDao.findById(201L)).thenReturn(Optional.of(closedTicket));

        assertThrows(IllegalStateException.class, () ->
                handshakeService.acknowledgeTicket(201L, 10L, "Cannot ack closed ticket"));

        // Ticket in SUBMITTED state cannot be moved to IN_PROGRESS (must be ACKNOWLEDGED first)
        GrievanceTicket submittedTicket = new GrievanceTicket();
        submittedTicket.setTicketId(202L);
        submittedTicket.setStudentUserId(18L);
        submittedTicket.setStatus(TicketStatus.SUBMITTED);
        when(ticketDao.findById(202L)).thenReturn(Optional.of(submittedTicket));

        assertThrows(IllegalStateException.class, () ->
                handshakeService.setEtaAndProgress(202L, 10L, LocalDateTime.now().plusHours(2), "Cannot skip ack"));

        // Ticket in ACKNOWLEDGED state cannot be marked resolved (must be IN_PROGRESS first)
        GrievanceTicket ackTicket = new GrievanceTicket();
        ackTicket.setTicketId(203L);
        ackTicket.setStatus(TicketStatus.ACKNOWLEDGED);
        when(ticketDao.findById(203L)).thenReturn(Optional.of(ackTicket));

        assertThrows(IllegalStateException.class, () ->
                handshakeService.markResolvedByDept(203L, 10L, "Cannot resolve unstarted work"));

        // Ticket not in AWAITING_STUDENT_CONFIRMATION cannot be confirmed fixed by student
        assertThrows(IllegalStateException.class, () ->
                handshakeService.confirmFixedByStudent(202L, 18L, "Cannot confirm before resolution"));

        // Ticket not in AWAITING_STUDENT_CONFIRMATION cannot be reopened by student
        assertThrows(IllegalStateException.class, () ->
                handshakeService.rejectFixAndReopen(202L, 18L, "Cannot reopen unreviewed ticket"));
    }

    @Test
    @DisplayName("6. Complete Valid Complaint Flow: SUBMITTED -> ACKNOWLEDGED -> IN_PROGRESS -> AWAITING -> CLOSED")
    void testCompleteValidComplaintWorkflow() {
        GrievanceTicket ticket = new GrievanceTicket();
        ticket.setTicketId(301L);
        ticket.setStudentUserId(18L);
        ticket.setStatus(TicketStatus.SUBMITTED);
        when(ticketDao.findById(301L)).thenReturn(Optional.of(ticket));

        // 1. Acknowledge
        handshakeService.acknowledgeTicket(301L, 10L, "Acknowledged");
        ticket.setStatus(TicketStatus.ACKNOWLEDGED);

        // 2. Set ETA & In Progress
        handshakeService.setEtaAndProgress(301L, 10L, LocalDateTime.now().plusHours(4), "In progress");
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        // 3. Mark Resolved by Dept
        handshakeService.markResolvedByDept(301L, 10L, "Completed repair");
        ticket.setStatus(TicketStatus.AWAITING_STUDENT_CONFIRMATION);

        // 4. Student Confirms Fixed
        handshakeService.confirmFixedByStudent(301L, 18L, 5, "Excellent repair");
        verify(ticketDao).recordClosureByStudent(eq(301L), any(LocalDateTime.class), eq(5), anyString());
    }
}

