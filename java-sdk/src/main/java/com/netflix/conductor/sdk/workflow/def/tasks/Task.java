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

import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.utils.InputOutputGetter;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Workflow Task */
public abstract class Task {

    private String name;

    private String description;

    private String taskReferenceName;

    private boolean optional;

    private int startDelay;

    private TaskType type;

    private Map<String, Object> input = new HashMap<>();

    protected final ObjectMapper om = new ObjectMapperProvider().getObjectMapper();

    public final InputOutputGetter taskInput;

    public final InputOutputGetter taskOutput;

    public Task(String taskReferenceName, TaskType type) {
        this.name = taskReferenceName;
        this.taskReferenceName = taskReferenceName;
        this.type = type;
        this.taskInput = new InputOutputGetter(taskReferenceName, InputOutputGetter.Field.input);
        this.taskOutput = new InputOutputGetter(taskReferenceName, InputOutputGetter.Field.output);
    }

    public Task name(String name) {
        this.name = name;
        return this;
    }

    public Task description(String description) {
        this.description = description;
        return this;
    }

    public Task input(String key, boolean value) {
        input.put(key, value);
        return this;
    }

    public Task input(String key, Object value) {
        input.put(key, value);
        return this;
    }

    public Task input(String key, char value) {
        input.put(key, value);
        return this;
    }

    public Task input(String key, InputOutputGetter value) {
        input.put(key, value.getParent());
        return this;
    }

    public Task input(InputOutputGetter value) {
        return input("input", value);
    }

    public Task input(String key, String value) {
        input.put(key, value);
        return this;
    }

    public Task input(String key, Number value) {
        input.put(key, value);
        return this;
    }

    public Task input(String key, Map<String, Object> value) {
        input.put(key, value);
        return this;
    }

    public Task input(Map<String, Object> map) {
        input.putAll(map);
        return this;
    }

    public Task input(MapBuilder builder) {
        input.putAll(builder.build());
        return this;
    }

    public Task input(Object... keyValues) {
        if (keyValues.length == 1) {
            Object kv = keyValues[0];
            Map objectMap = om.convertValue(kv, Map.class);
            input.putAll(objectMap);
            return this;
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
        return this;
    }

    /*
    TODO: Delete this
    public WorkflowTask output(String key, Number value) {
        output.put(key, value);
        return this;
    }

    public WorkflowTask output(String key, String value) {
        output.put(key, value);
        return this;
    }

    public WorkflowTask output(String key, Map<String, Object> value) {
        output.put(key, value);
        return this;
    }

    public WorkflowTask output(MapBuilder builder) {
        output.putAll(builder.build());
        return this;
    }

    public WorkflowTask output(String key, boolean value) {
        output.put(key, value);
        return this;
    }

     */

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
        return Arrays.asList(toWorkflowTask());
    }

    protected WorkflowTask toWorkflowTask() {
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName(name);
        workflowTask.setTaskReferenceName(taskReferenceName);
        workflowTask.setWorkflowTaskType(type);
        workflowTask.setDescription(description);
        workflowTask.setInputParameters(input);

        TaskDef taskDef = new TaskDef();
        taskDef.setName(name);
        taskDef.setRetryCount(3);
        taskDef.setRetryDelaySeconds(1);
        taskDef.setRetryLogic(TaskDef.RetryLogic.FIXED);

        workflowTask.setTaskDefinition(taskDef);

        return workflowTask;
    }
}
