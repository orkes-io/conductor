package com.netflix.conductor.sdk.tasks;

import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.MyWorkflowInput;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.testing.WorkflowTestRunner;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.BaseWorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.DoWhile;
import com.netflix.conductor.sdk.workflow.def.tasks.DynamicFork;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.testing.workflows.Task1Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestDynamicFork extends AbstractSDKTest {

    @WorkflowTask("get_forks")
    public List<BaseWorkflowTask> getForks(Object input) {
        List<BaseWorkflowTask> tasks = new ArrayList<>();
        tasks.add(new SimpleTask("task_1", "task_1"));
        tasks.add(new SimpleTask("task_2", "task_2"));
        return tasks;
    }


    @WorkflowTask("get_fork_inputs")
    public Map<String, Object> getForkInputs(Object o) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("task_1", new Task1Input());
        inputs.put("task_2", new Task1Input());
        return inputs;
    }

    @Test
    public void testDynamicForkWithFunctions() throws ExecutionException, InterruptedException {

        DynamicFork dynamicFork = new DynamicFork("dynamic_fork",
                this::getForks, this::getForkInputs);
        ConductorWorkflow workflow = new WorkflowBuilder(executor)
                .name("test_dynamic_fork")
                .ownerEmail("owner@example.com")
                .add(dynamicFork)
                .build();

        CompletableFuture<Workflow> future = workflow.execute(new MyWorkflowInput());
        System.out.println("future: " + future);
        Workflow execution = future.get();
        System.out.println("execution: " + execution);


    }

    @Test
    public void testDynamicFork() throws ExecutionException, InterruptedException {

        DynamicFork dynamicFork = new DynamicFork("dynamic_fork",
                "${get_forks.output.result}", "${get_fork_inputs.output}");

        ConductorWorkflow workflow = new WorkflowBuilder(executor)
                .name("test_dynamic_fork_with_json_defs")
                .ownerEmail("owner@example.com")
                .add(new SimpleTask("get_forks", "get_forks"))
                .add(new SimpleTask("get_fork_inputs", "get_fork_inputs"))
                .add(dynamicFork)
                .build();

        CompletableFuture<Workflow> future = workflow.execute(new MyWorkflowInput());
        Workflow execution = future.get();
        System.out.println("execution: " + execution);

    }
}
