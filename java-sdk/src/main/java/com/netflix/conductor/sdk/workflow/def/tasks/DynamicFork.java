package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

public class DynamicFork extends BaseWorkflowTask {

    public static final String TYPE = "FORK_JOIN_DYNAMIC";

    private String forkParameter;

    private String forkTaskInputParam;

    private Join join;

    public DynamicFork(String taskReferenceName, String forkParameter, String forkTaskInputParam) {
        super(taskReferenceName, TaskType.FORK_JOIN_DYNAMIC);
        this.join = new Join(taskReferenceName + "_join");
    }



}
