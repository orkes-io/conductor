/*
 * Copyright 2023 Netflix, Inc.
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
package com.netflix.conductor.common.metadata.workflow;

import com.netflix.conductor.annotations.protogen.ProtoField;
import com.netflix.conductor.annotations.protogen.ProtoMessage;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ProtoMessage
public class JumpWorkflowExecutionRequest {

    public Map<String, Object> getSkippedTasksOutput() {
        return skippedTasksOutput;
    }

    public void setSkippedTasksOutput(Map<String, Object> skippedTasksOutput) {
        this.skippedTasksOutput = skippedTasksOutput;
    }

    public Map<String, Object> getJumpTaskInput() {
        return jumpTaskInput;
    }

    public void setJumpTaskInput(Map<String, Object> jumpTaskInput) {
        this.jumpTaskInput = jumpTaskInput;
    }

    public String getTaskReferenceName() {
        return taskReferenceName;
    }

    public void setTaskReferenceName(String taskReferenceName) {
        this.taskReferenceName = taskReferenceName;
    }
    @ProtoField(id = 3)
    private Map<String, Object> skippedTasksOutput;

    @ProtoField(id = 2)
    private Map<String, Object> jumpTaskInput;

    @ProtoField(id = 1)
    @NotNull(message = "Jump task reference name cannot be null or empty")
    private String taskReferenceName;

}
