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

import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.utils.InputOutputGetter;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;

import java.util.*;

public abstract class TaskChain {

    protected List<Task<?>> tasks = new ArrayList<>();

    public TaskChain() {

    }

    public TaskChain task(Task<?>... tasks) {
        Collections.addAll(this.tasks, tasks);
        return this;
    }

    public TaskChain add(Task<?>... tasks) {
        Collections.addAll(this.tasks, tasks);
        return this;
    }

    public DoWhile loop(String taskReferenceName, int loopCount, Task... tasks) {
        DoWhile doWhile = new DoWhile(taskReferenceName, loopCount, tasks);
        add(doWhile);
        return doWhile;
    }

    public DoWhile loop(String taskReferenceName, String condition, Task... tasks) {
        DoWhile doWhile = new DoWhile(taskReferenceName, condition, tasks);
        add(doWhile);
        return doWhile;
    }

    public Fork parallel(String taskReferenceName, Task[]... forkedTasks) {
        Fork fork = new Fork(taskReferenceName, forkedTasks);
        add(fork);
        return fork;
    }

    public Switch decide(String taskReferenceName, String caseExpression) {
        Switch decide = new Switch(taskReferenceName, caseExpression);
        add(decide);
        return decide;
    }

    public TaskChain terminate(String taskReferenceName,
                                  Workflow.WorkflowStatus terminationStatus,
                                  String reason,
                                  Object workflowOutput ) {
        Terminate terminate = new Terminate(taskReferenceName, terminationStatus, reason, workflowOutput);
        add(terminate);
        return this;
    }
}
