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

import com.google.common.base.Strings;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;

/**
 * JQ Transformation task
 * See https://stedolan.github.io/jq/ for how to form the queries to parse JSON payloads
 */
public class Javascript extends Task<Javascript> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Javascript.class);

    static {
        TaskRegistry.register(TaskType.INLINE.name(), Javascript.class);
    }

    private static final String EXPRESSION_PARAMETER = "expression";

    private static final String EVALUDATOR_TYPE_PARAMETER = "evaluatorType";

    private static final String ENGINE = "nashorn";

    public Javascript(String taskReferenceName, String expression) {
        super(taskReferenceName, TaskType.INLINE);
        if(Strings.isNullOrEmpty(expression)) {
            throw new AssertionError("Null/Empty expression");
        }
        super.input(EVALUDATOR_TYPE_PARAMETER, "javascript");
        super.input(EXPRESSION_PARAMETER, expression);
    }

    Javascript(WorkflowTask workflowTask) {
        super(workflowTask);
    }

    public String getExpression() {
        return (String)getInput().get(EXPRESSION_PARAMETER);
    }

    public Javascript validate() {
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(ENGINE);
        if(scriptEngine == null) {
            LOGGER.error("missing "  + ENGINE + " engine.  Ensure you are running support JVM");
            return this;
        }

        try {

            Bindings bindings = scriptEngine.createBindings();
            bindings.put("$", new HashMap<>());
            scriptEngine.eval(getExpression(), bindings);

        } catch (ScriptException e) {
            String message = e.getMessage();
            throw new ValidationError(message);
        }
        return this;
    }
}
