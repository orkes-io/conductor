package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import com.netflix.conductor.sdk.workflow.executor.task.WorkerExecutor;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TestWorkflowExecutions {

    @Test
    public void testExecuteSimple() throws ExecutionException, InterruptedException, JsonProcessingException {
        String url = "https://conductorapp.trescommas.dev/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);


        WorkflowBuilder builder = new WorkflowBuilder(executor);
        ConductorWorkflow conductorWorkflow = builder
                .name("test_wf_as_code")
                .add("my_task", o -> 42)
                .add(new Fork(a->1, b -> 2))
                .output("result", "${my_task.output.result}")
                .build();

        System.out.println(new ObjectMapperProvider().getObjectMapper().writeValueAsString(conductorWorkflow.toWorkflowDef()));

        Workflow executed = conductorWorkflow.execute(new HashMap<>()).get();
        System.out.println(executed);
    }
}
