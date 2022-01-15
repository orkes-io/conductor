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

    public void testComplex() throws ExecutionException, InterruptedException {
        WorkflowExecutor executor = new WorkflowExecutor(null);


        Switch decision = new Switch("decide_1", o -> {
            return null;
        })
                .defaultCase("task_2")
                .switchCase("fedex", "task_3", "task_21")
                .switchCase("ups", "task_4", "task_21")
                .switchCase("international",
                        new Switch("int_courier", "${workflow.input.address.shipping_country}")
                                .defaultCase(new Terminate("term_unsupported", Workflow.WorkflowStatus.FAILED, "bad"))
                                .switchCase("india", "blue_dart")
                                .switchCase("germany", "dhl")
                );

        Fork fork = new Fork(
                new WorkerTask("task_14" ),
                new WorkerTask("task_15" ))
                .joinOn("task_14", "task_15");
        fork.setTaskReferenceName("hello");

        DoWhile doWhile = new DoWhile("do_while1", "@.loopTak['iteration'] < 10",
                new WorkerTask("task_2").input("name", "${workflow.input.name}")
        );

        WorkflowBuilder builder = new WorkflowBuilder(executor);
        ConductorWorkflow conductorWorkflow = builder
                .name("test_wf_as_code")
                .version(1)
                .description("Test workflow as a code")
                .add(o -> {
                    return 42;
                })
                .build();

        Workflow executed = conductorWorkflow.execute(new HashMap<>()).get();
        System.out.println(executed);
    }
}
