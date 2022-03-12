package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.testing.LocalServerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class TaskRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRegistry.class);

    private static Map<String, Class<? extends Task>> taskTypeMap = new HashMap<>();

    public static void register(String taskType, Class<? extends Task> taskImplementation) {
        taskTypeMap.put(taskType, taskImplementation);
    }

    public static Task<?> getTask(WorkflowTask workflowTask) {
        Class<? extends Task> clazz = taskTypeMap.get(workflowTask.getType());
        if(clazz == null) {
            throw new UnsupportedOperationException("No support to convert " + workflowTask.getType());
        }
        Task<?> task = null;
        try {
            task = clazz.getDeclaredConstructor(WorkflowTask.class).newInstance(workflowTask);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return task;
        }
        return task;
    }
}
