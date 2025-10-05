package de.kruemelnerd.learning_neo4j.controller;

import de.kruemelnerd.learning_neo4j.service.SbomService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/ui")
public class UiController {

    private final SbomService sbomService;

    public UiController(SbomService sbomService) {
        this.sbomService = sbomService;
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model) throws Exception {
        var stats = sbomService.parseOnly(file.getInputStream());
        model.addAttribute("msg", "Parsing OK: " + stats);
        model.addAttribute("result", "Parsing OK: " + stats);
        return "upload";
    }
}
