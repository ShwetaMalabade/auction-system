package com.database.auction.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserLogin {

    Logger logger = LoggerFactory.getLogger(UserLogin.class);

    @GetMapping("/login")
    public String Login(){
        logger.info("Successfully Logged in");
        return "Successful";
    }
}
