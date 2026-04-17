package com.mega.demo.controller;

import com.mega.demo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieService service;

    // 메인 페이지
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("movies", service.getAll());
        return "index";
    }

    // JSON → DB 적재
    @GetMapping("/load")
    @ResponseBody
    public String load() throws Exception {
        service.loadJson();
        return "DB 적재 완료";
    }
}