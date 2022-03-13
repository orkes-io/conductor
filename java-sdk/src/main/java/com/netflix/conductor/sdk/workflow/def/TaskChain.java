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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.*;

public abstract class TaskChain {

    protected List<Task<?>> tasks = new ArrayList<>();

    public TaskChain() {}

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

    public Switch decide(String taskReferenceName, String caseExpression,
                         Callable<List<Task<?>>> defaultCase, Callable<Map<String, List<Task<?>>>> switchCases) {
        Switch decide = new Switch(taskReferenceName, caseExpression);

        try {
            decide.defaultCase(defaultCase.call());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Map<String, List<Task<?>>> decisionCases = switchCases.call();
            decide.decisionCases(decisionCases);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        add(decide);
        return decide;
    }

    public SetVariable setVariable(String taskReferenceName, Task[]... forkedTasks) {
        SetVariable setVar = new SetVariable(taskReferenceName);
        add(setVar);
        return setVar;
    }

    public Wait wait(String taskReferenceName) {
        Wait wait = new Wait(taskReferenceName);
        add(wait);
        return wait;
    }

    public <T> SubWorkflow subWorkflow(
            String taskReferenceName, ConductorWorkflow<T> conductorWorkflow) {
        SubWorkflow subWorkflow = new SubWorkflow(taskReferenceName, conductorWorkflow);
        add(subWorkflow);
        return subWorkflow;
    }

    public <T> SubWorkflow subWorkflow(
            String taskReferenceName, String subWorkflowName, Integer subWorkflowVersion) {
        SubWorkflow subWorkflow =
                new SubWorkflow(taskReferenceName, subWorkflowName, subWorkflowVersion);
        add(subWorkflow);
        return subWorkflow;
    }

    public TaskChain terminate(
            String taskReferenceName,
            Workflow.WorkflowStatus terminationStatus,
            String reason,
            Object workflowOutput) {
        Terminate terminate =
                new Terminate(taskReferenceName, terminationStatus, reason, workflowOutput);
        add(terminate);
        return this;
    }
}
