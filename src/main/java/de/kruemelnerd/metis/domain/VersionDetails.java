package de.kruemelnerd.metis.domain;

import java.util.List;

public class VersionDetails {
    private String componentName;
    private String componentType;
    private String version;   // bevorzugt v.version, sonst v.label
    private String purl;
    private List<String> depends;

    public VersionDetails() {}

    public VersionDetails(String componentName, String componentType, String version, String purl, List<String> depends) {
        this.componentName = componentName;
        this.componentType = componentType;
        this.version = version;
        this.purl = purl;
        this.depends = depends;
    }

    public String getComponentName() { return componentName; }
    public String getComponentType() { return componentType; }
    public String getVersion() { return version; }
    public String getPurl() { return purl; }
    public List<String> getDepends() { return depends; }

    @Override
    public String toString() {
        return "VersionDetails{" +
                "componentName='" + componentName + '\'' +
                ", componentType='" + componentType + '\'' +
                ", version='" + version + '\'' +
                ", purl='" + purl + '\'' +
                ", depends=" + depends +
                '}';
    }
}
