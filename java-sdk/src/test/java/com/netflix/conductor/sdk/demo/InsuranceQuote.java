package com.netflix.conductor.sdk.demo;

public class InsuranceQuote {

    private String name;

    private String industryCode;

    private double insuranceAmount;

    private int approvedAmount;

    private double rate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public double getInsuranceAmount() {
        return insuranceAmount;
    }

    public void setInsuranceAmount(double insuranceAmount) {
        this.insuranceAmount = insuranceAmount;
    }

    public int getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(int approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
