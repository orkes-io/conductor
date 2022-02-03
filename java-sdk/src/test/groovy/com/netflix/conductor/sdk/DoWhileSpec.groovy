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

        then: "Verify there are no worker executed tasks"
        with(doWhile.getWorkerExecutedTasks()) {
            it.isEmpty()
        }
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
        def doWhile2 = new DoWhile("execute_2_times", 2, { Task1Input input ->
            return 42;
        });
        def conductorWorkflow =
                new WorkflowBuilder(executor)
                        .name("test_do_while")
                        .ownerEmail("owner@example.com")
                        .add(doWhile)
                        .add(doWhile2)
                        .build();

        when: "Conductor workflow is executed"
        Workflow execution = conductorWorkflow.execute([:]).get();

        then: "Verify that the workflow has executed successfully"
        with(execution) {
            execution.status.successful
        }
    }
}
