/*
 * Copyright 2022 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.conductor.sdk.workflow.def;

import java.util.*;

import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.sdk.workflow.def.tasks.DoWhile;
import com.netflix.conductor.sdk.workflow.def.tasks.Task;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.InputOutputGetter;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;

/** @param <T> Input type for the workflow */
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

    private List<Task> tasks = new ArrayList<>();

    private WorkflowExecutor workflowExecutor;

    public final InputOutputGetter input =
            new InputOutputGetter("workflow", InputOutputGetter.Field.input);

    public WorkflowBuilder(WorkflowExecutor workflowExecutor) {
        this.workflowExecutor = workflowExecutor;
    }

    public WorkflowBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    public WorkflowBuilder<T> version(int version) {
        this.version = version;
        return this;
    }

    public WorkflowBuilder<T> description(String description) {
        this.description = description;
        return this;
    }

    public WorkflowBuilder<T> failureWorkflow(String failureWorkflow) {
        this.failureWorkflow = failureWorkflow;
        return this;
    }

    public WorkflowBuilder<T> ownerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
        return this;
    }

    public WorkflowBuilder<T> timeoutPolicy(
            WorkflowDef.TimeoutPolicy timeoutPolicy, long timeoutSeconds) {
        this.timeoutPolicy = timeoutPolicy;
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public WorkflowBuilder<T> defaultInput(T defaultInput) {
        this.defaultInput = defaultInput;
        return this;
    }

    public WorkflowBuilder<T> restartable(boolean restartable) {
        this.restartable = restartable;
        return this;
    }

    public WorkflowBuilder<T> output(String key, boolean value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder<T> output(String key, String value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder<T> output(String key, Number value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder<T> output(String key, Object value) {
        output.put(key, value);
        return this;
    }

    public WorkflowBuilder<T> output(MapBuilder mapBuilder) {
        output.putAll(mapBuilder.build());
        return this;
    }

    public WorkflowBuilder<T> add(Task task) {
        this.tasks.add(task);
        return this;
    }

    public WorkflowBuilder<T> add(Task... tasks) {
        Collections.addAll(this.tasks, tasks);
        return this;
    }

    public WorkflowBuilder<T> doWhile(String taskReferenceName, String condition, Task... tasks) {
        DoWhile doWhile = new DoWhile(taskReferenceName, condition, tasks);
        add(doWhile);
        return this;
    }

    public WorkflowBuilder<T> loop(String taskReferenceName, int loopCount, Task... tasks) {
        DoWhile doWhile = new DoWhile(taskReferenceName, loopCount, tasks);
        add(doWhile);
        return this;
    }

    public ConductorWorkflow<T> build() {
        ConductorWorkflow<T> workflow = new ConductorWorkflow<T>(workflowExecutor);
        if (description != null) {
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
        workflow.setWorkflowOutput(output);

        for (Task task : tasks) {
            workflow.add(task);
        }
        return workflow;
    }
}
