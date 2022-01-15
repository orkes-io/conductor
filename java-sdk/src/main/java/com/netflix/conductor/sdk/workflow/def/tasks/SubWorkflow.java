package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;

public class SubWorkflow extends BaseWorkflowTask {

    public static final String TYPE = "SUB_WORKFLOW";

    private ConductorWorkflow conductorWorkflow;

    private String workflowName;

    private int workflowVersion;

    public SubWorkflow(String taskReferenceName, String workflowName, int  workflowVersion) {
        super(taskReferenceName, TaskType.SUB_WORKFLOW);
        this.workflowName = workflowName;
        this.workflowVersion = workflowVersion;
    }

    public SubWorkflow(String taskReferenceName, ConductorWorkflow conductorWorkflow) {
        super(taskReferenceName, TaskType.SUB_WORKFLOW);
        this.conductorWorkflow = conductorWorkflow;
    }
}
