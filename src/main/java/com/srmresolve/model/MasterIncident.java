package com.srmresolve.model;

import java.time.LocalDateTime;

public class MasterIncident {
    private Long masterIncidentId;
    private Integer categoryId;
    private Integer assignedDeptId;
    private String buildingBlock;
    private String incidentTitle;
    private String normalizedTokens;
    private MasterIncidentStatus incidentStatus;
    private int affectedStudentCount;
    private LocalDateTime etaTimestamp;
    private boolean syntheticDemo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient display fields
    private String categoryDisplayName;
    private String departmentName;

    public MasterIncident() {}

    public Long getMasterIncidentId() { return masterIncidentId; }
    public void setMasterIncidentId(Long masterIncidentId) { this.masterIncidentId = masterIncidentId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Integer getAssignedDeptId() { return assignedDeptId; }
    public void setAssignedDeptId(Integer assignedDeptId) { this.assignedDeptId = assignedDeptId; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public String getIncidentTitle() { return incidentTitle; }
    public void setIncidentTitle(String incidentTitle) { this.incidentTitle = incidentTitle; }
    public String getNormalizedTokens() { return normalizedTokens; }
    public void setNormalizedTokens(String normalizedTokens) { this.normalizedTokens = normalizedTokens; }
    public MasterIncidentStatus getIncidentStatus() { return incidentStatus; }
    public void setIncidentStatus(MasterIncidentStatus incidentStatus) { this.incidentStatus = incidentStatus; }
    public int getAffectedStudentCount() { return affectedStudentCount; }
    public void setAffectedStudentCount(int affectedStudentCount) { this.affectedStudentCount = affectedStudentCount; }
    public LocalDateTime getEtaTimestamp() { return etaTimestamp; }
    public void setEtaTimestamp(LocalDateTime etaTimestamp) { this.etaTimestamp = etaTimestamp; }
    public boolean isSyntheticDemo() { return syntheticDemo; }
    public void setSyntheticDemo(boolean syntheticDemo) { this.syntheticDemo = syntheticDemo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCategoryDisplayName() { return categoryDisplayName; }
    public void setCategoryDisplayName(String categoryDisplayName) { this.categoryDisplayName = categoryDisplayName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}

