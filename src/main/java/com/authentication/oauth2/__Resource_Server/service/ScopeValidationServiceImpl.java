package com.authentication.oauth2.__Resource_Server.service;

import com.authentication.oauth2.__Resource_Server.entity.ApiRule;
import com.authentication.oauth2.__Resource_Server.repository.ScopeMappingRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScopeValidationServiceImpl implements ScopeValidationService{

    private final ScopeMappingRepo scopeMappingRepo;

    public ScopeValidationServiceImpl(ScopeMappingRepo scopeMappingRepo) {
        this.scopeMappingRepo = scopeMappingRepo;
    }

    @Override
    public void saveApiRules(ApiRule apiRule) {
        scopeMappingRepo.save(apiRule);
    }

    @Override
    public List<ApiRule> getAllApiRules() {
        return scopeMappingRepo.findAll();
    }
}
