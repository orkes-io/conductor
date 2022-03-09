package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

/**
 * Wait task
 */
public class Wait extends Task {

    public Wait(String taskReferenceName) {
        super(taskReferenceName, TaskType.WAIT);
    }

}
