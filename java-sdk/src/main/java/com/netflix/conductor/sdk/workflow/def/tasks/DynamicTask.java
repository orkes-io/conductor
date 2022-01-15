package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.Map;
import java.util.function.Function;

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
