package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

public class DoWhile extends BaseWorkflowTask {

    private String loopCondition;

    private BaseWorkflowTask[] tasks;

    public DoWhile(String taskReferenceName, String condition, BaseWorkflowTask... tasks) {
        super(taskReferenceName, TaskType.DO_WHILE);
        this.tasks = tasks;
    }
}
