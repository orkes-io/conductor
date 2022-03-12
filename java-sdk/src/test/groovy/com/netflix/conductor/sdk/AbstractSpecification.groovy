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

import com.netflix.conductor.sdk.testing.WorkflowTestRunner
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification


@Slf4j
class AbstractSpecification extends Specification {

    @Shared
    WorkflowExecutor executor;

    @Shared
    WorkflowTestRunner testRunner;

    def setupSpec() {
        log.info("Setting up spec: " + testRunner)
        if(executor != null) {
            return;
        }
        testRunner = new WorkflowTestRunner(8097, "3.5.2");
        testRunner.init("com.netflix.conductor");
        executor = testRunner.getWorkflowExecutor();
    }

    def cleanupSpec() {
        log.info("cleaning up spec")
        testRunner.shutdown();
    }
}
