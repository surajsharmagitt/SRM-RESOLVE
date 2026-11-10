package com.srmresolve.model;

public class LocationCategoryHeatmap {
    private String buildingBlock;
    private LocationScope locationScope;
    private Integer categoryId;
    private String categoryCode;
    private String categoryName;
    private long ticketCount;
    private long openTicketCount;
    private long escalatedCount;
    private long uniqueAffectedStudents;

    public LocationCategoryHeatmap() {}

    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public LocationScope getLocationScope() { return locationScope; }
    public void setLocationScope(LocationScope locationScope) { this.locationScope = locationScope; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public long getTicketCount() { return ticketCount; }
    public void setTicketCount(long ticketCount) { this.ticketCount = ticketCount; }
    public long getOpenTicketCount() { return openTicketCount; }
    public void setOpenTicketCount(long openTicketCount) { this.openTicketCount = openTicketCount; }
    public long getEscalatedCount() { return escalatedCount; }
    public void setEscalatedCount(long escalatedCount) { this.escalatedCount = escalatedCount; }
    public long getUniqueAffectedStudents() { return uniqueAffectedStudents; }
    public void setUniqueAffectedStudents(long uniqueAffectedStudents) { this.uniqueAffectedStudents = uniqueAffectedStudents; }
}

