package com.netflix.conductor.sdk.workflow.executor.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.workflow.def.tasks.WorkerTask;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
            Method method = execFunction.getClass().getMethods()[0];
            Object[] parameters = getInvocationParameters(method, task);
            //Object invocationResult = method.invoke(execFunction, parameters);
            Object output = execFunction.apply(parameters[0]);
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

    private Object[] getInvocationParameters(Method method, Task task) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 1 && parameterTypes[0].equals(Task.class)) {
            return new Object[]{task};
        } else if (parameterTypes.length == 1 && parameterTypes[0].equals(Map.class)) {
            return new Object[]{task.getInputData()};
        }

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] values = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] paramAnnotation = parameterAnnotations[i];
            if (paramAnnotation != null && paramAnnotation.length > 0) {
                for (Annotation ann : paramAnnotation) {
                    if (ann.annotationType().equals(InputParam.class)) {
                        InputParam ip = (InputParam) ann;
                        String name = ip.value();
                        Object value = task.getInputData().get(name);
                        values[i] = objectMapper.convertValue(value, parameterTypes[0]);
                    }
                }
            } else {
                Object input = objectMapper.convertValue(task.getInputData(), parameterTypes[0]);
                values[i] = input;
            }
        }
        return values;
    }

    @Override
    public String toString() {
        return "UserWorker:" + workerTask.getName();
    }

    @Override
    public int getPollingInterval() {
        return 1;
    }
}
