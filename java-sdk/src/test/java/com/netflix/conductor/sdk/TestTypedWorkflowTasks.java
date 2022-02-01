package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SuppressWarnings("ALL")
public class TestTypedWorkflowTasks {


    @WorkflowTask("get_credit_scores")
    public CreditProcessingResult getCreditScores(CustomerInfo customerInfo) {
        CustomerInfo ci = null;
        return new CreditProcessingResult(customerInfo);
    }

    @WorkflowTask("fooBarTask")
    public CreditProcessingResult fooBar(String name, int creditScore, String zipCode) {
        return new CreditProcessingResult(null);
    }


    @Test
    public void testExecuteSimple() throws ExecutionException, InterruptedException, JsonProcessingException {

        String url = "https://saastestapi.orkes.net/api/";
        //url = "http://localhost:8080/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);
        executor.initWorkers(TestTypedWorkflowTasks.class.getPackageName());

        Switch sw1 = new Switch("switch_task",
                input -> {
                    if (input.equals("a")) {
                        return "path_a";
                    }
                    return "path_b";
                })
                .switchCase("path_b", "task_2");

        AtomicInteger counter = new AtomicInteger(10);

        DoWhile doWhile = new DoWhile("execute_3_times", 3,
                input -> counter.getAndIncrement())
                .add(sw1)
                .add(new SimpleTask("task_100", "task_100_0"));

        DynamicFork dynamicFork = new DynamicFork("dynamic_fork",
                "${fooBarTask.output.tasks}", "${fooBarTask.output.taskInputs}");

        SimpleTask<CreditProcessingResult> fooBar = new SimpleTask("fooBarTask", "fooBarTask");
        fooBar.input(
                "name", ConductorWorkflow.input.get("name"),
                "creditScore", ConductorWorkflow.input.get("creditScore"),
                "zipCode", ConductorWorkflow.input.get("creditScore"),
                "amount", doWhile.taskOutput.get("amount")
        );

        SimpleTask fooBar2 = new SimpleTask("fooBarTask", "fooBarTask");
        fooBar2.input(
                "name", ConductorWorkflow.input.get("name"),
                "creditScore", ConductorWorkflow.input.get("creditScore"),
                "zipCode", ConductorWorkflow.input.get("creditScore"),
                "amount", 12
        );

        SimpleTask fooBar3 = new SimpleTask("fooBarTask", "fooBarTask");
        //fooBar3.input(fooBar.taskOutput.get(CreditProcessingResult.class));



        WorkflowBuilder<MyWorkflowInput> builder = new WorkflowBuilder<>(executor);
        builder
                .name("test_wf_as_code")
                .add(fooBar)
                .add(
                        new SimpleTask("get_credit_scores","get_credit_scores")
                )
                .build();

        SimpleTask getCreditScores =
                new SimpleTask("get_credit_scores", "get_credit_scores");

        ConductorWorkflow<MyWorkflowInput> conductorWorkflow = new WorkflowBuilder(executor)
                .name("test_wf_as_code")
                .add(fooBar)
                .add(getCreditScores)
                .add(new SimpleTask("task_2", "task_2_0"))
                .add("taskx", o -> "Hello World from taskx")
                .add(new Fork("my_fork_with_2_branches",
                        new Function[]{input -> 1, input2 -> 2, input2 -> 3},
                        new Function[]{input -> 100}))
                .add(doWhile)
                .add(dynamicFork)
                .build();
         /*
        ConductorWorkflow conductorWorkflow = builder
                .name("name")
                .version(1)
                .failureWorkflow("failureWorkflow")
                .add(new SimpleTask("task_2", "task_2").useGlobalTaskDef())
                .add(
                        new Switch("", "${workflow.input.city}")
                                .switchCase("nyc",
                                        new SimpleTask("a", "b"),
                                        new SimpleTask("ab", "bcd"))
                                .switchCase("sfo",
                                        new SimpleTask("a", "b"),
                                        new SimpleTask("a", "bc")
                                )
                                .defaultCase("")
                )
                .build();
*/
        //ConductorWorkflow conductorWorkflow2 = ConductorWorkflow.byNameAndVersion("abcd", 2);
        boolean success = conductorWorkflow.registerWorkflow();
        conductorWorkflow.execute(new MyWorkflowInput()).thenAccept(workflow -> {

        });

        //Workflow executed = conductorWorkflow.execute(new HashMap<>()).get();
        //System.out.println(executed);
    }
}
