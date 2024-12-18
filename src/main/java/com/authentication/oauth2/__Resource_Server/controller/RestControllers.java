package com.authentication.oauth2.__Resource_Server.controller;

import org.springframework.http.MediaType;
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

    @GetMapping("/v2/profile")
    public String pathParamApis(@RequestParam("operation") String operation, @RequestParam("name") String name){
        return "GET.profile fetch from path param apis";
    }

    @PostMapping(value = "/v2/profile",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String formRequest(ProfileRequest profileRequest){
        return "POST.profile fetch from form body apis";
    }

}
