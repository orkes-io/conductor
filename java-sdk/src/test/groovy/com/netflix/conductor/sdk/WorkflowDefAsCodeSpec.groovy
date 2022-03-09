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
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask
import com.netflix.conductor.sdk.workflow.def.tasks.Switch
import com.netflix.conductor.testing.workflows.Task1Input

class WorkflowDefAsCodeSpec extends AbstractSpecification {

    @WorkflowTask("task_a")
    def taskA(TaskInput input) {
        return [
                "key1": "value1",
                "key2": input.creditScore,
                "key3": [
                        "name":"conductor",
                        "version": 4.4
                ]
        ];
    }

    @WorkflowTask("task_b")
    def taskB() {
        return [:];
    }

    @WorkflowTask("task_c")
    def taskC() {
        return [:];
    }

    def "Test Workflow definition using code"() {

        given: "Workflow builder"
        def taskA = new SimpleTask("task_a", "task_a")
        def taskB = new SimpleTask("task_b", "task_b")
        def taskC = new SimpleTask("task_c", "task_c")
        def taskC1 = new SimpleTask("task_c", "task_c1")
        def taskD = new SimpleTask("task_c", "task_d")

        def switchTask = new Switch("to_b_or_to_c", taskA.taskOutput.get("key2"))
        with(switchTask) {
            switchCase("42", taskB)
            defaultCase(taskC)
        }

        def switchTask2 = new Switch("to_d_or_not", { input ->
            return "null";
        })

        with(taskA) {
            input(
                    "name", ConductorWorkflow.input.get("name"),
                    "age", ConductorWorkflow.input.get("age"),
                    "creditScore", ConductorWorkflow.input.get("creditScore")
            )
        }
        WorkflowBuilder builder = new WorkflowBuilder(executor);
        with(builder) {
            name("test_workflow")
            version(5)
            ownerEmail("owner@example.com")
            defaultInput(new Task1Input())
            add(taskA)
            add(switchTask)
            add(taskC1)
        }
        ConductorWorkflow conductorWorkflow = builder.build()

        when: "workflow def is constructed"
        def workflowDef = conductorWorkflow.toWorkflowDef()

        then: "verify the output"
        with(workflowDef) {
            name == 'test_workflow'
            version == 5
            tasks.size() == 3
            tasks.get(0).getTaskReferenceName() == 'task_a'
        }
        def execution = conductorWorkflow.execute([
                "creditScore": 42
        ]).get()
        with(execution) {
            status == Workflow.WorkflowStatus.COMPLETED
        }


    }
}
