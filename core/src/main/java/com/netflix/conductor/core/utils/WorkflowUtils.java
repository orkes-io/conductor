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
package com.netflix.conductor.core.utils;

import java.util.HashMap;
import java.util.Map;

public class WorkflowUtils {
    /**
     * Used to populate System Metadata and Dynamic flag to workflow input
     *
     * @param input Map that needs the metadata to be added
     */
    public static void populateDynamicFlagInSystemMetadata(Map<String, Object> input) {
        HashMap<String, Object> systemMetadata = new HashMap();
        systemMetadata.put("dynamic", true);
        input.put("_system_metadata", systemMetadata);
    }
}
