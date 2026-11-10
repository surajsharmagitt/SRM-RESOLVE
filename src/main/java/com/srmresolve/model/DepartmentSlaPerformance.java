package com.srmresolve.model;

public class DepartmentSlaPerformance {
    private Integer deptId;
    private String deptCode;
    private String deptName;
    private long totalAssignedTickets;
    private long totalClosedTickets;
    private long totalReopenedTickets;
    private long ackSlaMetCount;
    private long ackSlaBreachedCount;
    private long resolutionSlaMetCount;
    private long resolutionSlaBreachedCount;
    private long currentlyOverdueCount;
    private long escalatedTicketCount;
    private Double avgClosureMinutes;

    public DepartmentSlaPerformance() {}

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public long getTotalAssignedTickets() { return totalAssignedTickets; }
    public void setTotalAssignedTickets(long totalAssignedTickets) { this.totalAssignedTickets = totalAssignedTickets; }
    public long getTotalClosedTickets() { return totalClosedTickets; }
    public void setTotalClosedTickets(long totalClosedTickets) { this.totalClosedTickets = totalClosedTickets; }
    public long getTotalReopenedTickets() { return totalReopenedTickets; }
    public void setTotalReopenedTickets(long totalReopenedTickets) { this.totalReopenedTickets = totalReopenedTickets; }
    public long getAckSlaMetCount() { return ackSlaMetCount; }
    public void setAckSlaMetCount(long ackSlaMetCount) { this.ackSlaMetCount = ackSlaMetCount; }
    public long getAckSlaBreachedCount() { return ackSlaBreachedCount; }
    public void setAckSlaBreachedCount(long ackSlaBreachedCount) { this.ackSlaBreachedCount = ackSlaBreachedCount; }
    public long getResolutionSlaMetCount() { return resolutionSlaMetCount; }
    public void setResolutionSlaMetCount(long resolutionSlaMetCount) { this.resolutionSlaMetCount = resolutionSlaMetCount; }
    public long getResolutionSlaBreachedCount() { return resolutionSlaBreachedCount; }
    public void setResolutionSlaBreachedCount(long resolutionSlaBreachedCount) { this.resolutionSlaBreachedCount = resolutionSlaBreachedCount; }
    public long getCurrentlyOverdueCount() { return currentlyOverdueCount; }
    public void setCurrentlyOverdueCount(long currentlyOverdueCount) { this.currentlyOverdueCount = currentlyOverdueCount; }
    public long getEscalatedTicketCount() { return escalatedTicketCount; }
    public void setEscalatedTicketCount(long escalatedTicketCount) { this.escalatedTicketCount = escalatedTicketCount; }
    public Double getAvgClosureMinutes() { return avgClosureMinutes; }
    public void setAvgClosureMinutes(Double avgClosureMinutes) { this.avgClosureMinutes = avgClosureMinutes; }
}

