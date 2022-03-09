package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Fork extends Task {

    private Join join;

    private Task[][] forkedTasks;

    private List<List<WorkerTask>> forkedTaskWorkers;

    public Fork(String taskReferenceName, Task[]... forkedTasks) {
        super(taskReferenceName, TaskType.FORK_JOIN);
        this.forkedTasks = forkedTasks;
    }

    public Fork(String taskReferenceName, Function<Object, Object>... forkedTaskFunctions) {
        super(taskReferenceName, TaskType.FORK_JOIN);
    }

    public Fork(String taskReferenceName, Function<Object, Object>[]... forkedTaskFunctions) {
        super(taskReferenceName, TaskType.FORK_JOIN);
        int i = 0;
        forkedTaskWorkers = new ArrayList<>();

        for (Function<Object, Object>[] forkedTaskFunctionList : forkedTaskFunctions) {
            List<WorkerTask> forkedTasks = new ArrayList<>();
            for (Function<Object, Object> forkedTaskFunction : forkedTaskFunctionList) {
                WorkerTask task = new WorkerTask("_forked_task_" + i++, forkedTaskFunction);
                forkedTasks.add(task);
            }
            forkedTaskWorkers.add(forkedTasks);
        }
    }

    public Fork joinOn(String... joinOn) {
        this.join = new Join(getTaskReferenceName() + "_join", joinOn);
        return this;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        List<WorkerTask> workerTasks = new ArrayList<>();
        if(forkedTasks != null) {
            for (Task[] forkedTaskList : forkedTasks) {
                for (Task baseWorkflowTask : forkedTaskList) {
                    workerTasks.addAll(baseWorkflowTask.getWorkerExecutedTasks());
                }
            }
        } else if(forkedTaskWorkers != null) {
            for (List<WorkerTask> forkedTaskWorkerList : forkedTaskWorkers) {
                workerTasks.addAll(forkedTaskWorkerList);
            }
        }
        return workerTasks;
    }

    @Override
    public List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> getWorkflowDefTasks() {
        com.netflix.conductor.common.metadata.workflow.WorkflowTask fork = toWorkflowTask();
        List<String> joinOnTaskRefNames = new ArrayList<>();
        List<List<com.netflix.conductor.common.metadata.workflow.WorkflowTask>> forkTasks = new ArrayList<>();
        if(forkedTasks != null) {
            for (Task[] forkedTaskList : forkedTasks) {
                List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> forkedWorkflowTasks = new ArrayList<>();
                for (Task baseWorkflowTask : forkedTaskList) {
                    forkedWorkflowTasks.addAll(baseWorkflowTask.getWorkflowDefTasks());
                }
                forkTasks.add(forkedWorkflowTasks);
                joinOnTaskRefNames.add(forkedWorkflowTasks.get(forkedWorkflowTasks.size()-1).getTaskReferenceName());
            }
        } else if(forkedTaskWorkers != null) {
            for (List<WorkerTask> forkedTaskWorkerList : forkedTaskWorkers) {
                List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> forkedWorkflowTasks = new ArrayList<>();
                for (WorkerTask workerTask : forkedTaskWorkerList) {
                    forkedWorkflowTasks.addAll(workerTask.getWorkflowDefTasks());
                }
                forkTasks.add(forkedWorkflowTasks);
                joinOnTaskRefNames.add(forkedWorkflowTasks.get(forkedWorkflowTasks.size()-1).getTaskReferenceName());
            }
        } else {
            //TODO: Add better reason, explanation, example on what to do etc...
            throw new IllegalStateException("Missing forked tasks");
        }
        fork.setForkTasks(forkTasks);

        com.netflix.conductor.common.metadata.workflow.WorkflowTask joinWorkflowTasks = null;
        if(this.join != null) {
            List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> joinTasks = this.join.getWorkflowDefTasks();
            joinWorkflowTasks = joinTasks.get(0);
        } else {
            joinWorkflowTasks = new com.netflix.conductor.common.metadata.workflow.WorkflowTask();
            joinWorkflowTasks.setWorkflowTaskType(TaskType.JOIN);
            joinWorkflowTasks.setTaskReferenceName(getTaskReferenceName() + "_join");
            joinWorkflowTasks.setName(joinWorkflowTasks.getTaskReferenceName());
            joinWorkflowTasks.setJoinOn(joinOnTaskRefNames);
        }

        return Arrays.asList(fork, joinWorkflowTasks);
    }
}
