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
package com.netflix.conductor.sdk;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.testing.LocalServerRunner;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.ValidationError;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.tests.KitchensinkWorkflowInput;

import static org.junit.Assert.fail;

public class SDKTests {

    private static final String AUTHORIZATION_HEADER = "X-Authorization";

    private static WorkflowExecutor executor;

    private static LocalServerRunner runner;

    @BeforeClass
    public static void init() throws IOException {
        runner = new LocalServerRunner(8080, "3.5.3");
        runner.startLocalServer();

        executor = new WorkflowExecutor("http://localhost:8080/api/", 1);

        executor.initWorkers("com.netflix.conductor.sdk");
    }

    @AfterClass
    public static void cleanUp() {
        executor.shutdown();
        runner.shutdown();
    }

    @WorkflowTask("get_user_info")
    public @OutputParam("zipCode") String getZipCode(@InputParam("name") String userName) {
        return "95014";
    }

    @WorkflowTask("task2")
    public @OutputParam("greetings") String task2() {
        return "Hello World";
    }

    @WorkflowTask("task3")
    public @OutputParam("greetings") String task3() {
        return "Hello World-3";
    }

    @Test
    public void test() throws ValidationError {

        SimpleTask getUserInfo =
                new SimpleTask("get_user_info", "get_user_info")
                        .input("name", ConductorWorkflow.input.get("name"));

        SimpleTask task2 = new SimpleTask("task2", "task2");
        SimpleTask forkTask1 = new SimpleTask("task2", "task22x");
        SimpleTask forkTask2 = new SimpleTask("task2", "ship_to_US");
        SimpleTask forkTask3 = new SimpleTask("task2", "ship_to_CA");
        SimpleTask forkTask4 = new SimpleTask("task2", "ship_to_Outsideof_NA");

        SimpleTask cupertino = new SimpleTask("task2", "cupertino");
        SimpleTask nyc = new SimpleTask("task2", "nyc");

        WorkflowBuilder<KitchensinkWorkflowInput> builder = new WorkflowBuilder<>(executor);
        KitchensinkWorkflowInput defaultInput = new KitchensinkWorkflowInput();
        defaultInput.setName("defaultName");
        int len = 3;
        Task[][] parallelTasks = new Task[len][1];
        for (int i = 0; i < len; i++) {
            parallelTasks[i][0] = new SimpleTask("task2", "task_parallel_" + i);
        }

        builder.name("sub_workflow_example")
                .version(6)
                .ownerEmail("hello@example.com")
                .description("Example Workflow for Redfin")
                .restartable(true)
                .variables(new MyWorkflowState())
                .timeoutPolicy(WorkflowDef.TimeoutPolicy.TIME_OUT_WF, 100)
                .defaultInput(defaultInput)
                .parallel("parallel", parallelTasks)
                .task(getUserInfo.input("a", "b"))
                .decide("decide2", "${workflow.input.zipCode}")
                .switchCase("95014", cupertino)
                .switchCase("10121", nyc)
                .end()
                // .subWorkflow("subflow", "sub_workflow_example", 5)
                .add(new SimpleTask("task2", "task222"));

        ConductorWorkflow<KitchensinkWorkflowInput> workflow = builder.build();
        boolean registered = workflow.registerWorkflow(true, true);
        System.out.println("Registered : " + registered);

        try {
            Workflow run =
                    workflow.execute(new KitchensinkWorkflowInput("viren", "10121", "CA")).get();
            System.out.println("executed http://localhost:5000/execution/" + run.getWorkflowId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void test2() throws ExecutionException, InterruptedException {
        CompletableFuture<Workflow> execution =
                executor.executeWorkflow(
                        "sub_workflow_example",
                        null,
                        new KitchensinkWorkflowInput("viren", "95014", "US"));
        Workflow run = execution.get();
        System.out.println("run: " + run);
    }
}
