package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class TestWorkflowExecutions {

    @WorkflowTask("task_2")
    public String task2() {
        return "Test";
    }

    @Test
    public void testExecuteSimple() throws ExecutionException, InterruptedException, JsonProcessingException {
        String url = "https://conductorapp.trescommas.dev/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);

        Switch sw1 = new Switch("aa", input -> {
            if(input.equals("a")) {
                return "path_a";
            }
            return "path_b";
        }).switchCase("path_q", "task_2");




        WorkflowBuilder builder = new WorkflowBuilder(executor);
        ConductorWorkflow conductorWorkflow = builder
                .name("test_wf_as_code")
                .add("my_task", o -> 42)
                .add("taskx", o -> {
                    return "Hello World";
                })
                .add(new Fork("fork_0", new Function[]{input->1}, new Function[]{input -> 100}))
                .build();

        conductorWorkflow.toWorkflowDef();

        System.out.println(new ObjectMapperProvider().getObjectMapper().writeValueAsString(conductorWorkflow.toWorkflowDef()));

        Workflow executed = conductorWorkflow.execute(new HashMap<>()).get();
        System.out.println(executed);
    }
}
