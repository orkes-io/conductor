package com.netflix.conductor.sdk;

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
import org.checkerframework.checker.units.qual.K;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

public class SDKTests {

    private static WorkflowTestRunner testRunner;

    private static WorkflowExecutor executor;

    @BeforeClass
    public static void init() throws IOException {
        executor = new WorkflowExecutor("https://loadtest.conductorworkflow.net/api/");
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

        SimpleTask getUserInfo = new SimpleTask("get_user_info", "get_user_info");
        getUserInfo.input("name", ConductorWorkflow.input.get("name"));

        SimpleTask task2 = new SimpleTask("task2", "task2");
        SimpleTask forkTask1 = new SimpleTask("task2", "task22x");
        SimpleTask forkTask2 = new SimpleTask("task2", "task22x2");

        DynamicFork dynamicFork = new DynamicFork("dyn1", "", "");

        WorkflowBuilder<KitchensinkWorkflowInput> builder = new WorkflowBuilder<>(executor);
        KitchensinkWorkflowInput defaultInput = new KitchensinkWorkflowInput();
        defaultInput.setName("defaultName");

        builder
                .name("redfin_example")
                .version(5)
                .ownerEmail("hello@example.com")
                .description("Example Workflow for Redfin")
                .restartable(true)
                .timeoutPolicy(WorkflowDef.TimeoutPolicy.TIME_OUT_WF, 10)
                .defaultInput(defaultInput)
                .task(getUserInfo)
                .task(
                        new Http("http_task").url("https://weatherdbi.herokuapp.com/data/weather/${workflow.input.zipCode}")
                                .input("zipCode", "${workflow.input.zipCode}")
                                .readTimeout(10_000)
                )
                .parallel("parallel", new Task[][]{{forkTask1}, {forkTask2}}).end()
                .decide("decide", getUserInfo.taskOutput.get("zipCode"))
                    .switchCase("95014", task2)
                    .defaultCase(new Terminate("terminate", Workflow.WorkflowStatus.FAILED, "I don't ship there", new HashMap<>()))
                    .end()
                .loop("run_twice", 2, new SimpleTask("task3", "task3"))
                    .input("key", "value")
                    .end()
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
}
