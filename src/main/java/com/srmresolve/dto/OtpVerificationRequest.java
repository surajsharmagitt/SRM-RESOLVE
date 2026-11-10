package com.srmresolve.dto;

import com.srmresolve.model.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class OtpVerificationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Registration number is required")
    private String regNo;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit number")
    private String otp;

    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose = OtpPurpose.REGISTRATION;

    public OtpVerificationRequest() {}

    public OtpVerificationRequest(String email, String regNo, String otp, OtpPurpose purpose) {
        this.email = email;
        this.regNo = regNo;
        this.otp = otp;
        this.purpose = purpose;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public OtpPurpose getPurpose() { return purpose; }
    public void setPurpose(OtpPurpose purpose) { this.purpose = purpose; }
}

