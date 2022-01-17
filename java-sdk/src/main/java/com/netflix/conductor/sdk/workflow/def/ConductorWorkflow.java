package com.netflix.conductor.sdk.workflow.def;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import com.netflix.conductor.sdk.workflow.def.tasks.BaseWorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Conductor workflow
 */
public class ConductorWorkflow {

    private String name;

    private String description;

    private String failureWorkflow;

    private int version;

    private Map<String, Object> output = new HashMap<>();

    private List<BaseWorkflowTask> tasks = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapperProvider().getObjectMapper();

    private final WorkflowExecutor workflowExecutor;

    ConductorWorkflow(WorkflowExecutor workflowExecutor) {
        this.workflowExecutor = workflowExecutor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFailureWorkflow(String failureWorkflow) {
        this.failureWorkflow = failureWorkflow;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public void add(BaseWorkflowTask task) {
        this.tasks.add(task);
    }

    /**
     *
     * @param input Workflow Input - The input object is converted a JSON doc as an input to the workflow
     * @return
     */
    public CompletableFuture<Workflow> execute(Object input) {
        return workflowExecutor.executeWorkflow(this, input);
    }

    public WorkflowDef toWorkflowDef() {
        WorkflowDef def = new WorkflowDef();
        def.setName(name);
        def.setDescription(name);
        def.setFailureWorkflow(failureWorkflow);
        def.setOutputParameters(output);
        for(BaseWorkflowTask task : tasks) {
            def.getTasks().addAll(task.getWorkflowDefTasks());
        }
        return def;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getVersion() {
        return version;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConductorWorkflow workflow = (ConductorWorkflow) o;
        return version == workflow.version && Objects.equals(name, workflow.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(toWorkflowDef());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
