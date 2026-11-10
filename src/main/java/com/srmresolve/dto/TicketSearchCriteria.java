package com.srmresolve.dto;

import com.srmresolve.model.LocationScope;
import com.srmresolve.model.Priority;
import com.srmresolve.model.TicketStatus;

import java.time.LocalDateTime;

public class TicketSearchCriteria {
    private String keyword;
    private Integer deptId;
    private Integer categoryId;
    private TicketStatus status;
    private Priority priority;
    private Boolean escalatedOnly;
    private Boolean overdueOnly;
    private String buildingBlock;
    private LocationScope locationScope;
    private Long studentUserId;
    private Long staffUserId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Integer limit = 50;
    private Integer offset = 0;

    public TicketSearchCriteria() {}

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Boolean getEscalatedOnly() { return escalatedOnly; }
    public void setEscalatedOnly(Boolean escalatedOnly) { this.escalatedOnly = escalatedOnly; }

    public Boolean getOverdueOnly() { return overdueOnly; }
    public void setOverdueOnly(Boolean overdueOnly) { this.overdueOnly = overdueOnly; }

    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }

    public LocationScope getLocationScope() { return locationScope; }
    public void setLocationScope(LocationScope locationScope) { this.locationScope = locationScope; }

    public Long getStudentUserId() { return studentUserId; }
    public void setStudentUserId(Long studentUserId) { this.studentUserId = studentUserId; }

    public Long getStaffUserId() { return staffUserId; }
    public void setStaffUserId(Long staffUserId) { this.staffUserId = staffUserId; }

    public LocalDateTime getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDateTime dateFrom) { this.dateFrom = dateFrom; }

    public LocalDateTime getDateTo() { return dateTo; }
    public void setDateTo(LocalDateTime dateTo) { this.dateTo = dateTo; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
}
