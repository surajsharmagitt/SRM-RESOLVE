package com.srmresolve.model;

public class RoutingRule {
    private Integer ruleId;
    private Integer categoryId;
    private LocationScope locationScope;
    private Integer targetDeptId;

    public RoutingRule() {}

    public Integer getRuleId() { return ruleId; }
    public void setRuleId(Integer ruleId) { this.ruleId = ruleId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public LocationScope getLocationScope() { return locationScope; }
    public void setLocationScope(LocationScope locationScope) { this.locationScope = locationScope; }
    public Integer getTargetDeptId() { return targetDeptId; }
    public void setTargetDeptId(Integer targetDeptId) { this.targetDeptId = targetDeptId; }
}

