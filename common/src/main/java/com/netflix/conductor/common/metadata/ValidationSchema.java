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
package com.netflix.conductor.common.metadata;

import java.util.Objects;

public class ValidationSchema {

    private Validation input;
    private Validation output;

    public static class Validation {
        private boolean validate = false;
        private String type;
        private String inlineSchema;
        private ExternalSchema externalSchema; // TBD
        private String validateAfterDate;

        public boolean isValidate() {
            return validate;
        }

        public void setValidate(boolean validate) {
            this.validate = validate;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getInlineSchema() {
            return inlineSchema;
        }

        public void setInlineSchema(String inlineSchema) {
            this.inlineSchema = inlineSchema;
        }

        public ExternalSchema getExternalSchema() {
            return externalSchema;
        }

        public void setExternalSchema(ExternalSchema externalSchema) {
            this.externalSchema = externalSchema;
        }

        public String getValidateAfterDate() {
            return validateAfterDate;
        }

        public void setValidateAfterDate(String validateAfterDate) {
            this.validateAfterDate = validateAfterDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Validation that = (Validation) o;
            return validate == that.validate
                    && Objects.equals(type, that.type)
                    && Objects.equals(inlineSchema, that.inlineSchema)
                    && Objects.equals(externalSchema, that.externalSchema)
                    && Objects.equals(validateAfterDate, that.validateAfterDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(validate, type, inlineSchema, externalSchema, validateAfterDate);
        }
    }

    public Validation getInput() {
        return input;
    }

    public Validation getOutput() {
        return output;
    }

    public void setInput(Validation input) {
        this.input = input;
    }

    public void setOutput(Validation output) {
        this.output = output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationSchema that = (ValidationSchema) o;
        return Objects.equals(input, that.input) && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output);
    }

    public static class ExternalSchema {
        private String integration;
        private String id;
        private String url;

        public String getIntegration() {
            return integration;
        }

        public void setIntegration(String integration) {
            this.integration = integration;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
