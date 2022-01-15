package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.sdk.workflow.def.tasks.BaseWorkflowTask;

import java.util.function.Function;

/**
 * Workflow task executed by a worker
 */
public class WorkerTask extends BaseWorkflowTask {

    private Function<Object, Object> taskExecutor;

    public WorkerTask(String taskReferenceName) {
        super(taskReferenceName, TaskType.SIMPLE);
    }

    public WorkerTask(String taskReferenceName, Function<Object, Object> taskExecutor) {
        super(taskReferenceName, TaskType.SIMPLE);
        this.taskExecutor = taskExecutor;
    }

    public Function<Object, Object> getTaskExecutor() {
        return taskExecutor;
    }
}
