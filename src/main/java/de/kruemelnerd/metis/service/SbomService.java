package de.kruemelnerd.metis.service;

import de.kruemelnerd.metis.domain.VersionDetails;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.parsers.BomParserFactory;
import org.cyclonedx.parsers.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class SbomService {

    Logger logger = LoggerFactory.getLogger(Neo4jClient.class);
    private final Neo4jClient neo4j;

    public SbomService(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }


    public Map<String, Object> parseOnly(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();        // 1) einmal einlesen
        Parser parser = BomParserFactory.createParser(bytes);
        Bom bom = parser.parse(bytes);

        int componentCount = 0;
        int dependencyEdges = 0;
        int uniqueRefs = 0;

        Map<String, Component> byRef = new HashMap<>();

        if (bom.getComponents() != null) {
            for (Component cdx : bom.getComponents()) {
                componentCount++;
                if (cdx.getBomRef() != null) {
                    byRef.put(cdx.getBomRef(), cdx);
                }
            }
            uniqueRefs = byRef.size();
        }

        if (bom.getDependencies() != null) {
            for (Dependency dependency : bom.getDependencies()) {
                if (dependency.getDependencies() != null) {
                    dependencyEdges += dependency.getDependencies().size();
                }
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("components", componentCount);
        stats.put("uniqueBomRefs", uniqueRefs);
        stats.put("dependencyEdges", dependencyEdges);
        stats.put("specVersion", bom.getSpecVersion());
        stats.put("serialNumber", bom.getSerialNumber());

        return stats;
    }

    public Optional<VersionDetails> getVersionDetails(long versionNodeId) {
        var row = neo4j.query("""
                            MATCH (c:SbomComponent)-[:HAS_VERSION]->(v:SbomVersion)
                            WHERE id(v) = $id
                            OPTIONAL MATCH (v)-[:DEPENDS_ON]->(d:SbomVersion)<-[:HAS_VERSION]-(dc:SbomComponent)
                            WITH c, v, collect(dc.name + ':' + coalesce(d.version, d.label)) AS depends
                            RETURN c.name  AS componentName,
                                   coalesce(c.type, 'unknown') AS componentType,
                                   coalesce(v.version, v.label) AS version,
                                   v.purl   AS purl,
                                   depends  AS depends
                        """).bind(versionNodeId).to("id")
                .fetch().one();

        return row.map(this::mapDetails);
    }

    @SuppressWarnings("unchecked")
    private VersionDetails mapDetails(Map<String, Object> row) {
        String componentName = (String) row.getOrDefault("componentName", "?");
        String componentType = (String) row.getOrDefault("componentType", "unknown");
        String version = (String) row.getOrDefault("version", "");
        String purl = (String) row.getOrDefault("purl", null);
        List<String> depends = (List<String>) row.getOrDefault("depends", List.of());
        var versiondetail = new VersionDetails(componentName, componentType, version, purl, depends);
        logger.debug("versiondetail: {}", versiondetail);
        return versiondetail;
    }

    record Node(String id, String name, String ver) {
    }

    public Optional<String> getDependencyTreeText(String rootComponentName, int depth) {
        // 1) Root-Version holen (Name + Version für Kopfzeile)
        var rootRow = neo4j.query("""
                    MATCH (c:SbomComponent {name:$root})-[:HAS_VERSION]->(v:SbomVersion)
                    RETURN id(v) AS id, c.name AS name, coalesce(v.version, v.label) AS ver
                    LIMIT 1
                """).bind(rootComponentName).to("root").fetch().one();
        if (rootRow.isEmpty()) return Optional.empty();

        String rootId = Long.toString(((Number) rootRow.get().get("id")).longValue());
        String rootName = String.valueOf(rootRow.get().get("name"));
        String rootVer = (String) rootRow.get().get("ver");



        // 2) Alle Kanten (DIRECTED) bis zur gewünschten Tiefe ab Root
        var rows = neo4j.query("""
MATCH (c:SbomComponent {name:$root})-[:HAS_VERSION]->(v:SbomVersion)
    MATCH p = (v)-[:DEPENDS_ON*]->(w:SbomVersion)
    WHERE length(p) <= $depth
    WITH relationships(p) AS rs
    UNWIND rs AS r
    WITH DISTINCT startNode(r) AS s, endNode(r) AS t
    OPTIONAL MATCH (c1:SbomComponent)-[:HAS_VERSION]->(s)
    OPTIONAL MATCH (c2:SbomComponent)-[:HAS_VERSION]->(t)
    RETURN id(s) AS sid, c1.name AS sName, coalesce(s.version, s.label) AS sVer,
           id(t) AS tid, c2.name AS tName, coalesce(t.version, t.label) AS tVer
                """).bindAll(Map.of("root", rootComponentName, "depth", depth)).fetch().all();

        // 3) Adjazenzliste aufbauen

        Map<String, Node> nodes = new HashMap<>();
        Map<String, List<String>> children = new HashMap<>();

        // root eintragen
        nodes.put(rootId, new Node(rootId, rootName, rootVer));
        children.computeIfAbsent(rootId, k -> new ArrayList<>());

        for (var r : rows) {
            String sid = Long.toString(((Number) r.get("sid")).longValue());
            String tid = Long.toString(((Number) r.get("tid")).longValue());
            String sName = String.valueOf(r.get("sName"));
            String sVer = String.valueOf(r.get("sVer"));
            String tName = String.valueOf(r.get("tName"));
            String tVer = String.valueOf(r.get("tVer"));

            nodes.putIfAbsent(sid, new Node(sid, sName, sVer));
            nodes.putIfAbsent(tid, new Node(tid, tName, tVer));
            children.computeIfAbsent(sid, k -> new ArrayList<>());
            if (!children.get(sid).contains(tid)) children.get(sid).add(tid);
        }

        // 4) DFS mit Zyklenerkennung und sortierter Ausgabe
        StringBuilder out = new StringBuilder();
        Set<String> path = new HashSet<>();   // für Zyklenerkennung (aktueller Pfad)

        out.append(rootName).append(":").append(rootVer).append("\n");
        dfsPrint(rootId, 1, depth, nodes, children, path, out);

        return Optional.of(out.toString());
    }

    private void dfsPrint(
            String currentId, int level, int maxDepth,
            Map<String, Node> nodes, Map<String, List<String>> children,
            Set<String> path, StringBuilder out) {

        if (level > maxDepth) return;
        var kids = children.getOrDefault(currentId, List.of());
        // deterministische Reihenfolge: nach Name, dann Version
        List<String> sorted = new ArrayList<>(kids);
        sorted.sort(Comparator.comparing((String id) -> nodes.get(id).name)
                .thenComparing(id -> nodes.get(id).ver, Comparator.nullsLast(String::compareTo)));

        path.add(currentId);
        for (String kid : sorted) {
            var n = nodes.get(kid);
            String prefix = "-".repeat(level) + " ";
            if (path.contains(kid)) {
                out.append(prefix).append(n.name).append(":").append(n.ver).append(" (cycle)\n");
                continue;
            }
            out.append(prefix).append(n.name).append(":").append(n.ver).append("\n");
            dfsPrint(kid, level + 1, maxDepth, nodes, children, path, out);
        }
        path.remove(currentId);
    }
}
