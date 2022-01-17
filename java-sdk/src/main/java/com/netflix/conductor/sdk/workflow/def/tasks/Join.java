package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

public class Join extends BaseWorkflowTask {

    private String[] joinOn;

    public Join(String taskReferenceName, String... joinOn) {
        super(taskReferenceName, TaskType.JOIN);
        this.joinOn = joinOn;
    }
}
