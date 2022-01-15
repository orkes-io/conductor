package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

public class SetVariable extends BaseWorkflowTask {

    public static final String TYPE = "SET_VARIABLE";

    public SetVariable(String taskReferenceName) {
        super(taskReferenceName, TaskType.SET_VARIABLE);
    }

}
