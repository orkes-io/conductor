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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.netflix.conductor.client.exception.ConductorClientException;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.Task;
import com.netflix.conductor.sdk.workflow.def.tasks.TaskRegistry;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.InputOutputGetter;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.C;

/** @param <T> Type of the workflow input */
public class ConductorWorkflow<T> {

    public static final InputOutputGetter input =
            new InputOutputGetter("workflow", InputOutputGetter.Field.input);

    public static final InputOutputGetter output =
            new InputOutputGetter("workflow", InputOutputGetter.Field.output);

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
     * Execute a dynamic workflow without creating a definition in metadata store.
     * <p><br/>
     * <b>Note</b>: Use this with caution - as this does not promote re-usability of the workflows
     * </p>
     * @param input Workflow Input - The input object is converted a JSON doc as an input to the
     *     workflow
     * @return
     */
    public CompletableFuture<Workflow> executeDynamic(T input) {
        return workflowExecutor.executeWorkflow(this, input);
    }

    public CompletableFuture<Workflow> execute(T input) {
        return workflowExecutor.executeWorkflow(this.getName(), this.getVersion(), input);
    }

    /**
     * @return true if success, false if the workflow already exists with the given version number
     */
    public boolean registerWorkflow() {
        return registerWorkflow(false, false);
    }

    public boolean registerWorkflow(boolean overwrite) {
        return registerWorkflow(overwrite, false);
    }

    /**
     * @param overwrite if true, the existing definition will be overwritten.  Use with caution
     * @return true if success, false if the workflow already exists with the given version number
     */
    public boolean registerWorkflow(boolean overwrite, boolean registerTasks) {
        WorkflowDef workflowDef = toWorkflowDef();
        List<WorkflowTask> missing = getMissingTasks(workflowDef);
        if(!missing.isEmpty()) {
            if(!registerTasks) {
                throw new RuntimeException("Workflow cannot be registered.  The following tasks do not have definitions.  " +
                        "Please register these tasks before creating the workflow.  Missing Tasks = " +
                        missing.stream().map(WorkflowTask::getName).collect(Collectors.toSet()));
            } else {
                missing.stream().forEach(this::registerTaskDef);
            }
        }
        return workflowExecutor.registerWorkflow(workflowDef, overwrite);
    }

    private List<WorkflowTask> getMissingTasks(WorkflowDef workflowDef) {
        List<WorkflowTask> missing = new ArrayList<>();
        workflowDef.collectTasks().stream().forEach(workflowTask -> {
            try {
                TaskDef taskDef = workflowExecutor.getMetadataClient().getTaskDef(workflowTask.getName());
            }catch(ConductorClientException cce) {
                if(cce.getStatus() == 404) {
                    missing.add(workflowTask);
                } else {
                    throw cce;
                }
            }
        });
        return missing;
    }

    private void registerTaskDef(WorkflowTask workflowTask) {
        TaskDef taskDef = new TaskDef();
        taskDef.setName(workflowTask.getName());
        taskDef.setRetryCount(3);
        taskDef.setRetryDelaySeconds(1);
        taskDef.setRetryLogic(TaskDef.RetryLogic.FIXED);
        workflowExecutor.getMetadataClient().registerTaskDefs(Arrays.asList(taskDef));
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

        for (Task task : tasks) {
            def.getTasks().addAll(task.getWorkflowDefTasks());
        }
        return def;
    }

    public static ConductorWorkflow<?> fromWorkflowDef(WorkflowDef def) {
        ConductorWorkflow<?> workflow = new ConductorWorkflow<>(null);
        workflow.setName(def.getName());
        workflow.setVersion(def.getVersion());
        workflow.setFailureWorkflow(def.getFailureWorkflow());
        workflow.setRestartable(def.isRestartable());
        //TODO: fix this
        //workflow.setDefaultInput(def.getInputTemplate());
        //def.getVariables()
        workflow.setWorkflowOutput(def.getOutputParameters());
        workflow.setOwnerEmail(def.getOwnerEmail());
        workflow.setDescription(def.getDescription());
        workflow.setTimeoutSeconds(def.getTimeoutSeconds());
        workflow.setTimeoutPolicy(def.getTimeoutPolicy());

        List<WorkflowTask> workflowTasks = def.getTasks();
        for (WorkflowTask workflowTask : workflowTasks) {
            Task task = TaskRegistry.getTask(workflowTask);
            workflow.tasks.add(task);
        }
        return workflow;
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
