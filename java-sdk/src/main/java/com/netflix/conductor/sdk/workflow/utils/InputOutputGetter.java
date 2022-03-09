package com.netflix.conductor.sdk.workflow.utils;

public class InputOutputGetter {

    public enum Field {
        input, output
    }

    public static final class Map {
        private String parent;


        public Map(String parent) {
            this.parent = parent;
        }

        public Map getMap(String key) {
            return new Map(parent + "." + key);
        }

        public String get(String key) {
            return parent + "." + key + "}";
        }
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

    public String getParent() {
        return "${" + name + "." + field + "}";
    }

    public Map getMap(String key) {
        return new Map("${" + name + "." + field + "." + key);
    }


    public static void main(String[] args) {
        InputOutputGetter input = new InputOutputGetter("task2", Field.output);
        System.out.println(input.get("code"));
        System.out.println(input.getMap("users").get("id"));
        System.out.println(input.getMap("users").getMap("address").get("city"));
        System.out.println(input.getMap("users").getMap("address").getMap("zip").get("code"));
    }
}
