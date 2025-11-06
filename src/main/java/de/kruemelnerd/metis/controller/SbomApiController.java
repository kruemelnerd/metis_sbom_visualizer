package de.kruemelnerd.metis.controller;

import de.kruemelnerd.metis.domain.SbomVersion;
import de.kruemelnerd.metis.service.SbomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/version/{id}")
    public ResponseEntity<?> versionDetails(@PathVariable long id) {
        return sbomService.getVersionDetails(id)
                .map(d -> ResponseEntity.ok(Map.of("status", "ok", "data", d)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "not_found")));

    }

    @GetMapping("/tree")
    public ResponseEntity<?> tree(@RequestParam String root,
                                  @RequestParam(defaultValue = "5") int depth) {
        return sbomService.getDependencyTreeText(root, depth)
                .<ResponseEntity<?>>map(text -> ResponseEntity.ok(Map.of("status","ok","text", text)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status","not_found")));
    }

}
