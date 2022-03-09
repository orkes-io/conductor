package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

public class Join extends Task {

    private String[] joinOn;

    public Join(String taskReferenceName, String... joinOn) {
        super(taskReferenceName, TaskType.JOIN);
        this.joinOn = joinOn;
    }
}
