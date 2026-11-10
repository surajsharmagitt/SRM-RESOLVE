package com.srmresolve.model;

public class DepartmentSatisfactionMetric {
    private Integer deptId;
    private String deptCode;
    private String deptName;
    private Long totalClosed;
    private Double avgStudentRating;
    private Long positiveRatingsCount;
    private Long negativeRatingsCount;
    private Double avgFirstResponseMinutes;
    private Double avgTurnaroundMinutes;
    private Double slaResolutionCompliancePct;

    public DepartmentSatisfactionMetric() {}

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }

    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public Long getTotalClosed() { return totalClosed; }
    public void setTotalClosed(Long totalClosed) { this.totalClosed = totalClosed; }

    public Double getAvgStudentRating() { return avgStudentRating; }
    public void setAvgStudentRating(Double avgStudentRating) { this.avgStudentRating = avgStudentRating; }

    public Long getPositiveRatingsCount() { return positiveRatingsCount; }
    public void setPositiveRatingsCount(Long positiveRatingsCount) { this.positiveRatingsCount = positiveRatingsCount; }

    public Long getNegativeRatingsCount() { return negativeRatingsCount; }
    public void setNegativeRatingsCount(Long negativeRatingsCount) { this.negativeRatingsCount = negativeRatingsCount; }

    public Double getAvgFirstResponseMinutes() { return avgFirstResponseMinutes; }
    public void setAvgFirstResponseMinutes(Double avgFirstResponseMinutes) { this.avgFirstResponseMinutes = avgFirstResponseMinutes; }

    public Double getAvgTurnaroundMinutes() { return avgTurnaroundMinutes; }
    public void setAvgTurnaroundMinutes(Double avgTurnaroundMinutes) { this.avgTurnaroundMinutes = avgTurnaroundMinutes; }

    public Double getSlaResolutionCompliancePct() { return slaResolutionCompliancePct; }
    public void setSlaResolutionCompliancePct(Double slaResolutionCompliancePct) { this.slaResolutionCompliancePct = slaResolutionCompliancePct; }
}
