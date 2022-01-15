package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;

/**
 * Wait task
 */
public class Wait extends BaseWorkflowTask {

    public Wait(String taskReferenceName) {
        super(taskReferenceName, TaskType.WAIT);
    }

}
