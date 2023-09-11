package com.netflix.conductor.common.metadata.workflow;

import java.util.Map;

public class UpgradeWorkflowRequest {

    public Map<String, Map<String, Object>> getTaskOutput() {
        return taskOutput;
    }

    public void setTaskOutput(Map<String, Map<String, Object>> taskOutput) {
        this.taskOutput = taskOutput;
    }

    public Map<String, Object> getWorkflowInput() {
        return workflowInput;
    }

    public void setWorkflowInput(Map<String, Object> workflowInput) {
        this.workflowInput = workflowInput;
    }

    private Map<String, Map<String, Object>> taskOutput;

    private Map<String, Object> workflowInput;
}
