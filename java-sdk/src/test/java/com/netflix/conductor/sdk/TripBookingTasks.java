package com.netflix.conductor.sdk;

import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.Task;
import com.netflix.conductor.sdk.workflow.def.tasks.Fork;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;
import com.netflix.conductor.sdk.workflow.def.tasks.Switch;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;

/**
 * Notes:
 * 1. Allow task deprecation
 * 2. Stop polling if backward compatibility is changed (allow override)
 * 3.
 */
public class TripBookingTasks {

    @com.netflix.conductor.sdk.task.WorkflowTask(("book_hotel"))
    public @OutputParam("hotelBookingRef") String bookHotel(
            @InputParam("hotelName") String hotelName,
            @InputParam(value = "starRatings", required = true) int starRatings) {
        return null;
    }

    @com.netflix.conductor.sdk.task.WorkflowTask(("book_flight"))
    public String bookFlight(TripBookingInput flightBookingDetails) {
        return null;
    }

    public ConductorWorkflow testWorkflow() throws Exception {
        String url = "https://saastestapi.orkes.net/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);
        executor.initWorkers(TestTypedWorkflowTasks.class.getPackageName());

        WorkflowBuilder<TripBookingInput> builder = new WorkflowBuilder<>(executor);
        Task[][] forkTasks = new Task[2][];
        forkTasks[0] = new Task[]{new SimpleTask("task_1", "task_1")};
        Fork fork = new Fork("fork0", forkTasks);

        Switch sw = new Switch("car_booking_cases", "${workflow.input.carAgency}");
        sw
                .switchCase("hertz", new SimpleTask("book_hertz", "book_hertz"))
                .defaultCase(fork);

        ConductorWorkflow<TripBookingInput> workflow = builder
                .name("travel_booking_workflow")
                .version(1)
                .failureWorkflow("compensate")
                .add(new SimpleTask("book_hotel", "book_hotel"))
                .add(fork)
                .build();

        TripBookingInput input = new TripBookingInput();
        input.setCarType("hertz");

        Workflow executed = workflow.execute(input).get();
        System.out.println("Executed: " + executed);


        return null;
    }

    public static void main(String[] args) throws Exception {

        new TripBookingTasks().testWorkflow();

        System.out.println("Done");

    }
}
