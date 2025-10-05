package de.kruemelnerd.learning_neo4j.service;

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.parsers.BomParserFactory;
import org.cyclonedx.parsers.Parser;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SbomService {


    public Map<String, Object> parseOnly(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();        // 1) einmal einlesen
        Parser parser = BomParserFactory.createParser(bytes);
        Bom bom = parser.parse(bytes);

        int componentCount = 0;
        int dependencyEdges = 0;
        int uniqueRefs = 0;

        Map<String, Component> byRef = new HashMap<>();

        if (bom.getComponents() != null) {
            for (Component cdx : bom.getComponents()) {
                componentCount++;
                if (cdx.getBomRef() != null) {
                    byRef.put(cdx.getBomRef(), cdx);
                }
            }
            uniqueRefs = byRef.size();
        }

        if (bom.getDependencies() != null) {
            for (Dependency dependency : bom.getDependencies()) {
                if (dependency.getDependencies() != null) {
                    dependencyEdges += dependency.getDependencies().size();
                }
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("components", componentCount);
        stats.put("uniqueBomRefs", uniqueRefs);
        stats.put("dependencyEdges", dependencyEdges);
        stats.put("specVersion", bom.getSpecVersion());
        stats.put("serialNumber", bom.getSerialNumber());

        return stats;
    }
}
