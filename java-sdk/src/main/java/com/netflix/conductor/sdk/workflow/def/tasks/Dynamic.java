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
package com.netflix.conductor.sdk.workflow.def.tasks;

import java.util.ArrayList;
import java.util.List;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

/** Wait task */
public class Dynamic extends Task {

    private static final String TASK_NAME_INPUT_PARAM = "taskToExecute";

    public Dynamic(String taskReferenceName, String dynamicTaskNameValue) {
        super(taskReferenceName, TaskType.DYNAMIC);
        super.input(TASK_NAME_INPUT_PARAM, dynamicTaskNameValue);
    }

    public List<WorkflowTask> getWorkflowDefTasks() {
        List<WorkflowTask> workflowTasks = new ArrayList<>();
        WorkflowTask task = toWorkflowTask();
        task.setDynamicTaskNameParam(TASK_NAME_INPUT_PARAM);
        workflowTasks.add(task);
        return workflowTasks;
    }
}
