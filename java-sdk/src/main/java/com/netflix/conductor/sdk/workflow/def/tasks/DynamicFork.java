package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DynamicFork extends BaseWorkflowTask {

    public static final String TYPE = "FORK_JOIN_DYNAMIC";

    private String forkTasks;

    private String forkTasksInputs;

    private Join join;

    public DynamicFork(String taskReferenceName, String forkTasks, String forkTasksInputs) {
        super(taskReferenceName, TaskType.FORK_JOIN_DYNAMIC);
        this.join = new Join(taskReferenceName + "_join");
        this.forkTasks = forkTasks;
        this.forkTasksInputs = forkTasksInputs;
        super.input("forkedTasks", forkTasks);
        super.input("forkedTasksInputs", forkTasksInputs);
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        return super.getWorkerExecutedTasks();
    }

    @Override
    protected WorkflowTask toWorkflowTask() {
        WorkflowTask task = super.toWorkflowTask();
        task.setDynamicForkTasksParam("forkedTasks");
        task.setDynamicForkTasksInputParamName("forkedTasksInputs");
        return task;
    }

    @Override
    public List<WorkflowTask> getWorkflowDefTasks() {
        List<WorkflowTask> tasks = new ArrayList<>();
        tasks.addAll(super.getWorkflowDefTasks());
        tasks.addAll(join.getWorkflowDefTasks());
        return tasks;
    }
}
