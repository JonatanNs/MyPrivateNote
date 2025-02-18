package com.myPrivateNote.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class ViewController {

    @GetMapping("/")
    public String home() {
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
