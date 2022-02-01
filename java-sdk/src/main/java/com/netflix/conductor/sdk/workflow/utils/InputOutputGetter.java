package com.netflix.conductor.sdk.workflow.utils;

public class InputOutputGetter {

    public enum Field {
        input, output
    }
    private String name;

    private Field field;

    public InputOutputGetter(String name, Field field) {
        this.name = name;
        this.field = field;
    }

    public String get(String key) {
        return "${" + name + "." + field + "." + key + "}";
    }

    public static void main(String[] args) {
        InputOutputGetter input = new InputOutputGetter("task2", Field.output);
        System.out.println(input.get("code"));
    }
}
