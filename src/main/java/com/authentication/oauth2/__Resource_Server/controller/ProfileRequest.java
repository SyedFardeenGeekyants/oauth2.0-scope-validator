package com.authentication.oauth2.__Resource_Server.controller;

public class ProfileRequest {

    public ProfileRequest(String operation, String name) {
        this.operation = operation;
        this.name = name;
    }

    private String name;
    private String operation;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProfileRequest{" +
                "name='" + name + '\'' +
                ", operation='" + operation + '\'' +
                '}';
    }
}
