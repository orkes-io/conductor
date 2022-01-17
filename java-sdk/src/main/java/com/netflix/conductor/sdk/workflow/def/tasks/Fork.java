package com.netflix.conductor.sdk.workflow.def.tasks;

import com.google.common.base.Joiner;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Fork extends BaseWorkflowTask {

    private Join join;

    private BaseWorkflowTask[][] forkedTasks;

    private List<List<WorkerTask>> forkedTaskWorkers;

    public Fork(String taskRefrenceName, BaseWorkflowTask[]... forkedTasks) {
        super(taskRefrenceName, TaskType.FORK_JOIN);
        this.forkedTasks = forkedTasks;
    }

    public Fork(String taskRefrenceName, Function<Object, Object>... forkedTaskFunctions) {
        super(taskRefrenceName, TaskType.FORK_JOIN);
    }

    public Fork(String taskRefrenceName, Function<Object, Object>[]... forkedTaskFunctions) {
        super(taskRefrenceName, TaskType.FORK_JOIN);
        int i = 0;
        forkedTaskWorkers = new ArrayList<>();

        for (Function<Object, Object>[] forkedTaskFunctionList : forkedTaskFunctions) {
            List<WorkerTask> forkedTasks = new ArrayList<>();
            for (Function<Object, Object> forkedTaskFunction : forkedTaskFunctionList) {
                WorkerTask task = new WorkerTask("forked_task_" + i++, forkedTaskFunction);
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
            for (BaseWorkflowTask[] forkedTaskList : forkedTasks) {
                for (BaseWorkflowTask baseWorkflowTask : forkedTaskList) {
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
    public List<WorkflowTask> getWorkflowDefTasks() {
        int i = 0;
        WorkflowTask fork = toWorkflowTask();
        List<String> joinOnTaskRefNames = new ArrayList<>();
        List<List<WorkflowTask>> forkTasks = new ArrayList<>();
        if(forkedTasks != null) {
            for (BaseWorkflowTask[] forkedTaskList : forkedTasks) {
                List<WorkflowTask> forkedWorkflowTasks = new ArrayList<>();
                for (BaseWorkflowTask baseWorkflowTask : forkedTaskList) {
                    forkedWorkflowTasks.addAll(baseWorkflowTask.getWorkflowDefTasks());
                }
                forkTasks.add(forkedWorkflowTasks);
                joinOnTaskRefNames.add(forkedWorkflowTasks.get(forkedWorkflowTasks.size()-1).getTaskReferenceName());
            }
        } else if(forkedTaskWorkers != null) {
            List<WorkflowTask> forkedWorkflowTasks = new ArrayList<>();
            for (List<WorkerTask> forkedTaskWorkerList : forkedTaskWorkers) {
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

        WorkflowTask joinWorkflowTasks = null;
        if(this.join != null) {
            joinWorkflowTasks = this.join.getWorkflowDefTasks().get(0);
        } else {
            joinWorkflowTasks = new WorkflowTask();
            joinWorkflowTasks.setWorkflowTaskType(TaskType.JOIN);
            joinWorkflowTasks.setTaskReferenceName(getTaskReferenceName() + "_join");
            joinWorkflowTasks.setJoinOn(joinOnTaskRefNames);
        }

        return Arrays.asList(fork, joinWorkflowTasks);
    }
}
