package com.netflix.conductor.sdk;


import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;

public class TestInputOutputMapping {


    @WorkflowTask("get_credit_scores")
    public CreditProcessingResult getCreditScores(CustomerInfo customerInfo) {
        CustomerInfo ci = null;
        return new CreditProcessingResult(customerInfo);
    }

    @WorkflowTask("get_loan_amount")
    public @OutputParam("loanAmount") double getLoanAmount(int ficoScore) {
        return 1.0;
    }

    public void test() {


        SimpleTask getCreditScores =
                new SimpleTask("fooBarTask", "fooBarTask");
        getCreditScores.input(
                "name", ConductorWorkflow.input.get("name"),
                "birthYear", ConductorWorkflow.input.get("birthYear"),
                "ssn", ConductorWorkflow.input.get("ssn")
        );

        SimpleTask getLoanAmount =
                new SimpleTask("get_loan_amount", "get_loan_amount");

        ConductorWorkflow workflowDef = new WorkflowBuilder(null)
                .name("chain_task_input_outputs")
                .add(getCreditScores)
                .build();
    }
}
