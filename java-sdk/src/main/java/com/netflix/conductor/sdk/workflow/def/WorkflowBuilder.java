package com.netflix.conductor.sdk.workflow.def;

import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.sdk.workflow.def.tasks.BaseWorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Conductor workflow Builder
 */
public class WorkflowBuilder<T> {

    private String name;

    private String description;

    private int version;

    private String failureWorkflow;

    private String ownerEmail;

    private WorkflowDef.TimeoutPolicy timeoutPolicy;

    private long timeoutSeconds;

    private boolean restartable = true;

    private T defaultInput;

    private Map<String, Object> output = new HashMap<>();

    private List<BaseWorkflowTask> tasks = new ArrayList<>();

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

    public WorkflowBuilder ownerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
        return this;
    }

    public WorkflowBuilder timeoutPolicy(WorkflowDef.TimeoutPolicy timeoutPolicy, long timeoutSeconds) {
        this.timeoutPolicy = timeoutPolicy;
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public WorkflowBuilder defaultInput(T defaultInput) {
        this.defaultInput = defaultInput;
        return this;
    }

    public WorkflowBuilder restartable(boolean restartable) {
        this.restartable = restartable;
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

    public WorkflowBuilder add(String taskReferenceName, Function<Object, Object> task) {
        WorkerTask workerTask = new WorkerTask(taskReferenceName, task);
        add(workerTask);
        return this;
    }

    public ConductorWorkflow<T> build() {
        ConductorWorkflow workflow = new ConductorWorkflow(workflowExecutor);
        if(description != null) {
            workflow.setDescription(description);
        }

        workflow.setName(name);
        workflow.setVersion(version);
        workflow.setDescription(description);
        workflow.setFailureWorkflow(failureWorkflow);
        workflow.setOwnerEmail(ownerEmail);
        workflow.setTimeoutPolicy(timeoutPolicy);
        workflow.setTimeoutSeconds(timeoutSeconds);
        workflow.setRestartable(restartable);
        workflow.setDefaultInput(defaultInput);
        workflow.setOutput(output);

        for (BaseWorkflowTask task : tasks) {
            workflow.add(task);
            List<WorkerTask> workerExecutedTasks = task.getWorkerExecutedTasks();
            workerExecutedTasks.stream()
                    .forEach(workerTask -> workflowExecutor.addWorker(workflow, workerTask));
        }
        return workflow;
    }

}
