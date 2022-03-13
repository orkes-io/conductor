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

import java.util.HashMap;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;

public class Terminate extends Task<Terminate> {

    static {
        TaskRegistry.register(TaskType.TERMINATE.name(), Terminate.class);
    }

    private static final String TERMINATION_STATUS_PARAMETER = "terminationStatus";

    private static final String TERMINATION_WORKFLOW_OUTPUT = "workflowOutput";

    private static final String TERMINATION_REASON_PARAMETER = "terminationReason";

    public Terminate(String taskReferenceName, String reason) {
        this(taskReferenceName, Workflow.WorkflowStatus.FAILED, reason, new HashMap<>());
    }

    public Terminate(
            String taskReferenceName, Workflow.WorkflowStatus terminationStatus, String reason) {
        this(taskReferenceName, terminationStatus, reason, new HashMap<>());
    }

    public Terminate(
            String taskReferenceName,
            Workflow.WorkflowStatus terminationStatus,
            String reason,
            Object workflowOutput) {
        super(taskReferenceName, TaskType.TERMINATE);

        input(TERMINATION_STATUS_PARAMETER, terminationStatus.name());
        input(TERMINATION_WORKFLOW_OUTPUT, workflowOutput);
        input(TERMINATION_REASON_PARAMETER, reason);
    }

    public Terminate(WorkflowTask workflowTask) {
        super(workflowTask);
    }
}
