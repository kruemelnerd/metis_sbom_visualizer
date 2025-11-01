package de.kruemelnerd.metis.domain;

import org.cyclonedx.model.Component;
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

    private Component.Type type;

    @Relationship(type = "HAS_VERSION")
    private Set<SbomVersion> sbomVersions = new HashSet<>();

    public SbomComponent() {
    }

    public SbomComponent(String name) {
        this.name = name;
    }

    public SbomComponent(String name, Component.Type type) {
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Component.Type getType() {
        return type;
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

        SbomComponent that = (SbomComponent) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(sbomVersions, that.sbomVersions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(type);
        result = 31 * result + Objects.hashCode(sbomVersions);
        return result;
    }
}
