package de.kruemelnerd.learning_neo4j.domain;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Node
public class Artefakt {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "HAS_VERSION")
    private Set<Version> versions = new HashSet<>();

    public Artefakt() {}

    public Artefakt(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Version> getVersions() {
        return versions;
    }

    public void addVersion(Version version) {
        this.versions.add(version);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Artefakt artefakt = (Artefakt) o;
        return Objects.equals(id, artefakt.id) && Objects.equals(name, artefakt.name) && Objects.equals(versions, artefakt.versions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(versions);
        return result;
    }
}
