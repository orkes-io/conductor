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
package com.netflix.conductor.sdk.workflow.def.tasks;

import java.util.*;

import com.google.common.base.Strings;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ValidationError;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.utils.InputOutputGetter;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Workflow Task */
public abstract class Task<T> {

    private String name;

    private String description;

    private String taskReferenceName;

    private boolean optional;

    private int startDelay;

    private TaskType type;

    private Map<String, Object> input = new HashMap<>();

    protected final ObjectMapper om = new ObjectMapperProvider().getObjectMapper();

    protected WorkflowBuilder<?> builder;

    public final InputOutputGetter taskInput;

    public final InputOutputGetter taskOutput;

    public Task(String taskReferenceName, TaskType type) {
        if(Strings.isNullOrEmpty(taskReferenceName)) {
            throw new AssertionError("taskReferenceName cannot be null");
        }
        if(type == null) {
            throw new AssertionError("type cannot be null");
        }

        this.name = taskReferenceName;
        this.taskReferenceName = taskReferenceName;
        this.type = type;
        this.taskInput = new InputOutputGetter(taskReferenceName, InputOutputGetter.Field.input);
        this.taskOutput = new InputOutputGetter(taskReferenceName, InputOutputGetter.Field.output);
    }

    Task(WorkflowTask workflowTask) {
        this(workflowTask.getTaskReferenceName(), TaskType.valueOf(workflowTask.getType()));
        this.input = workflowTask.getInputParameters();
        this.description = workflowTask.getDescription();
        this.name = workflowTask.getName();
    }

    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    public T description(String description) {
        this.description = description;
        return (T) this;
    }

    public T input(String key, boolean value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, Object value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, char value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, InputOutputGetter value) {
        input.put(key, value.getParent());
        return (T) this;
    }

    public T input(InputOutputGetter value) {
        return input("input", value);
    }

    public T input(String key, String value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, Number value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(String key, Map<String, Object> value) {
        input.put(key, value);
        return (T) this;
    }

    public T input(Map<String, Object> map) {
        input.putAll(map);
        return (T) this;
    }

    public T input(MapBuilder builder) {
        input.putAll(builder.build());
        return (T) this;
    }

    public T input(Object... keyValues) {
        if (keyValues.length == 1) {
            Object kv = keyValues[0];
            Map objectMap = om.convertValue(kv, Map.class);
            input.putAll(objectMap);
            return (T) this;
        }
        if (keyValues.length % 2 == 1) {
            throw new IllegalArgumentException("Not all keys have value specified");
        }
        for (int i = 0; i < keyValues.length; ) {
            String key = keyValues[i].toString();
            Object value = keyValues[i + 1];
            input.put(key, value);
            i += 2;
        }
        return (T) this;
    }

    public void setBuilder(WorkflowBuilder<?> builder) {
        this.builder = builder;
    }

    public <I>WorkflowBuilder<I> end() {
        return (WorkflowBuilder<I>) builder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskReferenceName() {
        return taskReferenceName;
    }

    public void setTaskReferenceName(String taskReferenceName) {
        this.taskReferenceName = taskReferenceName;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(int startDelay) {
        this.startDelay = startDelay;
    }

    public TaskType getType() {
        return type;
    }

    public List<WorkflowTask> getWorkflowDefTasks() {
        return List.of(toWorkflowTask());
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    protected WorkflowTask toWorkflowTask() {
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName(name);
        workflowTask.setTaskReferenceName(taskReferenceName);
        workflowTask.setWorkflowTaskType(type);
        workflowTask.setDescription(description);
        workflowTask.setInputParameters(input);

        return workflowTask;
    }
}
