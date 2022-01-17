package com.netflix.conductor.sdk.workflow.executor.task;

import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerExecutor.class);

    private Map<ConductorWorkflow, Set<Worker>> workers;

    private Map<ConductorWorkflow, TaskRunnerConfigurer> taskRunners;

    private Map<ConductorWorkflow, Boolean> taskRunnerState;

    private TaskClient taskClient;

    public WorkerExecutor(String apiURL){
        this.workers = new HashMap<>();
        this.taskClient = new TaskClient();
        this.taskClient.setRootURI(apiURL);

        this.taskRunners = new ConcurrentHashMap<>();
        this.taskRunnerState = new ConcurrentHashMap<>();
    }

    public synchronized void add(ConductorWorkflow workflow, WorkerTask workerTask) {
        this.workers.computeIfAbsent(workflow, wf -> new HashSet<>());
        this.workers.get(workflow).add(new UserWorker(workerTask));
    }

    public synchronized void startPolling(ConductorWorkflow workflow) {
        taskRunnerState.computeIfAbsent(workflow, wf -> Boolean.FALSE);

        if( Boolean.TRUE.equals(taskRunnerState.get(workflow)) ) {
            LOGGER.warn("Workers are already running for {}/{}, skipping worker initialization", workflow.getName(), workflow.getVersion());
            return;
        }
        TaskRunnerConfigurer taskRunner = taskRunners.get(workflow);
        Set<Worker> taskWorkers = workers.get(workflow);
        if(taskWorkers == null || taskWorkers.isEmpty()) {
            System.out.println("No task workers for this workflow");
            return;
        }

        if(taskRunner == null) {
            taskRunner = new TaskRunnerConfigurer.Builder(taskClient, taskWorkers)
                    .withThreadCount(taskWorkers.size())
                    .withWorkerNamePrefix(workflow.getName())
                    .build();
            taskRunners.put(workflow, taskRunner);
        }
        taskRunner.init();
        taskRunnerState.put(workflow, Boolean.TRUE);
        System.out.println("Started polling for " + workflow.getName());
        LOGGER.info("Started polling workers for {}/{}", workflow.getName(), workflow.getVersion());
    }

    public void shutdown(ConductorWorkflow workflow) {
        taskRunnerState.computeIfAbsent(workflow, wf -> Boolean.FALSE);

        if( Boolean.FALSE.equals(taskRunnerState.get(workflow)) ) {
            return;
        }
        TaskRunnerConfigurer taskRunner = taskRunners.get(workflow);
        if(taskRunner != null) {
            taskRunner.shutdown();
        }
        taskRunnerState.put(workflow, Boolean.FALSE);
        LOGGER.info("Stopped polling workers for {}/{}", workflow.getName(), workflow.getVersion());
    }



}
