package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DynamicFork extends Task {

    public static final String TYPE = "FORK_JOIN_DYNAMIC";

    private String forkTasks;

    private String forkTasksInputs;

    private Join join;

    private WorkerTask forkWorkerTask;

    private WorkerTask forkInputWorkerTask;

    public DynamicFork(String taskReferenceName, String forkTasks, String forkTasksInputs) {
        super(taskReferenceName, TaskType.FORK_JOIN_DYNAMIC);
        this.join = new Join(taskReferenceName + "_join");
        this.forkTasks = forkTasks;
        this.forkTasksInputs = forkTasksInputs;
        super.input("forkedTasks", forkTasks);
        super.input("forkedTasksInputs", forkTasksInputs);
    }

    public DynamicFork(String taskReferenceName,
                       Function<Object, List<Task>> forkTasks,
                       Function<Object, Map<String, Object>> forkTasksInputs) {

        super(taskReferenceName, TaskType.FORK_JOIN_DYNAMIC);
        this.join = new Join(taskReferenceName + "_join");
        this.forkTasks = "forkTasks";
        this.forkTasksInputs = "forkTasksInputs";

        this.forkWorkerTask = new WorkerTask(
                taskReferenceName + "_forkTaskFn",
                input -> {
                    List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> defTasks = new ArrayList<>();
                    List<Task> tasks = forkTasks.apply(input);
                    for (Task task : tasks) {
                        List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> workflowDefTasks = task.getWorkflowDefTasks();
                        for (com.netflix.conductor.common.metadata.workflow.WorkflowTask workflowDefTask : workflowDefTasks) {
                            defTasks.add(workflowDefTask);
                        }
                    }
                    return defTasks;
                }
        );
        super.input("forkedTasks", "${" + this.forkWorkerTask.getTaskReferenceName() + ".output.result}");

        this.forkInputWorkerTask = new WorkerTask(taskReferenceName + "_forkTaskInputFn", input -> {
            Map<String, Object> result = forkTasksInputs.apply(input);
            Map<String, Object> output = new HashMap<>();
            output.put("result", result);
            return output;
        });
        super.input("forkedTasksInputs", "${" + this.forkInputWorkerTask.getTaskReferenceName() + ".output.result}");
    }

    public Join getJoin() {
        return join;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        List<WorkerTask> workerExecutedTasks = new ArrayList<>();

        if(forkWorkerTask != null && forkInputWorkerTask != null) {
            workerExecutedTasks.addAll(forkWorkerTask.getWorkerExecutedTasks());
            workerExecutedTasks.addAll(forkInputWorkerTask.getWorkerExecutedTasks());
        }
        System.out.println("Returning workerExecutedTasks : " + workerExecutedTasks);
        return workerExecutedTasks;
    }

    @Override
    protected com.netflix.conductor.common.metadata.workflow.WorkflowTask toWorkflowTask() {
        com.netflix.conductor.common.metadata.workflow.WorkflowTask task = super.toWorkflowTask();
        task.setDynamicForkTasksParam("forkedTasks");
        task.setDynamicForkTasksInputParamName("forkedTasksInputs");
        return task;
    }

    @Override
    public List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> getWorkflowDefTasks() {
        List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> tasks = new ArrayList<>();
        if(forkWorkerTask != null && forkInputWorkerTask != null) {
            tasks.addAll(forkWorkerTask.getWorkflowDefTasks());
            tasks.addAll(forkInputWorkerTask.getWorkflowDefTasks());
        }
        tasks.addAll(super.getWorkflowDefTasks());
        tasks.addAll(join.getWorkflowDefTasks());
        return tasks;
    }
}
