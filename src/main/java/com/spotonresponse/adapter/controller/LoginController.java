package com.spotonresponse.adapter.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(path = "/login", produces = "application/json")
    public String login(@RequestParam("username") String username, String password) {
        String u = username;
        String p = password;
        return "cvs";
    }
}