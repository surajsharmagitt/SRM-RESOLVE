package com.srmresolve.dto;

import com.srmresolve.model.LocationScope;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public class CreateTicketRequest {

    private Integer categoryId;

    private LocationScope locationScope = LocationScope.HOSTEL;

    private String buildingBlock;

    private String roomOrLocation;

    @NotBlank(message = "Description is required")
    private String rawDescription;

    private String aiEnhancedDescription;

    private Long linkedMasterIncidentId; // Set if user clicks "I'm Affected Too"

    private MultipartFile attachment;

    public CreateTicketRequest() {}

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public LocationScope getLocationScope() { return locationScope; }
    public void setLocationScope(LocationScope locationScope) { this.locationScope = locationScope; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public String getRoomOrLocation() { return roomOrLocation; }
    public void setRoomOrLocation(String roomOrLocation) { this.roomOrLocation = roomOrLocation; }
    public String getRawDescription() { return rawDescription; }
    public void setRawDescription(String rawDescription) { this.rawDescription = rawDescription; }
    public String getAiEnhancedDescription() { return aiEnhancedDescription; }
    public void setAiEnhancedDescription(String aiEnhancedDescription) { this.aiEnhancedDescription = aiEnhancedDescription; }
    public Long getLinkedMasterIncidentId() { return linkedMasterIncidentId; }
    public void setLinkedMasterIncidentId(Long linkedMasterIncidentId) { this.linkedMasterIncidentId = linkedMasterIncidentId; }
    public MultipartFile getAttachment() { return attachment; }
    public void setAttachment(MultipartFile attachment) { this.attachment = attachment; }
}
