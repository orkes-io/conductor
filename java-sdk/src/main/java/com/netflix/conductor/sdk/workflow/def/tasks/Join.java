package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

public class Join extends BaseWorkflowTask {

    public static final String TYPE = "JOIN";

    private String[] joinOn;

    public Join(String taskReferenceName, String... joinOn) {
        super(taskReferenceName, TaskType.JOIN);
        this.joinOn = joinOn;
    }
}
