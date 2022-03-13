package com.netflix.conductor.sdk;

import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.testing.WorkflowTestRunner;
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
import org.checkerframework.checker.units.qual.K;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SDKTests {

    private static final String AUTHORIZATION_HEADER = "X-Authorization";

    private static final String token = "";

    private static WorkflowExecutor executor;

    private static ClientFilter getAuthFilter(String token) {

        ClientFilter filter = new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
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
        TaskClient taskClient = new TaskClient(new DefaultClientConfig(), (ClientHandler) null, filter);
        taskClient.setRootURI(url);

        WorkflowClient workflowClient = new WorkflowClient(new DefaultClientConfig(), (ClientHandler) null, filter);
        workflowClient.setRootURI(url);

        MetadataClient metadataClient = new MetadataClient(new DefaultClientConfig(), (ClientHandler) null, filter);
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

        SimpleTask getUserInfo = new SimpleTask("get_user_info", "get_user_info")
                .input("name", ConductorWorkflow.input.get("name"));

        SimpleTask task2 = new SimpleTask("task2", "task2");
        SimpleTask forkTask1 = new SimpleTask("task2", "task22x");
        SimpleTask forkTask2 = new SimpleTask("task2", "task22x2");

        WorkflowBuilder<KitchensinkWorkflowInput> builder = new WorkflowBuilder<>(executor);
        KitchensinkWorkflowInput defaultInput = new KitchensinkWorkflowInput();
        defaultInput.setName("defaultName");
        int len = 3;
        Task[][] parallelTasks = new Task[len][1];
        for(int i = 0; i < len; i++) {
            parallelTasks[i][0] = new SimpleTask("task2", "task_parallel_" + i);;
        }

        builder
                .name("sub_workflow_example")
                .version(6)
                .ownerEmail("hello@example.com")
                .description("Example Workflow for Redfin")
                .restartable(true)
                .variables(new MyWorkflowState())
                .timeoutPolicy(WorkflowDef.TimeoutPolicy.TIME_OUT_WF, 100)
                .defaultInput(defaultInput)
                .parallel("parallel", parallelTasks)
                .task(getUserInfo)
                .decide("decide1", "${workflow.input.name}")
                    .switchCase("viren", task2)
                    .defaultCase(forkTask1)
                .subWorkflow("subflow", "sub_workflow_example", 5)
                .add(new SimpleTask("task2", "task222"));

        ConductorWorkflow<KitchensinkWorkflowInput> workflow = builder.build();
        boolean registered = workflow.registerWorkflow(true, true);
        System.out.println("Registered : " + registered);


        try {
            workflow.execute(new KitchensinkWorkflowInput("viren", "10121", "US")).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() throws ExecutionException, InterruptedException {
        CompletableFuture<Workflow> execution = executor.executeWorkflow("sub_workflow_example", null, new KitchensinkWorkflowInput());
        Workflow run = execution.get();
        System.out.println("run: " + run);
    }
}
