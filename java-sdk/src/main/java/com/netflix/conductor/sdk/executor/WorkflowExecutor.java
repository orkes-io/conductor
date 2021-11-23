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
package com.netflix.conductor.sdk.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.ClassPath;
import com.google.common.util.concurrent.Uninterruptibles;
import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.executor.healthcheck.HealthCheckClient;
import com.netflix.conductor.sdk.task.executor.WorkerExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class WorkflowExecutor {

    private Map<String, Method> workerExecutors = new HashMap<>();

    private Map<String, Object> workerClassObjs = new HashMap<>();

    private TaskRunnerConfigurer taskRunner;

    private TaskClient taskClient;

    private WorkflowClient workflowClient;

    private MetadataClient metadataClient;

    private HealthCheckClient healthCheck;

    private Map<String, CountDownLatch> runningWorkflows = new ConcurrentHashMap<>();

    private String localServerPort;

    private Process serverProcess;

    private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();

    private final CountDownLatch serverProcessLatch = new CountDownLatch(1);

    private final ObjectMapper om = new ObjectMapper();

    private static final WorkflowExecutor instance;

    static {
        String localServerPort = Optional
                .ofNullable(System.getProperty("conductorServerPort"))
                .orElse("8080");
        String serverURL = Optional
                .ofNullable(System.getProperty("conductorServerURL"))
                .orElse("http://localhost:" + localServerPort + "/");
        instance = new WorkflowExecutor(serverURL, localServerPort);
    }

    public static final WorkflowExecutor getInstance() {
        return instance;
    }

    private WorkflowExecutor(String serverURL, String localServerPort) {
        this.localServerPort = localServerPort;
        String conductorServerApiBase = serverURL + "api/";

        taskClient = new TaskClient();
        taskClient.setRootURI(conductorServerApiBase);

        workflowClient = new WorkflowClient();
        workflowClient.setRootURI(conductorServerApiBase);

        metadataClient = new MetadataClient();
        metadataClient.setRootURI(conductorServerApiBase);

        healthCheck = new HealthCheckClient(serverURL + "health");
    }

    /**
     * Starts the local server and workers and waits for any running workflows to be completed.
     * @param basePackages list of packages - comma separated - to scan for annotated worker implementation
     */
    public void startServerAndPolling(String basePackages) {
        startLocalServer();
        initWorkers(basePackages);
    }

    /**
     * Starts the local server. Downloads the latest conductor build from the maven repo
     * If you want to start the server from a specific download location, set `repositoryURL` system property with the
     * link to the actual downloadable server boot jar file.
     *
     * <b>System Properties that can be set</b>
     * conductorVersion: when specified, uses this version of conductor to run tests (and downloads from maven repo)
     * repositoryURL: full url where the server boot jar can be downloaded from. This can be a public repo or internal
     * repository, allowing full control over the location and version of the conductor server
     */
    public void startLocalServer() {
        try {

            String conductorVersion = Optional
                    .ofNullable(System.getProperty("conductorVersion"))
                    .orElse("3.3.4");

            String repositoryURL = Optional
                    .ofNullable(System.getProperty("repositoryURL"))
                    .orElse("https://repo1.maven.org/maven2/com/netflix/conductor/conductor-server/" + conductorVersion + "/conductor-server-" + conductorVersion + "-boot.jar");

            Runtime.getRuntime().addShutdownHook(new Thread(()->shutdown()));
            installAndStartServer(repositoryURL, localServerPort);
            healthCheckExecutor.scheduleAtFixedRate(()->{
                try {
                    if(serverProcessLatch.getCount() > 0) {
                        boolean isRunning = healthCheck.isServerRunning();
                        if(isRunning) {
                            serverProcessLatch.countDown();
                        }
                    }
                }catch(Throwable e) {
                    return;
                }
            }, 100, 100, TimeUnit.MILLISECONDS);
            Uninterruptibles.awaitUninterruptibly(serverProcessLatch, 1, TimeUnit.MINUTES);

            if(serverProcessLatch.getCount() > 0) {
                throw new RuntimeException("Server not healthy");
            }
            healthCheckExecutor.shutdownNow();

            ses.scheduleAtFixedRate(()->{

                runningWorkflows.entrySet().forEach(e -> {
                    String workflowId = e.getKey();
                    CountDownLatch latch = e.getValue();
                    Workflow workflow = workflowClient.getWorkflow(workflowId, false);
                    if(workflow.getStatus().isTerminal()) {
                        latch.countDown();
                    }
                });
            }, 100, 100, TimeUnit.MILLISECONDS);

        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Finds any worker implementation and starts polling for tasks
     * @param basePackage list of packages - comma separated - to scan for annotated worker implementation
     */
    public void initWorkers(String basePackage) {
        scanWorkers(basePackage);
        startPolling();
    }



    private void startPolling() {

        List<Worker> executors = new ArrayList<>();
        workerExecutors.entrySet().forEach( e-> {
            String taskName = e.getKey();
            Method method = e.getValue();
            Object obj = workerClassObjs.get(taskName);
            WorkerExecutor executor = new WorkerExecutor(taskName, method, obj);
            executors.add(executor);
        });
        if(executors.isEmpty()) {
            return;
        }

        taskRunner = new TaskRunnerConfigurer.Builder(taskClient, executors)
                .withThreadCount(executors.size())
                .build();
        taskRunner.init();
    }



    private synchronized void installAndStartServer(String repositoryURL, String localServerPort) throws IOException {

        if(serverProcess != null) {
            return;
        }

        String configFile = WorkflowExecutor.class.getResource("/test-server.properties").getFile();
        String tempDir = System.getProperty("java.io.tmpdir");
        Path serverFile = Paths.get(tempDir, "conductor-server.jar");
        if(!Files.exists(serverFile)) {
            Files.copy(new URL(repositoryURL).openStream(), serverFile);
        }
        serverProcess = Runtime.getRuntime().exec("java -Dserver.port=" + localServerPort + " -DCONDUCTOR_CONFIG_FILE=" + configFile + " -jar " + serverFile.toString());
        InputStream in = serverProcess.getInputStream();
        InputStream err = serverProcess.getErrorStream();
        BufferedReader error = new BufferedReader(new InputStreamReader(serverProcess.getErrorStream()));
        BufferedReader op = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));

        Executors.newSingleThreadScheduledExecutor().execute(()->{

            String line = null;
            while(true) {
                try {
                    if (!((line=error.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //copy to standard error
                System.err.println(line);
            }
        });

        Executors.newSingleThreadScheduledExecutor().execute(()->{

            String line = null;
            while(true) {
                try {
                    if (!((line=op.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //copy to standard out
                System.out.println(line);
            }
        });

    }

    public void shutdown() {
        if(serverProcess != null) {
            serverProcess.destroyForcibly();
        }
        if(taskRunner != null) {
            taskRunner.shutdown();
        }
        ses.shutdown();
    }

    /**
     * Starts a workflow execution and waits till
     * @param name
     * @param version
     * @param input
     * @return
     */
    public Workflow executeWorkflow(String name, int version, Map<String, Object> input) {
        StartWorkflowRequest request = new StartWorkflowRequest();
        request.setInput(input);
        request.setName(name);
        request.setVersion(version);

        String workflowId = workflowClient.startWorkflow(request);
        CountDownLatch latch = new CountDownLatch(1);
        runningWorkflows.put(workflowId, latch);
        Uninterruptibles.awaitUninterruptibly(latch);
        runningWorkflows.remove(workflowId);
        return workflowClient.getWorkflow(workflowId, true);
    }



    TypeReference<List<TaskDef>> listOfTaskDefs = new TypeReference<List<TaskDef>>() {};

    public void loadTaskDefs(String resourcePath) throws IOException {
        InputStream resource = WorkflowExecutor.class.getResourceAsStream(resourcePath);
        if(resource != null) {
            List<TaskDef> taskDefs = om.readValue(resource, listOfTaskDefs);
            loadMetadata(taskDefs);
        }
    }

    public void loadWorkflowDefs(String resourcePath) throws IOException {
        InputStream resource = WorkflowExecutor.class.getResourceAsStream(resourcePath);
        if(resource != null) {
            WorkflowDef workflowDef = om.readValue(resource, WorkflowDef.class);
            loadMetadata(workflowDef);
        }
    }

    public void loadMetadata(WorkflowDef workflowDef) {
        metadataClient.registerWorkflowDef(workflowDef);
    }

    public void loadMetadata(List<TaskDef> taskDefs) {
        metadataClient.registerTaskDefs(taskDefs);
    }

    private void scanWorkers(String basePackage) {
        try {
            List<String> packagesToScan = new ArrayList<>();
            if(basePackage != null) {
                String[] packages = basePackage.split(",");
                for(String pkg : packages) {
                    packagesToScan.add(pkg);
                }
            }

            long s = System.currentTimeMillis();
            ClassPath.from(WorkflowExecutor.class.getClassLoader()).getAllClasses().stream().forEach(classMeta -> {
                String name = classMeta.getName();
                if(!includePackage(packagesToScan, name)) {
                    return;
                }
                try {
                    Class<?> clazz = classMeta.load();
                    Object obj = clazz.getConstructor().newInstance();
                    scanClass(clazz, obj);
                }catch(Throwable t) {

                }
            });
            long e = System.currentTimeMillis();
            System.out.println("Took " + (e-s) + " ms to scan all the classes");

        }catch(Exception e) {
            e.printStackTrace();
        }

    }

    private boolean includePackage(List<String> packagesToScan, String name) {
        for(String scanPkg : packagesToScan) {
            if(name.startsWith(scanPkg))
                return true;
        }
        return false;
    }

    private void scanClass(Class<?> clazz, Object obj) {
        for(Method method : clazz.getMethods()) {
            WorkflowTask annotation = method.getAnnotation(WorkflowTask.class);
            if(annotation == null) {
                continue;
            }
            String name = annotation.value();
            workerExecutors.put(name, method);
            workerClassObjs.put(name, obj);
        }
    }
}

