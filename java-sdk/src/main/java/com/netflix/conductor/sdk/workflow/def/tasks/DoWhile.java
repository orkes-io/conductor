package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DoWhile extends BaseWorkflowTask {

    private String loopCondition;

    private List<BaseWorkflowTask> tasks = new ArrayList<>();

    public DoWhile(String taskReferenceName, String condition, BaseWorkflowTask... tasks) {
        super(taskReferenceName, TaskType.DO_WHILE);
        for (BaseWorkflowTask task : tasks) {
            this.tasks.add(task);
        }
        this.loopCondition = condition;
    }

    public DoWhile(String taskReferenceName, int loopCount, BaseWorkflowTask... tasks) {
        super(taskReferenceName, TaskType.DO_WHILE);
        for (BaseWorkflowTask task : tasks) {
            this.tasks.add(task);
        }
        this.loopCondition = getForLoopCondition(loopCount);

    }

    public DoWhile(String taskReferenceName, int loopCount, Function<Object, Object>... taskFunctions) {
        super(taskReferenceName, TaskType.DO_WHILE);
        this.loopCondition = getForLoopCondition(loopCount);
        for(int i = 0; i < taskFunctions.length; i++) {
            this.tasks.add(new WorkerTask(getTaskReferenceName() + "_task" + i, taskFunctions[i]));
        }
    }

    public DoWhile add(BaseWorkflowTask... tasks) {
        for (BaseWorkflowTask task : tasks) {
            this.tasks.add(task);
        }
        return this;
    }

    private String getForLoopCondition(int loopCount) {
        return "if ( $." + getTaskReferenceName() + "['iteration'] < " + loopCount + ") { true; } else { false; }";
    }

    @Override
    public List<WorkflowTask> getWorkflowDefTasks() {
        List<WorkflowTask> workflowTasks = super.getWorkflowDefTasks();
        WorkflowTask loopTask = workflowTasks.get(0);
        loopTask.setLoopCondition(loopCondition);

        List<WorkflowTask> loopTasks = new ArrayList<>();
        for (BaseWorkflowTask task : tasks) {
            loopTasks.addAll(task.getWorkflowDefTasks());
        }
        loopTask.setLoopOver(loopTasks);

        return workflowTasks;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        List<WorkerTask> workerTasks = new ArrayList<>();
        for (BaseWorkflowTask task : tasks) {
            workerTasks.addAll(task.getWorkerExecutedTasks());
        }
        return workerTasks;
    }
}
