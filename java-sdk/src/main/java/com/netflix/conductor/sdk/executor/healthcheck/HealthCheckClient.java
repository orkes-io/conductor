/**
 * Copyright 2021 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.conductor.sdk.executor.healthcheck;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class HealthCheckClient {

    private final String healthCheckURL;

    private final ObjectMapper om;

    public HealthCheckClient(String healthCheckURL) {
        this.healthCheckURL = healthCheckURL;
        this.om = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public boolean isServerRunning() {
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(healthCheckURL).openStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            HealthCheckResults healthCheckResults = om.readValue(response.toString(), HealthCheckResults.class);
            return healthCheckResults.healthy;
        }catch(Throwable t) {
            return false;
        }

    }

    private static final class HealthCheckResults {

        private boolean healthy;

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }
    }
}
