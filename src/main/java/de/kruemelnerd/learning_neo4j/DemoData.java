package de.kruemelnerd.learning_neo4j;


import de.kruemelnerd.learning_neo4j.domain.SbomComponent;
import de.kruemelnerd.learning_neo4j.domain.SbomVersion;
import de.kruemelnerd.learning_neo4j.repository.SbomComponentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoData implements CommandLineRunner {

    private final SbomComponentRepository sbomComponentRepository;

    public DemoData(SbomComponentRepository sbomComponentRepository) {
        this.sbomComponentRepository = sbomComponentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        SbomComponent sbomComponent = new SbomComponent("example-lib");
        sbomComponent.addVersion(new SbomVersion("1.0.0", "miau"));
        sbomComponentRepository.save(sbomComponent);
    }
}
