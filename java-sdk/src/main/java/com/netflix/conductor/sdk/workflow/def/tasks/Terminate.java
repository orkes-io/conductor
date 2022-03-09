package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.run.Workflow;

public class Terminate extends Task {

    //TODO: Make this two variables public in the core Terminate.java
    private static final String TERMINATION_STATUS_PARAMETER = "terminationStatus";

    private static final String TERMINATION_WORKFLOW_OUTPUT = "workflowOutput";

    private String terminationStatus;

    private Object workflowOutput;

    public Terminate(String taskReferenceName, Workflow.WorkflowStatus terminationStatus, Object workflowOutput) {
        this(taskReferenceName, terminationStatus.name(), workflowOutput);
    }

    public Terminate(String taskReferenceName, String terminationStatus, Object workflowOutput) {
        super(taskReferenceName, TaskType.TERMINATE);
        this.terminationStatus = terminationStatus;
        this.workflowOutput = workflowOutput;
        input(TERMINATION_STATUS_PARAMETER, terminationStatus);
        input(TERMINATION_WORKFLOW_OUTPUT, workflowOutput);
    }
}
