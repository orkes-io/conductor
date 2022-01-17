package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.executor.task.AnnotatedWorkerExecutor;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import org.checkerframework.checker.units.qual.A;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class TestWorkflowExecutions {


    @WorkflowTask("task_2")
    public Map<String, Object> task2(Map<String, Object> input) {
        input.put("a", "b");
        return input;
    }

    public TaskResult task21(Map<String, Object> input) {
        TaskResult taskResult = new TaskResult();
        taskResult.setStatus(TaskResult.Status.COMPLETED);
        taskResult.getOutputData().put("key1", "value");
        taskResult.getOutputData().put("key2",42);
        return taskResult;
    }

    @WorkflowTask("task_100")
    public Map<String, Object> task100(Map<String, Object> input) {
        input.put("a", "b");
        return input;
    }

    @Test
    public void testExecuteSimple() throws ExecutionException, InterruptedException, JsonProcessingException {

        String url = "https://saastestapi.orkes.net/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);
        executor.initWorkers(TestWorkflowExecutions.class.getPackageName());

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
                .add(new SimpleTask("task_100"));

        WorkflowBuilder builder = new WorkflowBuilder(executor);
        ConductorWorkflow conductorWorkflow = builder
                .name("test_wf_as_code")
                .add(new SimpleTask("task_2"))
                .add("my_task", o -> task21(null))
                .add("taskx", o -> "Hello World from taskx")
                .add(new Fork("my_fork_with_2_branches",
                        new Function[]{input -> 1, input2 -> 2, input2 -> 3},
                        new Function[]{input -> 100}))
                .add(doWhile)
                .build();

        Workflow executed = conductorWorkflow.execute(new HashMap<>()).get();
        System.out.println(executed);
    }
}
