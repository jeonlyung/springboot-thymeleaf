package com.project.springboot_thymeleaf.biz;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        // templates 폴더의 index.html을 찾아서 보여줍니다.
        return "index";
    }
}
