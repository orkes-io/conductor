/*
 * Copyright 2022 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.conductor.sdk

import com.netflix.conductor.common.run.Workflow
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder
import com.netflix.conductor.sdk.workflow.def.tasks.DoWhile
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask
import com.netflix.conductor.testing.workflows.Task1Input

class DoWhileSpec extends AbstractSpecification {

    def "Test DoWhile Definition"() {

        given: "DoWhile Task Configuration"
        def task1 = new SimpleTask("task_1", "task_1")
        def doWhile = new DoWhile("execute_3_times", 3, task1);
        when: "A do while is configured to execute 3 times"

        then: "Verify that the doWhile contains task_1 as the loop task"
        with(doWhile.getWorkflowDefTasks()) {
            it.size() == 1
            it.get(0).getLoopCondition() != null
            it.get(0).getTaskReferenceName() == 'execute_3_times'
            it.get(0).loopOver.size() == 1
            it.get(0).loopOver.get(0).getTaskReferenceName() == 'task_1'
        }
    }

    def "Test DoWhile workflow execution"() {
        given: "Workflow with doWhile"
        def task1 = new SimpleTask("task_1", "task_1")
        def doWhile = new DoWhile("execute_3_times", 3, task1);
        def conductorWorkflow =
                new WorkflowBuilder(executor)
                        .name("test_do_while")
                        .ownerEmail("owner@example.com")
                        .add(doWhile)
                        .build();

        when: "Conductor workflow is executed"
        Workflow execution = conductorWorkflow.execute([:]).get();

        then: "Verify that the workflow has executed successfully"
        with(execution) {
            execution.status.successful
        }
    }
}
