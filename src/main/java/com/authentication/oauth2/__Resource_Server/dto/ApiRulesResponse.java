package com.authentication.oauth2.__Resource_Server.dto;

import java.util.List;

public class ApiRulesResponse {

    private String url;
    private String httpMethod;
    private List<Rule> rules;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "ApiRulesResponse{" +
                "url='" + url + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", rules=" + rules +
                '}';
    }
}
