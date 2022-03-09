package com.netflix.conductor.sdk;

import java.util.HashMap;
import java.util.Map;

public class CreditProcessingResult {

    Map<String, Integer> scoreByReportingAgency = new HashMap<>();

    int ficoScore;

    private CustomerInfo customerInfo;

    public CreditProcessingResult() {}
    public CreditProcessingResult(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public Map<String, Integer> getScoreByReportingAgency() {
        return scoreByReportingAgency;
    }

    public void setScoreByReportingAgency(Map<String, Integer> scoreByReportingAgency) {
        this.scoreByReportingAgency = scoreByReportingAgency;
    }

    public int getFicoScore() {
        return ficoScore;
    }

    public void setFicoScore(int ficoScore) {
        this.ficoScore = ficoScore;
    }
}
