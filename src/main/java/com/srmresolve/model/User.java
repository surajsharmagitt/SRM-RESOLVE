package com.srmresolve.model;

import java.time.LocalDateTime;

public class User {
    private Long userId;
    private String regOrStaffId;
    private String fullName;
    private String email;
    private String passwordHash;
    private Role role;
    private VerificationStatus verificationStatus;
    private String departmentCode;
    private Integer studyYear;
    private String section;
    private ResidenceType residenceType;
    private String buildingBlock;
    private String roomOrZone;
    private Integer assignedDeptId;
    private LocalDateTime createdAt;

    public User() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRegOrStaffId() { return regOrStaffId; }
    public void setRegOrStaffId(String regOrStaffId) { this.regOrStaffId = regOrStaffId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public Integer getStudyYear() { return studyYear; }
    public void setStudyYear(Integer studyYear) { this.studyYear = studyYear; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public ResidenceType getResidenceType() { return residenceType; }
    public void setResidenceType(ResidenceType residenceType) { this.residenceType = residenceType; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public String getRoomOrZone() { return roomOrZone; }
    public void setRoomOrZone(String roomOrZone) { this.roomOrZone = roomOrZone; }
    public Integer getAssignedDeptId() { return assignedDeptId; }
    public void setAssignedDeptId(Integer assignedDeptId) { this.assignedDeptId = assignedDeptId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

