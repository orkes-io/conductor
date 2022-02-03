package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.sdk.task.OpParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class TestInputOutputMapping {


    @WorkflowTask("get_credit_scores")
    public CreditProcessingResult getCreditScores(CustomerInfo customerInfo) {
        CustomerInfo ci = null;
        return new CreditProcessingResult(customerInfo);
    }

    @WorkflowTask("get_loan_amount")
    public @OpParam("loanAmount") double getLoanAmount(int ficoScore) {
        return 1.0;
    }

    public void test() {


        SimpleTask<CustomerInfo, CreditProcessingResult> getCreditScores =
                new SimpleTask<>("fooBarTask", "fooBarTask");
        getCreditScores.input(
                "name", ConductorWorkflow.input.get("name"),
                "birthYear", ConductorWorkflow.input.get("birthYear"),
                "ssn", ConductorWorkflow.input.get("ssn")
        );

        SimpleTask<Integer, Double> getLoanAmount =
                new SimpleTask<>("get_loan_amount", "get_loan_amount");

        getLoanAmount.input(CreditProcessingResult::getFicoScore);

        ConductorWorkflow workflowDef = new WorkflowBuilder(null)
                .name("chain_task_input_outputs")
                .add(getCreditScores)
                .add(getLoanAmount)
                .build();
    }
}
