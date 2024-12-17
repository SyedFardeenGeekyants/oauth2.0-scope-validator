package com.authentication.oauth2.__Resource_Server.service;

import com.authentication.oauth2.__Resource_Server.entity.ApiRule;

import java.util.List;

public interface ScopeValidationService {

    void saveApiRules(ApiRule apiRule);

    List<ApiRule> getAllApiRules();
}
