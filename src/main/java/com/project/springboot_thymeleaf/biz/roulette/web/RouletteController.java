package com.project.springboot_thymeleaf.biz.roulette.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouletteController {

    @GetMapping("/roulette")
    public String roulette() {
        return "roulette/roulette";
    }
}

