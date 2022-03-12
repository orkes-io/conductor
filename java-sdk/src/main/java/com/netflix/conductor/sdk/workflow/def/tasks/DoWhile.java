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
import java.util.List;

import com.google.common.base.Strings;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ValidationError;

public class DoWhile extends Task<DoWhile> {

    static {
        TaskRegistry.register(TaskType.DO_WHILE.name(), DoWhile.class);
    }

    private String loopCondition;

    private List<Task<?>> tasks = new ArrayList<>();

    public DoWhile(String taskReferenceName, String condition, Task... tasks) {
        super(taskReferenceName, TaskType.DO_WHILE);
        for (Task<?> task : tasks) {
            this.tasks.add(task);
        }
        this.loopCondition = condition;
    }

    public DoWhile(String taskReferenceName, int loopCount, Task... tasks) {
        super(taskReferenceName, TaskType.DO_WHILE);
        for (Task<?> task : tasks) {
            this.tasks.add(task);
        }
        this.loopCondition = getForLoopCondition(loopCount);
    }

    DoWhile(WorkflowTask workflowTask) {
        super(workflowTask);
        this.loopCondition = workflowTask.getLoopCondition();
        for (WorkflowTask task : workflowTask.getLoopOver()) {
            Task<?> loopTask = TaskRegistry.getTask(task);
            this.tasks.add(loopTask);
        }
    }

    public DoWhile add(Task<?>... tasks) {
        for (Task<?> task : tasks) {
            this.tasks.add(task);
        }
        return this;
    }

    private String getForLoopCondition(int loopCount) {
        return "if ( $."
                + getTaskReferenceName()
                + "['iteration'] < "
                + loopCount
                + ") { true; } else { false; }";
    }

    public String getLoopCondition() {
        return loopCondition;
    }

    public List<? extends Task> getTasks() {
        return tasks;
    }

    @Override
    public List<WorkflowTask> getWorkflowDefTasks() {
        List<WorkflowTask> workflowTasks = super.getWorkflowDefTasks();
        WorkflowTask loopTask = workflowTasks.get(0);
        loopTask.setLoopCondition(loopCondition);

        List<WorkflowTask> loopTasks = new ArrayList<>();
        for (Task task : tasks) {
            loopTasks.addAll(task.getWorkflowDefTasks());
        }
        loopTask.setLoopOver(loopTasks);

        return workflowTasks;
    }
}
