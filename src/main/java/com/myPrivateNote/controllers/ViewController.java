package com.myPrivateNote.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String showHome(){
        return "home";
    }

    @GetMapping("/connexion")
    public String showLogin(){
        return "login";
    }

    @GetMapping("/inscription")
    public String showRegister(){
        return "register";
    }
}
