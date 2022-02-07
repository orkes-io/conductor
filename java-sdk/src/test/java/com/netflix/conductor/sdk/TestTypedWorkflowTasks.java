package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;
import com.netflix.conductor.testing.workflows.Task1Input;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SuppressWarnings("ALL")
public class TestTypedWorkflowTasks {


    @WorkflowTask("task_2xx")
    public TaskResult getCreditScores(Task task) {
        if(task.getRetryCount() < 1 && task.getReferenceTaskName().equals("task_2_dyn_1")) {
            throw new RuntimeException("just chill for a bit!");
        }
        TaskResult result = new TaskResult(task);
        result.setStatus(TaskResult.Status.COMPLETED);
        result.getOutputData().put("task_result_key", "value_" + task.getRetryCount());
        result.getOutputData().put("num", task.getRetryCount());

        return result;
    }

    @WorkflowTask("task_3")
    public TaskResult task3(Task task) {
        if(task.getRetryCount() < 1) {
            //throw new RuntimeException("just chill for a bit!");
        }
        task.getOutputData().put("my_key", 42);
        task.getOutputData().put("text_output", 42 + "_" + task.getRetryCount());
        TaskResult result = new TaskResult(task);
        result.setStatus(TaskResult.Status.COMPLETED);
        return result;
    }

    @WorkflowTask("fooBarTask")
    public CreditProcessingResult fooBar(String name, int creditScore, String zipCode) {
        return new CreditProcessingResult(null);
    }

    public static class TaskBuilder {
        public static <T, R> CompletableFuture<R> simpleTask(String refName, Function<T, R> fn) {
            return null;
        }
    }

    public void testCodeBaseFlow() {
        String url = "https://saastestapi.orkes.net/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);
        SimpleTask task3 = new SimpleTask("task_3", "task3");
        CompletableFuture<TaskResult> output = TaskBuilder.simpleTask("task_3", (Task task) -> task3(task));


        ConductorWorkflow<MyWorkflowInput> conductorWorkflow = new WorkflowBuilder(executor)
                .name("reproduce_join_issue")
                .build();

    }

    @Test
    public void testExecuteSimple() throws ExecutionException, InterruptedException, JsonProcessingException {

        String url = "https://saastestapi.orkes.net/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);
        executor.initWorkers(TestTypedWorkflowTasks.class.getPackageName());
        final int count = 3;
        DynamicFork fork = new DynamicFork("dyn_fork", o -> {
            List<BaseWorkflowTask> tasks = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                tasks.add(new SimpleTask("task_2xx", "task_2_dyn_" + i));
            }
            SimpleTask task3 = new SimpleTask("task_3", "task3");
            tasks.add(task3);
            return tasks;
        }, o -> {
            Map<String, Object> inputs = new HashMap<>();
            for(int i = 0; i < count; i++) {
                Map<String, Object> input = new HashMap<>();
                input.put("key", "value_" + i);
                inputs.put("task_2_dyn_" + i, input);
            }
            Map<String, Object> input = new HashMap<>();
            input.put("key", "value");
            inputs.put("task3", input);
            return inputs;
        });

        ConductorWorkflow<MyWorkflowInput> conductorWorkflow = new WorkflowBuilder(executor)
                .name("reproduce_join_issue")
                .add(fork)
                .add("aa", o -> fooBar(ConductorWorkflow.input.get("name"), 12, "9445"))
                .loop("do_2_times", 2, fork)
                .output("result", "${" + fork.getJoin().getTaskReferenceName() + ".output}")
                .build();

        Workflow execution = conductorWorkflow.execute(new MyWorkflowInput()).get();
        System.out.println(execution);

    }
}
