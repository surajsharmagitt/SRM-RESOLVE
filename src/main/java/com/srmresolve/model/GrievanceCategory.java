package com.srmresolve.model;

public class GrievanceCategory {
    private Integer categoryId;
    private String categoryCode;
    private String displayName;
    private String iconClass;
    private boolean sharedUtility;
    private Priority defaultPriority;
    private Integer monthlyThresholdLimit;

    public GrievanceCategory() {}

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
    public boolean isSharedUtility() { return sharedUtility; }
    public void setSharedUtility(boolean sharedUtility) { this.sharedUtility = sharedUtility; }
    public Priority getDefaultPriority() { return defaultPriority; }
    public void setDefaultPriority(Priority defaultPriority) { this.defaultPriority = defaultPriority; }
    public Integer getMonthlyThresholdLimit() { return monthlyThresholdLimit; }
    public void setMonthlyThresholdLimit(Integer monthlyThresholdLimit) { this.monthlyThresholdLimit = monthlyThresholdLimit; }
}

