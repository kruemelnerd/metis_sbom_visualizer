package de.kruemelnerd.learning_neo4j;


import de.kruemelnerd.learning_neo4j.domain.Artefakt;
import de.kruemelnerd.learning_neo4j.domain.Version;
import de.kruemelnerd.learning_neo4j.repository.ComponentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoData implements CommandLineRunner {

    private final ComponentRepository componentRepository;

    public DemoData(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Artefakt artefakt = new Artefakt("example-lib");
        artefakt.addVersion(new Version("1.0.0", "miau"));
        componentRepository.save(artefakt);
    }
}
