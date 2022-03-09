package com.netflix.conductor.sdk.demo;


import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.Task;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;
import com.netflix.conductor.sdk.workflow.def.tasks.Switch;
import com.netflix.conductor.sdk.workflow.def.tasks.Terminate;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;

import java.util.concurrent.TimeUnit;

public class KitchensinkDemo {

    private WorkflowExecutor executor;

    public KitchensinkDemo() {
        String url = "https://saastestapi.orkes.net/api/";
        this.executor = new WorkflowExecutor(url);
        this.executor.initWorkers("com.netflix.conductor.sdk.demo");
    }

    /**
     * Input to a task
     *  1. Task
     *  2. Map
     *  3. Unnamed Object
     *  4. Named parameters
     *
     */

    int state = 0;
    @com.netflix.conductor.sdk.task.WorkflowTask("task0")
    public TaskResult task0(com.netflix.conductor.common.metadata.tasks.Task task) {
        if(task.getRetryCount() < 1) {
            task.setStatus(com.netflix.conductor.common.metadata.tasks.Task.Status.FAILED);
            task.setReasonForIncompletion("try again!!!");
            return new TaskResult(task);
        }

        task.setStatus(com.netflix.conductor.common.metadata.tasks.Task.Status.COMPLETED);
        return new TaskResult(task);
    }


    @com.netflix.conductor.sdk.task.WorkflowTask("task1")
    public @OutputParam("greetings") String task1(@InputParam("input") GenerateQuote input) {
        return "Greetings!, " + input.getName();
    }

    @com.netflix.conductor.sdk.task.WorkflowTask("task2")
    public InsuranceQuote task2(@InputParam("name") String name) {
        InsuranceQuote quote = new InsuranceQuote();
        quote.setInsuranceAmount(100);
        quote.setName(name);
        quote.setIndustryCode("tech");

        return quote;
    }

    @com.netflix.conductor.sdk.task.WorkflowTask("task3")
    public @OutputParam("new_quote") InsuranceQuote task3(@InputParam("industryCode") String industryCode) {
        InsuranceQuote quote = new InsuranceQuote();
        quote.setInsuranceAmount(100);
        quote.setIndustryCode("TA" + industryCode);

        return quote;
    }

    public void test() throws Exception {
        try {
            _test();
        }finally {
            executor.shutdown();
            System.exit(0);
        }
    }
    public void _test() throws Exception {

        GenerateQuote input = new GenerateQuote();
        input.setIndustryCode("finance");
        input.setInsuranceAmount(100_000_000);
        input.setName("DBS Bank");

        Task<String> task1 =
                new SimpleTask<String>("task1", "task1")
                .input(ConductorWorkflow.input);;

        SimpleTask<InsuranceQuote> task2 = new SimpleTask<>("task2", "task2");
        task2.input("name", ConductorWorkflow.input.get("name"));

        SimpleTask<InsuranceQuote> task3 = new SimpleTask<>("task3", "task3");
        task3.input("industryCode", task2.taskOutput.get("industryCode"));

        Switch decide = new Switch("decision", "${workflow.input.industryCode}");
        decide.switchCase("finance", task1, task2);
        decide.switchCase("tech", task2);
        decide.defaultCase(new Terminate("bad", Workflow.WorkflowStatus.FAILED, "unsupported"));

        Switch decide2 = new Switch("decision2", (@InputParam("abc") GenerateQuote wfInput) -> {
            return wfInput.getIndustryCode();
        });

        decide2.defaultCase(new Terminate("bad2", Workflow.WorkflowStatus.FAILED, "unsupported2"));
        decide2.switchCase("finance", task3);
        decide2.input(ConductorWorkflow.input);

        ConductorWorkflow<GenerateQuote> conductorWorkflow = new WorkflowBuilder<GenerateQuote>(executor)
                .name("test_sdk_v" + System.currentTimeMillis())
                .add(new SimpleTask<>("task0", "task0"))
                .add(decide)
                .build();

        System.out.println("Ready to execute the workflow");
        Workflow execution = conductorWorkflow.execute(input).get(3, TimeUnit.MINUTES);
        System.out.println(execution);

    }

    public static void main(String[] args) throws Exception {
        new KitchensinkDemo().test();
    }
}
