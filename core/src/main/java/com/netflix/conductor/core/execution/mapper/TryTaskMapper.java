/*
 * Copyright 2022 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.conductor.core.execution.mapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.core.exception.TerminateWorkflowException;
import com.netflix.conductor.core.execution.evaluators.Evaluator;
import com.netflix.conductor.model.TaskModel;
import com.netflix.conductor.model.WorkflowModel;

/**
 * An implementation of {@link TaskMapper} to map a {@link WorkflowTask} of type {@link
 * TaskType#SWITCH} to a List {@link TaskModel} starting with Task of type {@link TaskType#SWITCH}
 * which is marked as IN_PROGRESS, followed by the list of {@link TaskModel} based on the case
 * expression evaluation in the Switch task.
 */
@Component
public class TryTaskMapper implements TaskMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TryTaskMapper.class);

    private final Map<String, Evaluator> evaluators;

    @Autowired
    public TryTaskMapper(Map<String, Evaluator> evaluators) {
        this.evaluators = evaluators;
    }

    @Override
    public String getTaskType() {
        return TaskType.TRY.name();
    }


    @Override
    public List<TaskModel> getMappedTasks(TaskMapperContext taskMapperContext) {
        LOGGER.debug("TaskMapperContext {} in TryTaskMapper", taskMapperContext);
        List<TaskModel> tasksToBeScheduled = new LinkedList<>();
        WorkflowTask workflowTask = taskMapperContext.getWorkflowTask();
        WorkflowModel workflowModel = taskMapperContext.getWorkflowModel();
        Map<String, Object> taskInput = taskMapperContext.getTaskInput();
        int retryCount = taskMapperContext.getRetryCount();

        TaskModel switchTask = taskMapperContext.createTaskModel();
        switchTask.setTaskType(TaskType.TASK_TYPE_TRY);
        switchTask.setTaskDefName(TaskType.TASK_TYPE_TRY);
        switchTask.getInputData().putAll(taskInput);
        switchTask.setStartTime(System.currentTimeMillis());
        switchTask.setStatus(TaskModel.Status.IN_PROGRESS);
        tasksToBeScheduled.add(switchTask);
        return tasksToBeScheduled;
    }
}
