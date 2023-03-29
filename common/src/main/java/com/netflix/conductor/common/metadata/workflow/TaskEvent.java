package com.netflix.conductor.common.metadata.workflow;

import com.netflix.conductor.annotations.protogen.ProtoField;
import java.util.Map;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TaskEvent {

    public enum Type {
        POSTGRESQL,
        KAFKA,
        MONGODB
    }

    @ProtoField(id = 1)
    @NotNull
    Type type;

    @ProtoField(id = 2)
    String schemaName;

    @ProtoField(id =3)
    Map<String, Object> values;

    @ProtoField(id =4)
    String topic;
}
