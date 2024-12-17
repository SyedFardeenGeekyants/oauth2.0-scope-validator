package com.authentication.oauth2.__Resource_Server.controller;

import com.authentication.oauth2.__Resource_Server.entity.ApiRule;
import com.authentication.oauth2.__Resource_Server.service.ScopeValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping("/api")
public class UiController {

    private final ScopeValidationService scopeValidationService;

    public UiController(ScopeValidationService scopeValidationService) {
        this.scopeValidationService = scopeValidationService;
    }

    @GetMapping("/register")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@ModelAttribute ApiRule apiRule) {

        log.debug("Api rules from form {}",apiRule);
        scopeValidationService.saveApiRules(apiRule);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

}
