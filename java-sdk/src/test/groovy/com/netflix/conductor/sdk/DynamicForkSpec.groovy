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
import com.netflix.conductor.sdk.task.WorkflowTask
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder
import com.netflix.conductor.sdk.workflow.def.tasks.DoWhile
import com.netflix.conductor.sdk.workflow.def.tasks.DynamicFork
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask

class DynamicForkSpec extends AbstractSpecification {

    @WorkflowTask("fork_tasks")
    def forkTaskGen() {
        def task1 = new SimpleTask("task_1", "task_1")
        def task2 = new SimpleTask("task_2", "task_2")
        return ["tasks": [task1, task2]]
    }

    @WorkflowTask("fork_input")
    def forkTaskInputGen() {
        return [
                "task_1": [:],
                "task_2": [:]
        ]
    }

    def "Test DynamicFork"() {

        given: "DynamicFork Task Configuration"
        def fork_tasks = new SimpleTask("fork_tasks", "fork_tasks")
        def fork_input = new SimpleTask("fork_input", "fork_input")
        def dynamicFork = new DynamicFork("dyn_fork",
                "\${fork_tasks.output.tasks}",
                "\${fork_input.output}");

        when: "A dynamic fork task is created"

        then: "Verify there is a join task created automatically"
        with(dynamicFork.getWorkflowDefTasks()) {
            it.size() == 2
            it.get(1).getType() == 'JOIN'
        }

        when: "A workflow is executed with dynamic fork"
        def conductorWorkflow =
                new WorkflowBuilder(executor)
                        .name("test_dynamic_fork")
                        .ownerEmail("owner@example.com")
                        .add(fork_tasks)
                        .add(fork_input)
                        .add(dynamicFork)
                        .build()
        def execution = conductorWorkflow.execute([:]).get()

        then:
        with(execution) {
            it.status == Workflow.WorkflowStatus.COMPLETED
        }

    }


}
