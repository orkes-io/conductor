/**
 * Copyright 2021 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.conductor.sdk.workflow.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.tasks.BaseWorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import com.netflix.conductor.sdk.workflow.executor.task.WorkerExecutor;
import com.netflix.conductor.sdk.workflow.utils.MapBuilder;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

public class WorkflowExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowExecutor.class);

    private final TypeReference<List<TaskDef>> listOfTaskDefs = new TypeReference<>() {};

    private Map<String, CompletableFuture<Workflow>> runningWorkflowFutures = new ConcurrentHashMap<>();

    private Map<String, ConductorWorkflow> workflowsWithLocalWorkers = new ConcurrentHashMap<>();

    private ObjectMapper objectMapper = new ObjectMapperProvider().getObjectMapper();

    private TaskClient taskClient;

    private WorkflowClient workflowClient;

    private MetadataClient metadataClient;

    private final WorkerExecutor workerExecutor;

    private ScheduledExecutorService scheduledWorkflowMonitor = Executors.newSingleThreadScheduledExecutor();

    public WorkflowExecutor(String apiServerURL) {
        String conductorServerApiBase = apiServerURL;

        taskClient = new TaskClient();
        taskClient.setRootURI(conductorServerApiBase);

        workflowClient = new WorkflowClient();
        workflowClient.setRootURI(conductorServerApiBase);

        metadataClient = new MetadataClient();
        metadataClient.setRootURI(conductorServerApiBase);

        workerExecutor = new WorkerExecutor(apiServerURL);

        scheduledWorkflowMonitor.scheduleAtFixedRate(() -> {

            for (Map.Entry<String, CompletableFuture<Workflow>> entry : runningWorkflowFutures.entrySet()) {
                String workflowId = entry.getKey();
                CompletableFuture<Workflow> future = entry.getValue();
                Workflow workflow = workflowClient.getWorkflow(workflowId, true);
                if (workflow.getStatus().isTerminal()) {
                    ConductorWorkflow conductorWorkflow = workflowsWithLocalWorkers.get(workflowId);
                    if(conductorWorkflow != null) {
                        workerExecutor.shutdown(conductorWorkflow);
                    }
                    future.complete(workflow);
                }
            }

        }, 100, 100, TimeUnit.MILLISECONDS);

    }

    public void addWorker(ConductorWorkflow workflow, WorkerTask task) {
        workerExecutor.add(workflow, task);
    }

    public CompletableFuture<Workflow> executeWorkflow(String name, int version, Object input) {
        CompletableFuture<Workflow> future = new CompletableFuture<>();
        Map<String, Object> inputMap = objectMapper.convertValue(input, Map.class);

        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setInput(inputMap);
        request.setName(name);
        request.setVersion(version);

        String workflowId = workflowClient.startWorkflow(request);
        runningWorkflowFutures.put(workflowId, future);

        return future;
    }

    public CompletableFuture<Workflow> executeWorkflow(ConductorWorkflow conductorWorkflow, MapBuilder mapBuilder) {
        return executeWorkflow(conductorWorkflow, mapBuilder.build());
    }
    public CompletableFuture<Workflow> executeWorkflow(ConductorWorkflow conductorWorkflow, Object input) {

        workerExecutor.startPolling(conductorWorkflow);

        CompletableFuture<Workflow> future = new CompletableFuture<>();

        Map<String, Object> inputMap = objectMapper.convertValue(input, Map.class);

        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setInput(inputMap);
        request.setName(conductorWorkflow.getName());
        request.setVersion(conductorWorkflow.getVersion());
        request.setWorkflowDef(conductorWorkflow.toWorkflowDef());

        String workflowId = workflowClient.startWorkflow(request);
        runningWorkflowFutures.put(workflowId, future);

        return future;
    }

    public void loadTaskDefs(String resourcePath) throws IOException {
        InputStream resource = WorkflowExecutor.class.getResourceAsStream(resourcePath);
        if (resource != null) {
            List<TaskDef> taskDefs = objectMapper.readValue(resource, listOfTaskDefs);
            loadMetadata(taskDefs);
        }
    }

    public void loadWorkflowDefs(String resourcePath) throws IOException {
        InputStream resource = WorkflowExecutor.class.getResourceAsStream(resourcePath);
        if (resource != null) {
            WorkflowDef workflowDef = objectMapper.readValue(resource, WorkflowDef.class);
            loadMetadata(workflowDef);
        }
    }

    public void loadMetadata(WorkflowDef workflowDef) {
        metadataClient.registerWorkflowDef(workflowDef);
    }

    public void loadMetadata(List<TaskDef> taskDefs) {
        metadataClient.registerTaskDefs(taskDefs);
    }

    public void shutdown() {
        scheduledWorkflowMonitor.shutdown();
    }



}
