package com.netflix.conductor.tests;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;

import java.util.Random;

public class KitchensinkWorkers {

    @WorkflowTask("task1")
    public TaskResult task1(Task task) {
        task.setStatus(Task.Status.COMPLETED);
        return new TaskResult(task);
    }

    @WorkflowTask("task2")
    public @OutputParam("greetings") String task2(@InputParam("name") String name) {
        return "Hello, " + name;
    }

    @WorkflowTask("task3")
    public @OutputParam("luckyNumber") int task3() {
        return new Random().nextInt(43);
    }
}
