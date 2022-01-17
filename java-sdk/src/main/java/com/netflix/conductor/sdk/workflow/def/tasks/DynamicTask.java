package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

public class DynamicTask extends BaseWorkflowTask {

    public static final String TYPE = "DYNAMIC";

    private String dynamicTaskValue;

    public DynamicTask(String taskReferenceName, String dynamicTaskValue) {
        super(taskReferenceName, TaskType.DYNAMIC);
        this.dynamicTaskValue = dynamicTaskValue;
    }

    public String getDynamicTaskValue() {
        return dynamicTaskValue;
    }

}
