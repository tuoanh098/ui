package com.trohub.ui.api.models;

public class ReadingRequest {
    private String meterType;
    private String meterId;
    private Long tenantId;
    private Long readingValue;
    private Integer periodYear;
    private Integer periodMonth;
    private String recordedBy;

    public ReadingRequest() {}

    public ReadingRequest(String meterType, String meterId, Long tenantId, Long readingValue, Integer periodYear, Integer periodMonth, String recordedBy) {
        this.meterType = meterType;
        this.meterId = meterId;
        this.tenantId = tenantId;
        this.readingValue = readingValue;
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
        this.recordedBy = recordedBy;
    }

    public String getMeterType() { return meterType; }
    public void setMeterType(String meterType) { this.meterType = meterType; }
    public String getMeterId() { return meterId; }
    public void setMeterId(String meterId) { this.meterId = meterId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getReadingValue() { return readingValue; }
    public void setReadingValue(Long readingValue) { this.readingValue = readingValue; }
    public Integer getPeriodYear() { return periodYear; }
    public void setPeriodYear(Integer periodYear) { this.periodYear = periodYear; }
    public Integer getPeriodMonth() { return periodMonth; }
    public void setPeriodMonth(Integer periodMonth) { this.periodMonth = periodMonth; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}

