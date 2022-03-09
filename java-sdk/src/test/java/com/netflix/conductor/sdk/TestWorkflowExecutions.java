package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class TestWorkflowExecutions {


    @WorkflowTask("task_2")
    public Map<String, Object> task2(Map<String, Object> input) {
        input.put("a2", "b2");
        return input;
    }

    @WorkflowTask("task_1")
    public Map<String, Object> task1(Map<String, Object> input) {
        input.put("a1", "b1");
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
    public Map<String, Object> task101() {
        return null;
    }

    @WorkflowTask("task_101")
    public GlobalState someTask(MyWorkflowInput input) {
        return null;
    }

    @WorkflowTask("task_100")
    public Map<String, Object> task100(Map<String, Object> input) {
        input.put("a", "b");
        return input;
    }

    @WorkflowTask("fooBarTask")
    public Map<String, Object> fooBarTask(Map<String, Object> input) {
        List<WorkflowTask> forkedTasks = new ArrayList<>();
        SimpleTask task1 = new SimpleTask("task_2", "task_2_1");
        SimpleTask task2 = new SimpleTask("task_2", "task_100_1");
        input.put("tasks", Arrays.asList(task1, task2));

        Map<String, Map<String, Object>> taskInputs = new HashMap<>();
        taskInputs.put("task_2_1", new HashMap<>());
        taskInputs.put("task_100_1", new HashMap<>());

        input.put("taskInputs", taskInputs);

        return input;
    }

    @Test
    public void testExecuteSimple() throws ExecutionException, InterruptedException, JsonProcessingException {

        String url = "https://saastestapi.orkes.net/api/";
        //url = "http://localhost:8080/api/";
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
                .add(new SimpleTask("task_100", "task_100_0"));

        DynamicFork dynamicFork = new DynamicFork("dynamic_fork",
                "${fooBarTask.output.tasks}", "${fooBarTask.output.taskInputs}");

        WorkflowBuilder builder = new WorkflowBuilder(executor);

        ConductorWorkflow conductorWorkflow = builder
                .name("test_wf_as_code")
                .add(new SimpleTask("task_2", "task_2_0"))
                .add(new Fork("my_fork_with_2_branches",
                        new Function[]{input -> 1, input2 -> 2, input2 -> 3},
                        new Function[]{input -> 100}))
                .add(doWhile)
                .add(new SimpleTask("fooBarTask", "fooBarTask"))
                .add(dynamicFork)
                .build();


        ConductorWorkflow conductorWorkflow2 = builder
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
                                        new SimpleTask("a", "bc"),
                                        doWhile
                                )
                                .defaultCase("")
                )
                .add(doWhile)
                .build();

        Object executed = conductorWorkflow2.execute(new HashMap<>()).get();
        System.out.println(executed);
    }
}
