package com.trohub.ui.api.models;

public class RegenerateInvoicesRequest {
    private Long tenantId;
    private Integer periodYear;
    private Integer periodMonth;

    public RegenerateInvoicesRequest() {
    }

    public RegenerateInvoicesRequest(Long tenantId, Integer periodYear, Integer periodMonth) {
        this.tenantId = tenantId;
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(Integer periodYear) {
        this.periodYear = periodYear;
    }

    public Integer getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(Integer periodMonth) {
        this.periodMonth = periodMonth;
    }
}
