package com.srmresolve.model;

public class StudentDirectory {
    private String regNo;
    private String fullName;
    private String officialEmail;
    private int admissionYear;
    private int currentStudyYear;
    private String departmentCode;
    private String section;
    private boolean active;

    public StudentDirectory() {}

    public StudentDirectory(String regNo, String fullName, String officialEmail, int admissionYear,
                            int currentStudyYear, String departmentCode, String section, boolean active) {
        this.regNo = regNo;
        this.fullName = fullName;
        this.officialEmail = officialEmail;
        this.admissionYear = admissionYear;
        this.currentStudyYear = currentStudyYear;
        this.departmentCode = departmentCode;
        this.section = section;
        this.active = active;
    }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getOfficialEmail() { return officialEmail; }
    public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }
    public int getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(int admissionYear) { this.admissionYear = admissionYear; }
    public int getCurrentStudyYear() { return currentStudyYear; }
    public void setCurrentStudyYear(int currentStudyYear) { this.currentStudyYear = currentStudyYear; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

