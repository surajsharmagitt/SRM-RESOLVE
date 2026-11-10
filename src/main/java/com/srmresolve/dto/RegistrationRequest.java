package com.srmresolve.dto;

import com.srmresolve.model.ResidenceType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {

    @NotBlank(message = "Registration number is required")
    private String regNo;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Residence type is required")
    private ResidenceType residenceType = ResidenceType.HOSTELLER;

    private String buildingBlock;
    private String roomOrZone;

    // Optional override if not in student directory
    private String departmentCode;
    private Integer studyYear;
    private String section;

    public RegistrationRequest() {}

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public ResidenceType getResidenceType() { return residenceType; }
    public void setResidenceType(ResidenceType residenceType) { this.residenceType = residenceType; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public String getRoomOrZone() { return roomOrZone; }
    public void setRoomOrZone(String roomOrZone) { this.roomOrZone = roomOrZone; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public Integer getStudyYear() { return studyYear; }
    public void setStudyYear(Integer studyYear) { this.studyYear = studyYear; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}

