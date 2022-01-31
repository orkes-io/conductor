package com.netflix.conductor.sdk.workflow.def.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import java.util.*;

/**
 * Abstraction of a Workflow Task
 */
public abstract class BaseWorkflowTask<R> {

    private String name;

    private String description;

    private String taskReferenceName;

    private boolean optional;

    private int startDelay;

    private TaskType type;

    private Map<String, Object> input = new HashMap<>();

    private Map<String, Object> output = new HashMap<>();

    protected final ObjectMapper om = new ObjectMapperProvider().getObjectMapper();

    public BaseWorkflowTask(String taskReferenceName, TaskType type) {
        this.name = taskReferenceName;
        this.taskReferenceName = taskReferenceName;
        this.type = type;
    }

    public BaseWorkflowTask name(String name) {
        this.name = name;
        return this;
    }


    public BaseWorkflowTask description(String description) {
        this.description = description;
        return this;
    }

    public BaseWorkflowTask input(String key, boolean value) {
        input.put(key, value);
        return this;
    }

    public BaseWorkflowTask input(String key, Object value) {
        input.put(key, value);
        return this;
    }

    public BaseWorkflowTask output(String key, boolean value) {
        output.put(key, value);
        return this;
    }

    public BaseWorkflowTask input(Object... keyValues) {
        if(keyValues.length %2 == 1) {
            throw new IllegalArgumentException("Not all keys have value specified");
        }
        for(int i = 0; i < keyValues.length;) {
            String key = keyValues[i].toString();
            Object value = keyValues[i+1];
            input.put(key, value);

            i += 2;
        }
        return this;
    }

    public BaseWorkflowTask input(String key, String value) {
        input.put(key, value);
        return this;
    }

    public BaseWorkflowTask output(String key, String value) {
        output.put(key, value);
        return this;
    }

    public BaseWorkflowTask input(String key, Number value) {
        input.put(key, value);
        return this;
    }

    public BaseWorkflowTask output(String key, Number value) {
        output.put(key, value);
        return this;
    }

    public BaseWorkflowTask input(String key, Map<String, Object> value) {
        input.put(key, value);
        return this;
    }

    public BaseWorkflowTask input(Map<String, Object> map) {
        input.putAll(map);
        return this;
    }

    public BaseWorkflowTask output(String key, Map<String, Object> value) {
        output.put(key, value);
        return this;
    }

    public BaseWorkflowTask input(MapBuilder builder) {
        input.putAll(builder.build());
        return this;
    }

    public BaseWorkflowTask output(MapBuilder builder) {
        output.putAll(builder.build());
        return this;
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

    public List<WorkerTask> getWorkerExecutedTasks() {
        return new ArrayList();
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
        workflowTask.setTaskDefinition(taskDef);

        return workflowTask;

    }

    /*
    public static class Output<R> {
        private String refName;

        public Output(String refName) {
            this.refName = refName;
        }

        public String get(String key) {
            return "${" + refName + ".output." + key + "}";
        }

        public Map<String, Object> get(Class<R> key) {
            return null;
        }
    }

    public final Output<R> taskOutput = new Output(taskReferenceName);

     */
}
