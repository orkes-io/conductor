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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.tasks.*;

public abstract class TaskChain {

    protected List<Task<?>> tasks = new ArrayList<>();

    public TaskChain() {}

    /**
     * Adds a sequence of tasks to be executed
     * @param tasks
     * @return
     */
    public TaskChain add(Task<?>... tasks) {
        Collections.addAll(this.tasks, tasks);
        return this;
    }

    /**
     * Execute tasks N number of time specified by loopCount, similar to a for loop
     * @param taskReferenceName
     * @param loopCount no. of times the tasks should be executed
     * @param tasks List of tasks to execute in sequence
     * @return
     */
    public DoWhile loop(String taskReferenceName, int loopCount, Task<?>... tasks) {
        DoWhile doWhile = new DoWhile(taskReferenceName, loopCount, tasks);
        add(doWhile);
        return doWhile;
    }

    /**
     * Execute tasks in a loop
     * @param taskReferenceName
     * @param condition Javascript expression that controls the loop.  The tasks are executed while the expression is true
     * @param tasks List of tasks to execute in sequence
     * @return
     */
    public DoWhile loop(String taskReferenceName, String condition, Task<?>... tasks) {
        DoWhile doWhile = new DoWhile(taskReferenceName, condition, tasks);
        add(doWhile);
        return doWhile;
    }

    /**
     * execute task specified in the forkedTasks parameter in parallel.
     * <p>forkedTask is a two-dimensional list that executes the outermost list in parallel and list within
     * that is executed sequentially.</p>
     * <p>e.g. [[task1, task2],[task3, task4],[task5]] are executed as:</p>
     *
     * <pre>
     *                    ---------------
     *                    |     fork    |
     *                    ---------------
     *                    |       |     |
     *                    |       |     |
     *                  task1  task3  task5
     *                  task2  task4    |
     *                    |      |      |
     *                 ---------------------
     *                 |       join        |
     *                 ---------------------
     * </pre>
     * <p>
     *     This method automatically adds a join that waits for all the *last* tasks in the
     *     fork (e.g. task2, task4 and task5 in the above example) to be completed.*
     * </p>
     * <p>
     *     Use join method @see {@link Fork#joinOn(String...)} to override this behavior (note: not a common scenario)
     * </p>
     * @param taskReferenceName
     * @param forkedTasks
     * @return
     */
    public Fork forkJoin(String taskReferenceName, Task<?>[]... forkedTasks) {
        Fork fork = new Fork(taskReferenceName, forkedTasks);
        add(fork);
        return fork;
    }

    /**
     * Switch case (similar to if...then...else or switch in java language)
     * @param taskReferenceName
     * @param caseExpression An expression that outputs a string value to be used as case branches.
     *                       Case expression can be a support value parameter
     *                       e.g. ${workflow.input.key} or ${task.output.key} or a Javascript statement.
     * @param isJavascriptExpression set to true if the caseExpression is a javascript statement
     * @return
     */
    public Switch decide(String taskReferenceName, String caseExpression, boolean isJavascriptExpression) {
        Switch decide = new Switch(taskReferenceName, caseExpression, isJavascriptExpression);
        add(decide);
        return decide;
    }

    /**
     * Sets the value of the variable in workflow.  Used for workflow state management.
     * Workflow state is a Map that is initialized using @see {@link WorkflowBuilder#variables(Object)}
     *
     * @param taskReferenceName
     * @param key Name of the key for which the value should be changed
     * @param value Value. Serialized using ObjectMapper
     * @return
     */
    public SetVariable setVariable(String taskReferenceName, String key, Object value) {
        SetVariable setVar = new SetVariable(taskReferenceName);
        setVar.input(key, value);
        add(setVar);
        return setVar;
    }

    /**
     * Wait until and external signal completes the task.
     * The external signal can be either an API call (POST /api/task) to update the task status or an event coming from
     * a supported external queue integration like SQS, Kafka, NATS, AMQP etc.
     * <p>
     * <br/>
     * see
     * <a href=https://netflix.github.io/conductor/reference-docs/wait-task/>
     * https://netflix.github.io/conductor/reference-docs/wait-task</a> for more details
     * </p>
     * @param taskReferenceName
     * @return
     */
    public Wait wait(String taskReferenceName) {
        Wait wait = new Wait(taskReferenceName);
        add(wait);
        return wait;
    }

    /**
     * Add a javascript task in the workflow.
     * Javascript tasks are executed on the Conductor server without having to write worker code
     * <p>
     *     Use {@link Javascript#validate()} method to validate the javascript to ensure the script is valid.
     *
     * </p>
     * @param taskReferenceName
     * @param script Script to execute
     * @return
     */
    public Javascript inlineJavascript(String taskReferenceName, String script) {
        Javascript javascript = new Javascript(taskReferenceName, script);
        add(javascript);
        return javascript;
    }

    /**
     * Add a javascript task in the workflow.
     * Javascript tasks are executed on the Conductor server without having to write worker code
     * <p>
     *     Use {@link Javascript#validate()} method to validate the javascript to ensure the script is valid.
     *
     * </p>
     * @param taskReferenceName
     * @param resource InputResource to load the script file from
     * @return
     */
    public Javascript inlineJavascript(String taskReferenceName, InputStream resource) {
        try {
            String script = new String(resource.readAllBytes());
            Javascript javascript = new Javascript(taskReferenceName, script);
            add(javascript);
            return javascript;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute a sub workflow
     * @param taskReferenceName
     * @param conductorWorkflow
     * @param <T>
     * @return
     */
    public <T> SubWorkflow subWorkflow(
            String taskReferenceName, ConductorWorkflow<T> conductorWorkflow) {
        SubWorkflow subWorkflow = new SubWorkflow(taskReferenceName, conductorWorkflow);
        add(subWorkflow);
        return subWorkflow;
    }

    /**
     * Execute a sub workflow
     * @param taskReferenceName
     * @param subWorkflowName
     * @param subWorkflowVersion
     * @param <T>
     * @return
     */
    public <T> SubWorkflow subWorkflow(
            String taskReferenceName, String subWorkflowName, Integer subWorkflowVersion) {
        SubWorkflow subWorkflow =
                new SubWorkflow(taskReferenceName, subWorkflowName, subWorkflowVersion);
        add(subWorkflow);
        return subWorkflow;
    }

    /**
     * Terminate the workflow
     * @param taskReferenceName
     * @param terminationStatus
     * @param reason
     * @param workflowOutput
     * @return
     */
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

    /**
     * Optional helper method used to return the WorkflowBuilder while chaining the tasks.
     * @return
     */
    public TaskChain end() {
        return this;
    }
}
