package com.netflix.conductor.common.metadata.workflow;

import com.netflix.conductor.annotations.protogen.ProtoField;
import com.netflix.conductor.annotations.protogen.ProtoMessage;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;

@ProtoMessage
public class TaskEventList {
    public List<TaskEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TaskEvent> events) {
        this.events = events;
    }

    @ProtoField(id = 1)
    private @Valid List<@Valid TaskEvent> events = new LinkedList<>();
}
