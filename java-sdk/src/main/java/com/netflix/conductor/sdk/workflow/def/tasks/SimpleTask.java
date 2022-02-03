package com.netflix.conductor.sdk.workflow.def.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow task executed by a worker
 */
public class SimpleTask<I, O> extends BaseWorkflowTask<I, O> {

    private static final int ONE_HOUR = 60 * 60;

    private final ObjectMapper objectMapper = new ObjectMapperProvider().getObjectMapper();

    private boolean useGlobalTaskDef;

    private TaskDef taskDef;

    private Map<String, Object> inputTemplate = new HashMap<>();

    public SimpleTask(String taskDefName, String taskReferenceName) {
        super(taskReferenceName, TaskType.SIMPLE);
        super.setName(taskDefName);
    }

    /**
     * When set workflow will  use the  task definition registered in conductor.
     * Workflow registration will fail if no task definitions are found in conductor server
     * @return current instance
     */
    public SimpleTask useGlobalTaskDef() {
        this.useGlobalTaskDef = true;
        return this;
    }

    public SimpleTask useTaskDef(TaskDef taskDef) {
        this.taskDef = taskDef;
        this.useGlobalTaskDef = false;
        return this;
    }

    @Override
    protected WorkflowTask toWorkflowTask() {
        WorkflowTask task = super.toWorkflowTask();
        if(this.taskDef != null) {
            task.setTaskDefinition(taskDef);
        }
        return task;
    }

    @Override
    public List<WorkflowTask> getWorkflowDefTasks() {
        List<WorkflowTask> tasks = super.getWorkflowDefTasks();
        if(useGlobalTaskDef) {
            tasks.forEach(task -> task.setTaskDefinition(null));
        }
        return tasks;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        return super.getWorkerExecutedTasks();
    }
}
