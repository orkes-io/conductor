/**
 * Copyright 2021 Netflix, Inc.
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
package com.netflix.conductor.testing.workflows;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.IpParam;
import com.netflix.conductor.sdk.task.OpParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.executor.WorkflowExecutor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KitchenSinkTest {
    private static WorkflowExecutor executor;

    @BeforeClass
    public static void init() {
        executor = WorkflowExecutor.getInstance();
        executor.startServerAndPolling("com.netflix.conductor");
    }

    @AfterClass
    public static void cleanUp() {
        WorkflowExecutor.getInstance().shutdown();
    }

    @Test
    public void testKitchenSink() throws Exception {



        executor.loadTaskDefs("/tasks.json");
        executor.loadWorkflow("/simple_workflow.json");

        Map<String, Object> input = new HashMap<>();
        input.put("task2Name", "task_2");
        input.put("mod", "1");
        input.put("oddEven", "12");

        Workflow workflow = executor.executeWorkflow("Decision_TaskExample", 1, input, "test");
        assertNotNull(workflow);
        assertNotNull(workflow.getOutput());
        assertEquals("b", workflow.getOutput().get("a"));      //task10's output
    }

    @Test
    public void testKitchenSink2() throws Exception {

        Map<String, Object> input = new HashMap<>();
        input.put("task2Name", "task_2");
        input.put("mod", "1");
        input.put("oddEven", "12");

        Workflow workflow = executor.executeWorkflow("Decision_TaskExample", 1, input, "test");
        assertNotNull(workflow);
        assertNotNull(workflow.getOutput());
        assertEquals(100, workflow.getOutput().get("c"));      //task10's output

    }

    @WorkflowTask("task_1")
    public Map<String, Object> task1(Task1Input input) {
        Map<String, Object> result = new HashMap<>();
        result.put("input", input);
        return result;
    }

    @WorkflowTask("task_2")
    public TaskResult task2(Task task) {
        task.setStatus(Task.Status.COMPLETED);
        return new TaskResult(task);
    }

    @WorkflowTask("task_10")
    public TaskResult task10(Task task) {
        task.setStatus(Task.Status.COMPLETED);
        task.getOutputData().put("a", "b");
        task.getOutputData().put("c", 100);
        task.getOutputData().put("x", false);
        return new TaskResult(task);
    }

    @WorkflowTask("task_8")
    public TaskResult task8(Task task) {
        task.setStatus(Task.Status.COMPLETED);
        return new TaskResult(task);
    }

    @WorkflowTask("task_5")
    public TaskResult task5(Task task) {
        task.setStatus(Task.Status.COMPLETED);
        return new TaskResult(task);
    }

    @WorkflowTask("task_3")
    public @OpParam("z1") String task3(
            @IpParam("taskToExecute") String p1
    ) {
        return "output of task3, p1=" + p1;
    }



    @WorkflowTask("task_30")
    public Map<String, Object> task30(Task task) {
        Map<String, Object> output = new HashMap<>();
        output.put("v1", "b");
        output.put("v2", Arrays.asList("one","two", 3));
        output.put("v3", 5);
        return output;

    }

    @WorkflowTask("task_31")
    public Map<String, Object> task31(Task task) {
        Map<String, Object> output = new HashMap<>();
        output.put("a1", "b");
        output.put("a2", Arrays.asList("one","two", 3));
        output.put("a3", 5);
        return output;

    }

    @WorkflowTask("HTTP")
    public Map<String, Object> http(Task task) {
        Map<String, Object> output = new HashMap<>();
        output.put("a1", "b");
        output.put("a2", Arrays.asList("one","two", 3));
        output.put("a3", 5);
        return output;

    }

    @WorkflowTask("EVENT")
    public Map<String, Object> event(Task task) {
        Map<String, Object> output = new HashMap<>();
        output.put("a1", "b");
        output.put("a2", Arrays.asList("one","two", 3));
        output.put("a3", 5);
        return output;

    }
}
