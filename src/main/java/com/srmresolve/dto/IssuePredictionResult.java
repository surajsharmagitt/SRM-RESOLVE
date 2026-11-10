package com.srmresolve.dto;

import com.srmresolve.model.LocationScope;
import com.srmresolve.model.Priority;

/**
 * Result returned by the CampusIntelligenceEngine when analyzing a student's problem text.
 * Contains predicted category, target department, priority, emergency flag, SLA deadlines,
 * and extracted location entity.
 */
public class IssuePredictionResult {

    private Integer categoryId;
    private String categoryCode;
    private String categoryDisplayName;
    private String categoryIcon;

    private Integer targetDeptId;
    private String targetDeptCode;
    private String targetDeptName;

    private Priority priority;
    private LocationScope locationScope;

    private String buildingBlock;
    private String roomOrLocation;

    private boolean emergency;
    private String emergencyActionNotice;

    private int ackMinutes;
    private int resolutionMinutes;

    private double confidenceScore;
    private String intelligenceReasoning;

    public IssuePredictionResult() {}

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getCategoryDisplayName() { return categoryDisplayName; }
    public void setCategoryDisplayName(String categoryDisplayName) { this.categoryDisplayName = categoryDisplayName; }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public Integer getTargetDeptId() { return targetDeptId; }
    public void setTargetDeptId(Integer targetDeptId) { this.targetDeptId = targetDeptId; }

    public String getTargetDeptCode() { return targetDeptCode; }
    public void setTargetDeptCode(String targetDeptCode) { this.targetDeptCode = targetDeptCode; }

    public String getTargetDeptName() { return targetDeptName; }
    public void setTargetDeptName(String targetDeptName) { this.targetDeptName = targetDeptName; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocationScope getLocationScope() { return locationScope; }
    public void setLocationScope(LocationScope locationScope) { this.locationScope = locationScope; }

    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }

    public String getRoomOrLocation() { return roomOrLocation; }
    public void setRoomOrLocation(String roomOrLocation) { this.roomOrLocation = roomOrLocation; }

    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }

    public String getEmergencyActionNotice() { return emergencyActionNotice; }
    public void setEmergencyActionNotice(String emergencyActionNotice) { this.emergencyActionNotice = emergencyActionNotice; }

    public int getAckMinutes() { return ackMinutes; }
    public void setAckMinutes(int ackMinutes) { this.ackMinutes = ackMinutes; }

    public int getResolutionMinutes() { return resolutionMinutes; }
    public void setResolutionMinutes(int resolutionMinutes) { this.resolutionMinutes = resolutionMinutes; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getIntelligenceReasoning() { return intelligenceReasoning; }
    public void setIntelligenceReasoning(String intelligenceReasoning) { this.intelligenceReasoning = intelligenceReasoning; }
}

