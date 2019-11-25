package com.spotonresponse.adapter.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(path = "/login", produces = "application/json")
    public String login(@RequestParam(value = "payload") String username, String password) {
        return "cvs";
    }
}