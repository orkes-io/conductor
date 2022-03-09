package com.netflix.conductor.sdk;

public class CustomerInfo {

    String name;

    int birthYear;

    String ssn;

    char prefix;

    public char getPrefix() {
        return prefix;
    }

    public void setPrefix(char prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
}
