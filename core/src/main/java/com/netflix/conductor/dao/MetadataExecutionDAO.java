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
package com.netflix.conductor.dao;

import java.util.Map;

/** Data access layer for storing workflow executions */
public interface MetadataExecutionDAO {


    void createWorkflowMetadata(String name, Integer version, Map<String, Object> tags);

    Map<String, Object> getWorkflowMetadata(String name, Integer version);

    boolean isInProgress(String workflowName, int workflowVersion, String correlationId, String workflowId);

    int getInprogressWorkflowCount(String workflowName, int workflowVersion, String correlationId);

    void addWorkflowToLimit(String workflowName, int workflowVersion, String correlationId, String workflowId);

    void removeWorkflowFromLimit(String workflowName, int workflowVersion, String correlationId, String workflowId);

}
