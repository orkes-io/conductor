package com.netflix.conductor.sdk.workflow.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.netflix.conductor.common.jackson.JsonProtoModule;

public class ObjectMapperProvider {

    public ObjectMapper getObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_EMPTY));
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //objectMapper.setSerializationInclusion(JsonInclude.Include.);



        objectMapper.registerModule(new JsonProtoModule());
        return objectMapper;
    }
}
