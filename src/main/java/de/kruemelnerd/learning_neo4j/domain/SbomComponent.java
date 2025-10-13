package de.kruemelnerd.learning_neo4j.domain;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Node
public class SbomComponent {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "HAS_VERSION")
    private Set<SbomVersion> sbomVersions = new HashSet<>();

    public SbomComponent() {
    }

    public SbomComponent(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<SbomVersion> getVersions() {
        return sbomVersions;
    }

    public void addVersion(SbomVersion sbomVersion) {
        this.sbomVersions.add(sbomVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        SbomComponent sbomComponent = (SbomComponent) o;
        return Objects.equals(id, sbomComponent.id) && Objects.equals(name, sbomComponent.name) && Objects.equals(sbomVersions, sbomComponent.sbomVersions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(sbomVersions);
        return result;
    }

    @Override
    public String toString() {
        return "SbomComponent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sbomVersions=" + sbomVersions.stream().toString() +
                '}';
    }
}
