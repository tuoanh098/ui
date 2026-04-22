package com.trohub.ui.api.models;

public class PriceRequest {
    private String meterType;
    private Double pricePerUnit;
    private String effectiveFrom;
    private String effectiveTo;

    public PriceRequest() {}

    public PriceRequest(String meterType, Double pricePerUnit, String effectiveFrom, String effectiveTo) {
        this.meterType = meterType;
        this.pricePerUnit = pricePerUnit;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    public String getMeterType() { return meterType; }
    public void setMeterType(String meterType) { this.meterType = meterType; }
    public Double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(Double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public String getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(String effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public String getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(String effectiveTo) { this.effectiveTo = effectiveTo; }
}

