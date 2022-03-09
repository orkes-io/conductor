package com.netflix.conductor.sdk;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.task.InputParam;
import com.netflix.conductor.sdk.task.OutputParam;
import com.netflix.conductor.sdk.task.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@SuppressWarnings("ALL")
public class TestTripBooking {

    @WorkflowTask("init_booking")
    public String initBooking(TripBookingInput input) {
        return "init:" + UUID.randomUUID().toString();
    }

    @WorkflowTask("book_flight")
    public @OutputParam("bookingId") String bookFlight(@InputParam("from") String from, @InputParam("to") String to) {
        return from + "-" + to + ":" + UUID.randomUUID().toString();
    }

    @WorkflowTask("book_hotel")
    public String bookHotel(@InputParam("flightBookingId") String name) {
        return UUID.randomUUID().toString();
    }

    @WorkflowTask("book_hertz")
    public String bookHertz(String carType) {
        return UUID.randomUUID().toString();
    }

    @WorkflowTask("book_national")
    public String bookNational(String carType) {
        return UUID.randomUUID().toString();
    }

    public static <T> T workflowInput() {
        return null;
    }

    public void bookTrip() throws ExecutionException, InterruptedException {
        String url = "https://saastestapi.orkes.net/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);
        //executor.initWorkers(TestTripBooking.class.getPackageName());


        TripBookingInput input = new TripBookingInput();
        input.setFrom("NYC");
        input.setTo("BOM");

        SimpleTask init = new SimpleTask("init_booking", "init_booking");


        SimpleTask bookFlight = new SimpleTask("book_flight", "book_flight");
        bookFlight.input(
                "from", ConductorWorkflow.input.get("from"),
                "to", ConductorWorkflow.input.get("to"),
                "days", ConductorWorkflow.input.get("days"));

        SimpleTask bookHotel = new SimpleTask("book_hotel", "book_hotel");
        bookHotel.input("flightBookingId", bookFlight.taskOutput.get("bookingId"));

        WorkflowBuilder<TripBookingInput> builder = new WorkflowBuilder<>(executor)
                .name("tripBooking2")
                .version(3);

        ConductorWorkflow<TripBookingInput> workflow = builder
                .add(bookFlight, bookHotel)
                .build();

        boolean executed = workflow.registerWorkflow();
        System.out.println(executed);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, JsonProcessingException {
        String url = "https://saastestapi.orkes.net/api/";
        WorkflowExecutor executor = new WorkflowExecutor(url);
        WorkflowBuilder<TripBookingInput> builder = new WorkflowBuilder<>(executor)
                .name("airflowTest")
                .version(1)
                .add(new SimpleTask("task_airflow", "task_airflow"));

        Workflow run = builder.build().execute(new TripBookingInput()).get();
        System.out.println(run);

    }





}
