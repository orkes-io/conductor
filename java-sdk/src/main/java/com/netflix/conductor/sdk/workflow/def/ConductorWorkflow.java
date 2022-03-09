package com.netflix.conductor.sdk.workflow.def;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.Task;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.InputOutputGetter;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @param <T> Type of the workflow input
 */
public class ConductorWorkflow<T> {

    public static final InputOutputGetter input = new InputOutputGetter("workflow", InputOutputGetter.Field.input);

    public static final InputOutputGetter output = new InputOutputGetter("workflow", InputOutputGetter.Field.output);

    private String name;

    private String description;

    private int version;

    private String failureWorkflow;

    private String ownerEmail;

    private WorkflowDef.TimeoutPolicy timeoutPolicy;

    private Map<String, Object> workflowOutput;

    private long timeoutSeconds;

    private boolean restartable = true;

    private T defaultInput;

    private List<Task> tasks = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapperProvider().getObjectMapper();

    private final WorkflowExecutor workflowExecutor;

    ConductorWorkflow(WorkflowExecutor workflowExecutor) {
        this.workflowOutput = new HashMap<>();
        this.workflowExecutor = workflowExecutor;
        this.restartable = true;
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

    public void add(Task task) {
        this.tasks.add(task);
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

    public String getFailureWorkflow() {
        return failureWorkflow;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public WorkflowDef.TimeoutPolicy getTimeoutPolicy() {
        return timeoutPolicy;
    }

    public void setTimeoutPolicy(WorkflowDef.TimeoutPolicy timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isRestartable() {
        return restartable;
    }

    public void setRestartable(boolean restartable) {
        this.restartable = restartable;
    }

    public T getDefaultInput() {
        return defaultInput;
    }

    public void setDefaultInput(T defaultInput) {
        this.defaultInput = defaultInput;
    }

    public Map<String, Object> getWorkflowOutput() {
        return workflowOutput;
    }

    public void setWorkflowOutput(Map<String, Object> workflowOutput) {
        this.workflowOutput = workflowOutput;
    }

    /**
     *
     * @param input Workflow Input - The input object is converted a JSON doc as an input to the workflow
     * @return
     */
    public CompletableFuture<Workflow> execute(T input) {
        return workflowExecutor.executeWorkflow(this, input);
    }

    /**
     *
     * @return true if success, false if the workflow already exists with the given version number
     */
    public boolean registerWorkflow() {
        return workflowExecutor.registerWorkflow(toWorkflowDef());
    }

    public WorkflowDef toWorkflowDef() {

        WorkflowDef def = new WorkflowDef();
        def.setName(name);
        def.setDescription(description);
        def.setVersion(version);
        def.setFailureWorkflow(failureWorkflow);
        def.setOwnerEmail(ownerEmail);
        def.setTimeoutPolicy(timeoutPolicy);
        def.setTimeoutSeconds(timeoutSeconds);
        def.setRestartable(restartable);
        def.setOutputParameters(workflowOutput);
        def.setInputTemplate(objectMapper.convertValue(defaultInput, Map.class));

        for(Task task : tasks) {
            def.getTasks().addAll(task.getWorkflowDefTasks());
        }
        return def;
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
