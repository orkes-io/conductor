package com.netflix.conductor.common.metadata.workflow;

import com.netflix.conductor.annotations.protogen.ProtoEnum;

@ProtoEnum
public enum EventType {
    ON_START("onStart"),
    ON_SCHEDULED("onScheduled"),
    ON_COMPLETED("onCompleted");
    private final String name;

    EventType(String name) {
        this.name = name;
    }
}
