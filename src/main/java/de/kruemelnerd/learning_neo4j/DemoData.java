package de.kruemelnerd.learning_neo4j;


import de.kruemelnerd.learning_neo4j.domain.SbomComponent;
import de.kruemelnerd.learning_neo4j.domain.SbomVersion;
import de.kruemelnerd.learning_neo4j.repository.SbomComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DemoData implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(DemoData.class);

    private final SbomComponentRepository sbomComponentRepository;

    public DemoData(SbomComponentRepository sbomComponentRepository) {
        this.sbomComponentRepository = sbomComponentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        LocalTime time = LocalTime.now();
        SbomComponent sbomComponent = new SbomComponent("example-lib");
        sbomComponent.addVersion(new SbomVersion("1.0.0", "miaux " + time));
        logger.info("SBOM written: " + sbomComponent.toString());
        sbomComponentRepository.save(sbomComponent);
    }
}
