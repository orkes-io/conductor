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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.ValidationError;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.tests.KitchensinkWorkflowInput;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

public class SDKTests {

    private static final String AUTHORIZATION_HEADER = "X-Authorization";

    private static final String token =
            "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwiaXNzIjoiaHR0cHM6Ly9hdXRoLm9ya2VzLmlvLyJ9..TAjV68cUkzautfxQ.Hi-CG3s4AdKTSWB9FXLfLinlugj1juItbyw7JRrhi6cPxPJh9mwmj-sE83Q9flHeeW5loozQ7DQZ28A8TRd8PAV6miikfbVV64nfRBWHYW4X5r7DFU-e-cp52CDINwFUyVNqnlswfphqxbXeeuUrPFXGIBClvUKiZUMf15LJhV73UeiyhMfG-SJ5SF8OY7DZ6ZQMrbA-4Wt7Bu-tg8kNyVKlppC2V3wsSZUZCl4nPvEznFCFucbulmlrPxRoJAPb0S0AgkXRflXDgeMTV4rEj5jFUYlh-4CfUeVS26dM3GUUEuoufmuskIY.evPcHEsIjwUZ_gLFTNvHJg";

    private static WorkflowExecutor executor;

    private static ClientFilter getAuthFilter(String token) {

        ClientFilter filter =
                new ClientFilter() {
                    @Override
                    public ClientResponse handle(ClientRequest request)
                            throws ClientHandlerException {
                        try {
                            request.getHeaders().add(AUTHORIZATION_HEADER, token);
                            return getNext().handle(request);
                        } catch (ClientHandlerException e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }
                };

        return filter;
    }

    @BeforeClass
    public static void init() throws IOException {
        String url = "https://loadtest.conductorworkflow.net/api/";
        url = "http://localhost:8080/api/";
        url = "https://play.orkes.io/api/";

        ClientFilter filter = getAuthFilter(token);
        TaskClient taskClient =
                new TaskClient(new DefaultClientConfig(), (ClientHandler) null, filter);
        taskClient.setRootURI(url);

        WorkflowClient workflowClient =
                new WorkflowClient(new DefaultClientConfig(), (ClientHandler) null, filter);
        workflowClient.setRootURI(url);

        MetadataClient metadataClient =
                new MetadataClient(new DefaultClientConfig(), (ClientHandler) null, filter);
        metadataClient.setRootURI(url);

        executor = new WorkflowExecutor(taskClient, workflowClient, metadataClient, 10);
        executor.initWorkers("com.netflix.conductor.sdk");
    }

    @AfterClass
    public static void cleanUp() {
        executor.shutdown();
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
                .task(getUserInfo)
                .decide(
                        "decide1",
                        "${workflow.input.countryCode}",
                        () -> {
                            return Arrays.asList(forkTask4);
                        },
                        () -> {
                            Map<String, List<Task<?>>> switchCase = new HashMap<>();
                            switchCase.put("US", Arrays.asList(forkTask2));
                            switchCase.put("CA", Arrays.asList(forkTask3));
                            return switchCase;
                        })
                // .subWorkflow("subflow", "sub_workflow_example", 5)
                .add(new SimpleTask("task2", "task222"));

        ConductorWorkflow<KitchensinkWorkflowInput> workflow = builder.build();
        boolean registered = workflow.registerWorkflow(true, true);
        System.out.println("Registered : " + registered);

        try {
            workflow.execute(new KitchensinkWorkflowInput("viren", "10121", "CA")).get();
        } catch (Exception e) {
            e.printStackTrace();
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
