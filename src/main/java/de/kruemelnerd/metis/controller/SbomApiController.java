package de.kruemelnerd.metis.controller;

import de.kruemelnerd.metis.service.SbomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@RequestMapping("/api/sbom")
public class SbomApiController {
    private final Logger log = LoggerFactory.getLogger(SbomApiController.class);

    private final SbomService sbomService;

    public SbomApiController(SbomService sbomService) {
        this.sbomService = sbomService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        var stats = (sbomService).parseOnly(file.getInputStream());
        return Map.of("status", "ok", "stats", stats);
    }
}
