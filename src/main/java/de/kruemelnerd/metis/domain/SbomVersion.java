package de.kruemelnerd.metis.domain;


import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Node
public class SbomVersion {

    @Id
    @GeneratedValue
    private Long id;

    private String label;

    private String purl;

    private String version;

    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private Set<SbomVersion> dependsOn = new HashSet<SbomVersion>();

    public void addDependency(SbomVersion sbomVersion) {
        dependsOn.add(sbomVersion);
    }

    public SbomVersion() {
    }

    public SbomVersion(String label, String version) {
        this.label = label;
        this.version = version;
        this.purl = label + ":" + version;
    }


    public SbomVersion(String label, String purl, String version) {
        this.label = label;
        this.purl = purl;
        this.version = version;
    }

    public Long getId() {
        return id;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<SbomVersion> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(Set<SbomVersion> dependsOn) {
        this.dependsOn = dependsOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        SbomVersion that = (SbomVersion) o;
        return Objects.equals(id, that.id) && Objects.equals(label, that.label) && Objects.equals(purl, that.purl) && Objects.equals(version, that.version) && Objects.equals(dependsOn, that.dependsOn);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(label);
        result = 31 * result + Objects.hashCode(purl);
        result = 31 * result + Objects.hashCode(version);
        result = 31 * result + Objects.hashCode(dependsOn);
        return result;
    }

    @Override
    public String toString() {
        return "SbomVersion{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", purl='" + purl + '\'' +
                ", version='" + version + '\'' +
                ", dependsOn=" + dependsOn +
                '}';
    }
}
