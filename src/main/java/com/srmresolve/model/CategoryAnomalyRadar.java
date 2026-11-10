package com.srmresolve.model;

public class CategoryAnomalyRadar {
    private Integer categoryId;
    private String categoryCode;
    private String displayName;
    private Integer monthlyThresholdLimit;
    private long totalStudentReports;
    private long groupedMasterIncidents;
    private long independentIncidents;
    private long totalUnderlyingIncidents;
    private long closedReports;
    private long escalatedReports;
    private Double avgResolutionMinutes;
    private boolean thresholdBreached;

    public CategoryAnomalyRadar() {}

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Integer getMonthlyThresholdLimit() { return monthlyThresholdLimit; }
    public void setMonthlyThresholdLimit(Integer monthlyThresholdLimit) { this.monthlyThresholdLimit = monthlyThresholdLimit; }
    public long getTotalStudentReports() { return totalStudentReports; }
    public void setTotalStudentReports(long totalStudentReports) { this.totalStudentReports = totalStudentReports; }
    public long getGroupedMasterIncidents() { return groupedMasterIncidents; }
    public void setGroupedMasterIncidents(long groupedMasterIncidents) { this.groupedMasterIncidents = groupedMasterIncidents; }
    public long getIndependentIncidents() { return independentIncidents; }
    public void setIndependentIncidents(long independentIncidents) { this.independentIncidents = independentIncidents; }
    public long getTotalUnderlyingIncidents() { return totalUnderlyingIncidents; }
    public void setTotalUnderlyingIncidents(long totalUnderlyingIncidents) { this.totalUnderlyingIncidents = totalUnderlyingIncidents; }
    public long getClosedReports() { return closedReports; }
    public void setClosedReports(long closedReports) { this.closedReports = closedReports; }
    public long getEscalatedReports() { return escalatedReports; }
    public void setEscalatedReports(long escalatedReports) { this.escalatedReports = escalatedReports; }
    public Double getAvgResolutionMinutes() { return avgResolutionMinutes; }
    public void setAvgResolutionMinutes(Double avgResolutionMinutes) { this.avgResolutionMinutes = avgResolutionMinutes; }
    public boolean isThresholdBreached() { return thresholdBreached; }
    public void setThresholdBreached(boolean thresholdBreached) { this.thresholdBreached = thresholdBreached; }
}

