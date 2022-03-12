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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ValidationError;

public class Fork extends Task<Fork> {

    static {
        TaskRegistry.register(TaskType.FORK_JOIN.name(), Fork.class);
    }

    private Join join;

    private Task[][] forkedTasks;

    public Fork(String taskReferenceName, Task[]... forkedTasks) {
        super(taskReferenceName, TaskType.FORK_JOIN);
        this.forkedTasks = forkedTasks;
    }

    Fork(WorkflowTask workflowTask) {
        super(workflowTask);
        int size = workflowTask.getForkTasks().size();
        this.forkedTasks = new Task[size][];
        int i = 0;
        for (List<WorkflowTask> forkTasks : workflowTask.getForkTasks()) {
            Task[] tasks = new Task[forkTasks.size()];
            for (int j = 0; j < forkTasks.size(); j++) {
                WorkflowTask forkWorkflowTask = forkTasks.get(j);
                Task task = TaskRegistry.getTask(forkWorkflowTask);
                tasks[j] = task;
            }
            this.forkedTasks[i++] = tasks;
        }
    }

    public Fork joinOn(String... joinOn) {
        this.join = new Join(getTaskReferenceName() + "_join", joinOn);
        return this;
    }

    @Override
    protected WorkflowTask toWorkflowTask() {
        WorkflowTask workflowTask = super.toWorkflowTask();
        return workflowTask;
    }

    @Override
    public List<WorkflowTask> getWorkflowDefTasks() {
        WorkflowTask fork = toWorkflowTask();
        List<String> joinOnTaskRefNames = new ArrayList<>();
        List<List<WorkflowTask>> forkTasks = new ArrayList<>();

        for (Task[] forkedTaskList : forkedTasks) {
            List<WorkflowTask> forkedWorkflowTasks = new ArrayList<>();
            for (Task baseWorkflowTask : forkedTaskList) {
                forkedWorkflowTasks.addAll(baseWorkflowTask.getWorkflowDefTasks());
            }
            forkTasks.add(forkedWorkflowTasks);
            joinOnTaskRefNames.add(
                    forkedWorkflowTasks.get(forkedWorkflowTasks.size() - 1).getTaskReferenceName());
        }

        fork.setForkTasks(forkTasks);

        WorkflowTask joinWorkflowTasks = null;
        if (this.join != null) {
            List<WorkflowTask> joinTasks = this.join.getWorkflowDefTasks();
            joinWorkflowTasks = joinTasks.get(0);
        } else {
            joinWorkflowTasks = new WorkflowTask();
            joinWorkflowTasks.setWorkflowTaskType(TaskType.JOIN);
            joinWorkflowTasks.setTaskReferenceName(getTaskReferenceName() + "_join");
            joinWorkflowTasks.setName(joinWorkflowTasks.getTaskReferenceName());
            joinWorkflowTasks.setJoinOn(joinOnTaskRefNames);
        }

        return Arrays.asList(fork, joinWorkflowTasks);
    }

    public Task[][] getForkedTasks() {
        return forkedTasks;
    }
}
