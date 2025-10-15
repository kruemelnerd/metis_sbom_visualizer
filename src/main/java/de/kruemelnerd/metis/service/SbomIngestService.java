package de.kruemelnerd.metis.service;

import de.kruemelnerd.metis.domain.SbomComponent;
import de.kruemelnerd.metis.domain.SbomVersion;
import de.kruemelnerd.metis.repository.SbomComponentRepository;
import de.kruemelnerd.metis.repository.SbomVersionRepository;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.parsers.BomParserFactory;
import org.cyclonedx.parsers.Parser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SbomIngestService {

    private final SbomComponentRepository componentRepository;
    private final SbomVersionRepository versionRepository;

    public SbomIngestService(SbomComponentRepository componentRepository, SbomVersionRepository versionRepository) {
        this.componentRepository = componentRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional
    public Map<String, Object> ingest(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        Parser parser = BomParserFactory.createParser(bytes);
        Bom bom = parser.parse(bytes);

        int createdComponents = 0, reusedComponents = 0;
        int createdVersions = 0, reusedVersions = 0;
        int createdDeps = 0;

        Map<String, SbomVersion> versionByRef = new HashMap<>();

        // Root-Komponente aus metadata.component berücksichtigen
        if (bom.getMetadata() != null && bom.getMetadata().getComponent() != null) {
            var root = bom.getMetadata().getComponent();
            var up = upsertVersion(root.getName(), root.getVersion(), root.getPurl());
            createdComponents += up.createdComponent ? 1 : 0;
            reusedComponents += up.createdComponent ? 0 : 1;
            createdVersions += up.createdVersion ? 1 : 0;
            reusedVersions += up.createdVersion ? 0 : 1;

            if (root.getBomRef() != null) {
                versionByRef.put(root.getBomRef(), up.version);
            }
        }


        if (bom.getComponents() != null) {
            for (org.cyclonedx.model.Component cdx : bom.getComponents()) {
                var up = upsertVersion(safe(cdx.getName(), "unknown"),
                        safe(cdx.getVersion(), "unspecified"),
                        cdx.getPurl());
                createdComponents += up.createdComponent ? 1 : 0;
                reusedComponents += up.createdComponent ? 0 : 1;
                createdVersions += up.createdVersion ? 1 : 0;
                reusedVersions += up.createdVersion ? 0 : 1;

                if (cdx.getBomRef() != null) {
                    versionByRef.put(cdx.getBomRef(), up.version);
                }
            }
        }


        if (bom.getDependencies() != null) {
            for (Dependency dependency : bom.getDependencies()) {
                SbomVersion from = versionByRef.get(dependency.getRef());
                if (from == null) {
                    from = synthesizeVersionFromRef(dependency.getRef());
                    if (from != null) {
                        versionByRef.put(dependency.getRef(), from);
                    } else {
                        // keine Infos → Kanten von unbekanntem Knoten auslassen
                        continue;
                    }
                }

                if (dependency.getRef() == null) continue;

                for (Dependency child : dependency.getDependencies()) {
                    SbomVersion to = versionByRef.get(child.getRef());
                    if (to == null) {
                        to = synthesizeVersionFromRef(child.getRef());
                        if (to != null) {
                            versionByRef.put(child.getRef(), to);
                        } else {
                            continue; // Ziel unbekannt → Kante auslassen
                        }
                    }
                    if (!from.getDependsOn().contains(to)) {

                        boolean created = versionRepository.mergeDependsOn(from.getId(), to.getId());
                        if (created) createdDeps++;
                    }
                }


            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("createdComponents", createdComponents);
        stats.put("reusedComponents", reusedComponents);
        stats.put("createdVersions", createdVersions);
        stats.put("reusedVersions", reusedVersions);
        stats.put("createdDeps", createdDeps);
        return stats;
    }

    private static String safe(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private record UpsertResult(SbomVersion version, boolean createdComponent, boolean createdVersion) {
    }

    private UpsertResult upsertVersion(String componentName, String versionNumber, String purl) {
        boolean createdComponent = false;
        boolean createdVersion = false;

        SbomComponent component = componentRepository.findByName(componentName).orElse(null);
        if (component == null) {
            component = new SbomComponent(componentName);
            component = componentRepository.save(component);
            createdComponent = true;
        }

        SbomVersion version = versionRepository.findByComponentAndVersion(componentName, versionNumber).orElse(null);
        if (version == null) {
            version = new SbomVersion(componentName, versionNumber);
            component.addVersion(version);
            componentRepository.save(component);
            createdVersion = true;
        } else if (version.getPurl() == null && purl != null) {
            version.setPurl(purl);
            versionRepository.save(version);
        }

        return new UpsertResult(version, createdComponent, createdVersion);
    }


    /**
     * Sehr einfacher PURL/BOM-Ref-Parser:
     * - Erwartet Strings wie "pkg:example/name@1.2.3"
     * - Fällt zurück auf label == ganze Ref, wenn nichts extrahierbar ist
     */
    private SbomVersion synthesizeVersionFromRef(String ref) {
        if (ref == null || ref.isBlank()) return null;
        // Nur purl-ähnliche Refs betrachten
        if (ref.startsWith("pkg:")) {
            String name = ref;
            String version = "unspecified";
            int at = ref.lastIndexOf('@');
            if (at > 0 && at < ref.length() - 1) {
                version = ref.substring(at + 1);
                // Name grob bestimmen (zwischen letztem '/' und '@')
                int slash = ref.lastIndexOf('/', at);
                name = (slash >= 0 && slash < at) ? ref.substring(slash + 1, at) : ref.substring(4, at); // 4 = len("pkg:")
            }
            return upsertVersion(name, version, ref).version;
        }
        // Nicht parsebar → optional einen Platzhalter anlegen:
        // return upsertVersion(ref, "unspecified", null).version;
        return null; // oder bewusst ignorieren
    }
}
