package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DoWhile extends Task {

    private String loopCondition;

    private List<Task> tasks = new ArrayList<>();

    public DoWhile(String taskReferenceName, String condition, Task... tasks) {
        super(taskReferenceName, TaskType.DO_WHILE);
        for (Task task : tasks) {
            this.tasks.add(task);
        }
        this.loopCondition = condition;
    }

    public DoWhile(String taskReferenceName, int loopCount, Task... tasks) {
        super(taskReferenceName, TaskType.DO_WHILE);
        for (Task task : tasks) {
            this.tasks.add(task);
        }
        this.loopCondition = getForLoopCondition(loopCount);

    }

    public <T> DoWhile(String taskReferenceName, int loopCount, Function<T, Object>... taskFunctions) {
        super(taskReferenceName, TaskType.DO_WHILE);
        this.loopCondition = getForLoopCondition(loopCount);
        for(int i = 0; i < taskFunctions.length; i++) {
            this.tasks.add(new WorkerTask(getTaskReferenceName() + "_task" + i, taskFunctions[i]));
        }
    }

    public DoWhile add(Task... tasks) {
        for (Task task : tasks) {
            this.tasks.add(task);
        }
        return this;
    }

    private String getForLoopCondition(int loopCount) {
        return "if ( $." + getTaskReferenceName() + "['iteration'] < " + loopCount + ") { true; } else { false; }";
    }

    @Override
    public List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> getWorkflowDefTasks() {
        List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> workflowTasks = super.getWorkflowDefTasks();
        com.netflix.conductor.common.metadata.workflow.WorkflowTask loopTask = workflowTasks.get(0);
        loopTask.setLoopCondition(loopCondition);

        List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> loopTasks = new ArrayList<>();
        for (Task task : tasks) {
            loopTasks.addAll(task.getWorkflowDefTasks());
        }
        loopTask.setLoopOver(loopTasks);

        return workflowTasks;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        List<WorkerTask> workerTasks = new ArrayList<>();
        for (Task task : tasks) {
            workerTasks.addAll(task.getWorkerExecutedTasks());
        }
        return workerTasks;
    }
}
