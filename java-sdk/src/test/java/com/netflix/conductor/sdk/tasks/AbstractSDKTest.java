package com.netflix.conductor.sdk.tasks;

import com.netflix.conductor.sdk.testing.WorkflowTestRunner;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractSDKTest {

    protected static WorkflowExecutor executor;

    @BeforeAll
    public static void init() {
        String url = "http://192.168.50.99:8080/api/";
        //WorkflowTestRunner testRunner = new WorkflowTestRunner(8096, "3.4.1");
        WorkflowTestRunner testRunner = new WorkflowTestRunner(url);
        testRunner.init("com.netflix.conductor");
        executor = testRunner.getWorkflowExecutor();
    }
}
