package com.netflix.conductor.sdk.workflow.def;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.BaseWorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Conductor workflow
 */
public class ConductorWorkflow2Backup {

    private String name;

    private String description;

    private String failureWorkflow;

    private int version;

    private List<BaseWorkflowTask> tasks = new ArrayList<>();

    private AtomicInteger taskId = new AtomicInteger(0);

    private final ObjectMapper om = new ObjectMapperProvider().getObjectMapper();

    public ConductorWorkflow2Backup(WorkflowExecutor workflowExecutor) {

    }

    public ConductorWorkflow2Backup(String name, int version) {
        this.name = name;
        this.version = version;
        this.description = name;
    }

    public ConductorWorkflow2Backup(String name) {
        this(name, 1);
    }

    public ConductorWorkflow2Backup description(String description) {
        this.description = description;
        return this;
    }

    public ConductorWorkflow2Backup failureWorkflow(String failureWorkflow) {
        this.failureWorkflow = failureWorkflow;
        return this;
    }

    public ConductorWorkflow2Backup add(BaseWorkflowTask task) {
        this.tasks.add(task);
        return this;
    }

    public ConductorWorkflow2Backup add(Function<Object, Object> task) {
        String name = this.name + "_task_" + taskId.getAndIncrement();
        add(name, task);
        return this;
    }

    public ConductorWorkflow2Backup add(String name, Function<Object, Object> task) {
        WorkerTask workerTask = new WorkerTask(name, task);
        add(workerTask);
        return this;
    }

    /**
     *
     * @param input Workflow Input - The input object is converted a JSON doc as an input to the workflow
     * @return
     */
    public CompletableFuture<Workflow> execute(Object input) {
        return null;
    }

    public WorkflowDef toWorkflowDef() {
        WorkflowDef def = new WorkflowDef();
        def.setName(name);
        def.setDescription(name);
        def.setFailureWorkflow(failureWorkflow);
        for(BaseWorkflowTask task : tasks) {
            def.getTasks().addAll(task.toWorkflowTask());
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
        ConductorWorkflow2Backup workflow = (ConductorWorkflow2Backup) o;
        return version == workflow.version && Objects.equals(name, workflow.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        try {
            return om.writeValueAsString(toWorkflowDef());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public static ConductorWorkflow2Backup byId(String id) {
        return null;
    }


}
