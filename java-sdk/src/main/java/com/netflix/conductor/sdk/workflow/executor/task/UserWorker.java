package com.netflix.conductor.sdk.workflow.executor.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

class UserWorker implements Worker {

    private static final Logger log = LoggerFactory.getLogger(UserWorker.class);

    private final WorkerTask workerTask;

    private ObjectMapper objectMapper = new ObjectMapperProvider().getObjectMapper();

    UserWorker(WorkerTask workerTask) {
        this.workerTask = workerTask;
    }

    @Override
    public String getTaskDefName() {
        return workerTask.getName();
    }

    @Override
    public TaskResult execute(Task task) {
        try {
            Function<Object, Object> execFunction = workerTask.getTaskExecutor();

            Object output = execFunction.apply(task);
            try {

                if(output instanceof TaskResult) {
                    TaskResult result = (TaskResult) output;
                    if(result.getStatus() == null) {
                        result.setStatus(TaskResult.Status.COMPLETED);
                    }
                    result.setTaskId(task.getTaskId());
                    return result;
                }

                Map<String, Object> taskOutput = objectMapper.convertValue(output, Map.class);
                task.getOutputData().putAll(taskOutput);
            }catch (RuntimeException notConvertible) {
                //This will happen if the result is a scalar value like String, Number or Boolean
                task.getOutputData().put("result", output);
            }

            task.setStatus(Task.Status.COMPLETED);
            return new TaskResult(task);

        } catch(Exception taskException) {
            log.error(taskException.getMessage(), taskException);
            task.setStatus(Task.Status.FAILED);
            task.setReasonForIncompletion(taskException.toString());
            return new TaskResult(task);
        }
    }

    @Override
    public String toString() {
        return "UserWorker:" + workerTask.getName();
    }

    @Override
    public int getPollingInterval() {
        return 10;
    }
}
