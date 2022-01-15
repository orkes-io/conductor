package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class Fork extends BaseWorkflowTask {

    private Join join;

    private BaseWorkflowTask[] forkedTasks;

    private Function<Object, Object>[] forkedTaskFunctions;

    public Fork(BaseWorkflowTask... forkedTasks) {
        super(null, TaskType.FORK_JOIN);
        this.forkedTasks = forkedTasks;
    }

    public Fork(Function<Object, Object>... forkedTaskFunctions) {
        super(null, TaskType.FORK_JOIN);
        this.forkedTaskFunctions = forkedTaskFunctions;
    }

    public Fork joinOn(String... joinOn) {
        this.join = new Join(getTaskReferenceName() + "_join", joinOn);
        return this;
    }

    @Override
    public List<WorkflowTask> updateWorkflowTask(WorkflowTask workflowTask) {
        //workflowTask.setForkTasks();
        return null;
    }
}
