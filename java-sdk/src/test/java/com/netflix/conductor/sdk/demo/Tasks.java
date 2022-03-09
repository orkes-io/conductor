package com.netflix.conductor.sdk.demo;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;

public class Tasks {

    @WorkflowTask("getIndustryMultiplier")
    public @OutputParam("multiplier") double getIndustryMultiplier(@InputParam("industryCode") String industryCode) {
        return 0.9;
    }

    @WorkflowTask("getAdjustedAmt")
    public @OutputParam("adjusted_amount") double getAdjustedAmt(@InputParam("multiplier") double multiplier, @InputParam("amount") double amount) {
        return amount * multiplier;
    }

    @WorkflowTask("generateQuote")
    public InsuranceQuote getAdjustedAmt(@InputParam("adjusted_amount") double amount) {
        InsuranceQuote quote = new InsuranceQuote();
        quote.setInsuranceAmount(amount);
        return quote;
    }

    @WorkflowTask("updateQuote")
    public InsuranceQuote updateQuote(InsuranceQuote quote) {
        quote.setRate(1.1);
        quote.setInsuranceAmount(quote.getInsuranceAmount() * quote.getRate());
        return quote;
    }
}
