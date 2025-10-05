package de.kruemelnerd.learning_neo4j.domain;


import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Node
public class Version {

    @Id @GeneratedValue
    private Long id;

    private String label;

    private String purl;

    @Relationship( type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private Set<Version> dependsOn = new HashSet<Version>();

    public void addDependency(Version version) {
        dependsOn.add(version);
    }

    public Version() {    }

    public Version(String label, String purl) {
        this.label = label;
        this.purl = purl;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPurl() {
        return purl;
    }

    public void setPurl(String purl) {
        this.purl = purl;
    }

    public Set<Version> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(Set<Version> dependsOn) {
        this.dependsOn = dependsOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;
        return Objects.equals(id, version.id) && Objects.equals(label, version.label) && Objects.equals(purl, version.purl) && Objects.equals(dependsOn, version.dependsOn);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(label);
        result = 31 * result + Objects.hashCode(purl);
        result = 31 * result + Objects.hashCode(dependsOn);
        return result;
    }
}
