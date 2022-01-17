package com.netflix.conductor.sdk.workflow.def;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.BaseWorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.Fork;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.executor.task.WorkerExecutor;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Conductor workflow
 */
public class WorkflowBuilder {

    private String name;

    private String description;

    private String failureWorkflow;

    private int version;

    private String correlationId;

    private Map<String, Object> output = new HashMap<>();

    private List<BaseWorkflowTask> tasks = new ArrayList<>();

    private AtomicInteger taskId = new AtomicInteger(0);

    private WorkflowExecutor workflowExecutor;

    public WorkflowBuilder(WorkflowExecutor workflowExecutor) {
        this.workflowExecutor = workflowExecutor;
    }

    public WorkflowBuilder name(String name) {
        this.name = name;
        return this;
    }

    public WorkflowBuilder version(int version) {
        this.version = version;
        return this;
    }

    public WorkflowBuilder description(String description) {
        this.description = description;
        return this;
    }

    public WorkflowBuilder failureWorkflow(String failureWorkflow) {
        this.failureWorkflow = failureWorkflow;
        return this;
    }

    public WorkflowBuilder output(String key, boolean value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder output(String key, String value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder output(String key, Number value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder output(String key, Object value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder output(MapBuilder mapBuilder) {
        output.putAll(mapBuilder.build());
        return this;
    }

    public WorkflowBuilder add(BaseWorkflowTask task) {
        this.tasks.add(task);
        return this;
    }

    public WorkflowBuilder add(Function<Object, Object> task) {
        String name = this.name + "_task_" + taskId.getAndIncrement();
        add(name, task);
        return this;
    }

    public WorkflowBuilder add(String name, Function<Object, Object> task) {
        WorkerTask workerTask = new WorkerTask(name, task);
        add(workerTask);
        return this;
    }

    public ConductorWorkflow build() {
        ConductorWorkflow workflow = new ConductorWorkflow(workflowExecutor);
        if(description != null) {
            workflow.setDescription(description);
        }
        workflow.setFailureWorkflow(failureWorkflow);
        workflow.setName(name);
        workflow.setVersion(version);
        workflow.setOutput(output);
        for (BaseWorkflowTask task : tasks) {
            workflow.add(task);
            task.getWorkerExecutedTasks()
                    .stream()
                    .forEach(workerTask -> workflowExecutor.addWorker(workflow, workerTask));
        }
        return workflow;
    }

}
