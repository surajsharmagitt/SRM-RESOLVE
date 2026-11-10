package com.srmresolve.dto;

import jakarta.validation.constraints.NotBlank;

public class AiEnhanceRequest {

    @NotBlank(message = "Category display name is required")
    private String categoryDisplayName;

    @NotBlank(message = "Building block is required")
    private String buildingBlock;

    @NotBlank(message = "Room or location is required")
    private String roomOrLocation;

    @NotBlank(message = "Raw student text is required")
    private String rawStudentText;

    public AiEnhanceRequest() {}

    public AiEnhanceRequest(String categoryDisplayName, String buildingBlock, String roomOrLocation, String rawStudentText) {
        this.categoryDisplayName = categoryDisplayName;
        this.buildingBlock = buildingBlock;
        this.roomOrLocation = roomOrLocation;
        this.rawStudentText = rawStudentText;
    }

    public String getCategoryDisplayName() { return categoryDisplayName; }
    public void setCategoryDisplayName(String categoryDisplayName) { this.categoryDisplayName = categoryDisplayName; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public String getRoomOrLocation() { return roomOrLocation; }
    public void setRoomOrLocation(String roomOrLocation) { this.roomOrLocation = roomOrLocation; }
    public String getRawStudentText() { return rawStudentText; }
    public void setRawStudentText(String rawStudentText) { this.rawStudentText = rawStudentText; }
}

