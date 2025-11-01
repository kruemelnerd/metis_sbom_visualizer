package de.kruemelnerd.metis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GraphPageController {
    @GetMapping("/ui/graph")
    public String page(){ return "graph"; }
}
