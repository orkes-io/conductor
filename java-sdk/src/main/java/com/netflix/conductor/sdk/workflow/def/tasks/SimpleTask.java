package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Workflow task executed by a worker
 */
public class SimpleTask extends BaseWorkflowTask {

    private static AtomicInteger counter = new AtomicInteger(0);

    private boolean useGlobalTaskDef;

    public SimpleTask(String taskDefName, String taskReferenceName) {
        super(taskReferenceName, TaskType.SIMPLE);
        super.setName(taskDefName);
    }

    public SimpleTask(String taskDefName) {
        super(taskDefName, TaskType.SIMPLE);
        super.setName(taskDefName);
        super.setTaskReferenceName(toRefName(taskDefName));
    }

    private String toRefName(String taskDefName) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return taskDefName.replaceAll(regex, replacement).toLowerCase() + "_" + counter.getAndIncrement();
    }

    public SimpleTask useGlobalTaskDef() {
        this.useGlobalTaskDef = true;
        return this;
    }

    @Override
    public List<WorkflowTask> getWorkflowDefTasks() {
        List<WorkflowTask> tasks = super.getWorkflowDefTasks();
        if(useGlobalTaskDef) {
            tasks.forEach(task -> task.setTaskDefinition(null));
        }
        return tasks;
    }
}
