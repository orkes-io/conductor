package com.netflix.conductor.sdk

import com.netflix.conductor.sdk.testing.WorkflowTestRunner
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification

class AbstractSpecification extends Specification {

    @Shared
    WorkflowExecutor executor;

    def setupSpec() {
        if(executor != null) {
            return;
        }
        //String url = "http://192.168.50.99:8080/api/";
        WorkflowTestRunner testRunner = new WorkflowTestRunner(8097, "3.4.1");
        //WorkflowTestRunner testRunner = new WorkflowTestRunner(url);
        testRunner.init("com.netflix.conductor");
        executor = testRunner.getWorkflowExecutor();
    }

    def cleanupSpec() {
        executor.shutdown();
    }
}
