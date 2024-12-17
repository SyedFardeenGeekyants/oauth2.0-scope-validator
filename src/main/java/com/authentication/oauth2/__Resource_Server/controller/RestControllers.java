package com.authentication.oauth2.__Resource_Server.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class RestControllers {

    @GetMapping("/profile")
    public String getProfile(){
        return "GET.profile fetch successfully!";
    }

    @PostMapping("/profile")
    public String addProfile(@RequestBody ProfileRequest request){

        if(request.getOperation().equalsIgnoreCase("Update")){
            return "Profile updated successfully!";
        }
        return "Profile added successfully!";
    }

    @DeleteMapping("/profile")
    public String deleteProfile(){
        return "Profile deleted successfully!";
    }

}
