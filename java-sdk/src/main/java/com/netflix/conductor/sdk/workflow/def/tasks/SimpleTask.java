package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Workflow task executed by a worker
 */
public class SimpleTask extends BaseWorkflowTask {

    public SimpleTask(String taskReferenceName) {
        super(taskReferenceName, TaskType.SIMPLE);
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        return Collections.EMPTY_LIST;
    }
}
