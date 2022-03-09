package com.netflix.conductor.sdk.workflow.def.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Workflow task executed by a worker
 */
public class SimpleTask<T> extends Task<T> {

    private static final int ONE_HOUR = 60 * 60;

    private final ObjectMapper objectMapper = new ObjectMapperProvider().getObjectMapper();

    private boolean useGlobalTaskDef;

    private TaskDef taskDef;

    private Map<String, Object> inputTemplate = new HashMap<>();

    private T output;



    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
    private final Type type = typeToken.getType(); // or getRawType() to return Class<? super T>
    private final Type rawType = typeToken.getRawType(); // or getRawType() to return Class<? super T>

    public Type getTypeX() {
        return type;
    }

    public SimpleTask(String taskDefName, String taskReferenceName) {
        super(taskReferenceName, TaskType.SIMPLE);
        super.setName(taskDefName);
        Type superClass = getClass().getGenericSuperclass();
        ParameterizedType p = (ParameterizedType) superClass;

        System.out.println("super Class: " + p);
        System.out.println("super Class 222: " + p.getActualTypeArguments()[0]);
        Type type = typeToken.getRawType(); // or getRawType() to return Class<? super T>
        System.out.println("Type : " + type);
    }

    public static <T> SimpleTask<T> newInstance(T t) {
        SimpleTask<T> xx = new SimpleTask<T>("", "") {};
        System.out.println("hello_xx: " + xx.getTypeX());
        return xx;
    }

    /**
     * When set workflow will  use the  task definition registered in conductor.
     * Workflow registration will fail if no task definitions are found in conductor server
     * @return current instance
     */
    public SimpleTask useGlobalTaskDef() {
        this.useGlobalTaskDef = true;
        return this;
    }

    public SimpleTask useTaskDef() {
        this.taskDef = taskDef;
        this.useGlobalTaskDef = false;
        return this;
    }

    public <T>SimpleTask input(Function<Object[], T> mapper) {
        return this;
    }

    @Override
    protected com.netflix.conductor.common.metadata.workflow.WorkflowTask toWorkflowTask() {
        com.netflix.conductor.common.metadata.workflow.WorkflowTask task = super.toWorkflowTask();
        if(this.taskDef != null) {
            task.setTaskDefinition(taskDef);
        }
        return task;
    }

    @Override
    public List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> getWorkflowDefTasks() {
        List<com.netflix.conductor.common.metadata.workflow.WorkflowTask> tasks = super.getWorkflowDefTasks();
        if(useGlobalTaskDef) {
            tasks.forEach(task -> task.setTaskDefinition(null));
        }
        return tasks;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        return super.getWorkerExecutedTasks();
    }


}
