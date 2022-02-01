package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Workflow task executed by a worker
 * T: type of the input the worker task takes.
 */
public class WorkerTask<T> extends BaseWorkflowTask {

    private Function<T, Object> taskExecutor;

    public WorkerTask(String taskReferenceName, Function<T, Object> taskExecutor) {
        super(taskReferenceName, TaskType.SIMPLE);
        this.taskExecutor = taskExecutor;
    }

    public Function<T, Object> getTaskExecutor() {
        return taskExecutor;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        return Arrays.asList(this);
    }

}
