package com.srmresolve.model;

import java.time.LocalDateTime;

public class Department {
    private Integer deptId;
    private String deptCode;
    private String deptName;
    private String contactEmail;
    private Integer monthlyAlertThreshold;
    private LocalDateTime createdAt;

    public Department() {}

    public Department(Integer deptId, String deptCode, String deptName, String contactEmail, Integer monthlyAlertThreshold, LocalDateTime createdAt) {
        this.deptId = deptId;
        this.deptCode = deptCode;
        this.deptName = deptName;
        this.contactEmail = contactEmail;
        this.monthlyAlertThreshold = monthlyAlertThreshold;
        this.createdAt = createdAt;
    }

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public Integer getMonthlyAlertThreshold() { return monthlyAlertThreshold; }
    public void setMonthlyAlertThreshold(Integer monthlyAlertThreshold) { this.monthlyAlertThreshold = monthlyAlertThreshold; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

