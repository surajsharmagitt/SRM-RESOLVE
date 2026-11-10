package com.srmresolve.model;

import java.time.LocalDateTime;

public class EmailOtpVerification {
    private Long otpId;
    private String email;
    private String regNo;
    private String otpHash;
    private OtpPurpose purpose;
    private int attemptCount;
    private int maxAttempts;
    private LocalDateTime resendAvailableAt;
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;

    public EmailOtpVerification() {}

    public Long getOtpId() { return otpId; }
    public void setOtpId(Long otpId) { this.otpId = otpId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }
    public OtpPurpose getPurpose() { return purpose; }
    public void setPurpose(OtpPurpose purpose) { this.purpose = purpose; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public LocalDateTime getResendAvailableAt() { return resendAvailableAt; }
    public void setResendAvailableAt(LocalDateTime resendAvailableAt) { this.resendAvailableAt = resendAvailableAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

