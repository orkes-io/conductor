package com.netflix.conductor.sdk.tasks;

import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.MyWorkflowInput;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.DoWhile;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;
import com.netflix.conductor.sdk.workflow.def.tasks.Switch;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import com.netflix.conductor.testing.workflows.Task1Input;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestDoWhile extends AbstractSDKTest {

    @Test
    public void verifyLoopIteratesNTimes() throws ExecutionException, InterruptedException {

        SimpleTask task1 = new SimpleTask("task_1", "task_1");
        SimpleTask task2 = new SimpleTask("task_2", "task_2");
        SimpleTask task3 = new SimpleTask("task_3", "task_3");

        DoWhile doWhile = new DoWhile("execute_3_times", 3, task1, task2, task3);
        DoWhile doWhile1 = new DoWhile("aa", 2, (Task1Input a) -> {
            return 42;
        });
        Switch sw1 = new Switch("s1", (Map<String, Object> input) -> {
            return null;
        });
        ConductorWorkflow workflow = new WorkflowBuilder(executor)
                .name("test_do_while_loop")
                .ownerEmail("owner@example.com")
                .add(doWhile)
                .add(doWhile1)
                .build();

        CompletableFuture<Workflow>  future = workflow.execute(new MyWorkflowInput());
        Workflow execution = future.get();
        System.out.println("execution: " + execution);
        System.out.println("Done");
    }
}
