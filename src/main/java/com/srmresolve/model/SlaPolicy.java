package com.srmresolve.model;

public class SlaPolicy {
    private Integer slaId;
    private Integer categoryId;
    private Priority priority;
    private int ackDeadlineMinutes;
    private int resolutionDeadlineMinutes;
    private String level1EscalateToRole;
    private String level2EscalateToRole;

    public SlaPolicy() {}

    public Integer getSlaId() { return slaId; }
    public void setSlaId(Integer slaId) { this.slaId = slaId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public int getAckDeadlineMinutes() { return ackDeadlineMinutes; }
    public void setAckDeadlineMinutes(int ackDeadlineMinutes) { this.ackDeadlineMinutes = ackDeadlineMinutes; }
    public int getResolutionDeadlineMinutes() { return resolutionDeadlineMinutes; }
    public void setResolutionDeadlineMinutes(int resolutionDeadlineMinutes) { this.resolutionDeadlineMinutes = resolutionDeadlineMinutes; }
    public String getLevel1EscalateToRole() { return level1EscalateToRole; }
    public void setLevel1EscalateToRole(String level1EscalateToRole) { this.level1EscalateToRole = level1EscalateToRole; }
    public String getLevel2EscalateToRole() { return level2EscalateToRole; }
    public void setLevel2EscalateToRole(String level2EscalateToRole) { this.level2EscalateToRole = level2EscalateToRole; }
}

