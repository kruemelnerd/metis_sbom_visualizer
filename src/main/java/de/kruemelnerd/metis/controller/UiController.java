package de.kruemelnerd.metis.controller;

import de.kruemelnerd.metis.service.SbomIngestService;
import de.kruemelnerd.metis.service.SbomService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/ui")
public class UiController {

    private final SbomService sbomService;
    private final SbomIngestService sbomIngestService;

    public UiController(SbomService sbomService, SbomIngestService sbomIngestService) {
        this.sbomService = sbomService;
        this.sbomIngestService = sbomIngestService;
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> handleUpload(@RequestParam("file") MultipartFile file, Model model) throws Exception {
        var stats = sbomIngestService.ingest(file.getInputStream());
        return Map.of("status", "ok", "stats", stats);
    }
}
