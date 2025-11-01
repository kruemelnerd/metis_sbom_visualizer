package de.kruemelnerd.metis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
public class GraphApi {
    private final Neo4jClient neo4jClient;
    Logger log = LoggerFactory.getLogger(SbomApiController.class);

    public GraphApi(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @GetMapping
    public Map<String, Object> graph(
            @RequestParam(defaultValue = "") String root,
            @RequestParam(defaultValue = "3") int depth,
            @RequestParam(defaultValue = "200") int limit) {
        String cypher;
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        params.put("depth", depth);

        if (root == null || root.isBlank()) {
            // get everything
            /* cypher = """
                    MATCH (n) RETURN n;
                    """;
            */
            cypher = """
                      MATCH (v:SbomVersion)-[r:DEPENDS_ON]->(w:SbomVersion)
                      WITH v,w,r LIMIT $limit
                      OPTIONAL MATCH (c1:SbomComponent)-[:HAS_VERSION]->(v)
                      OPTIONAL MATCH (c2:SbomComponent)-[:HAS_VERSION]->(w)
                      RETURN id(v) AS sid, coalesce(c1.name,'?') AS sComp, v.label AS sVer,
                             id(w) AS tid, coalesce(c2.name,'?') AS tComp, w.label AS tVer
                    """;


        } else {
            params.put("root", root);
            cypher = """
                    MATCH (c:SbomComponent {name:$root})-[:HAS_VERSION]->(v:SbomVersion)
                    CALL {
                      WITH v
                      MATCH p = (v)-[:DEPENDS_ON*]->(w:SbomVersion)
                      WHERE length(p) <= $depth
                      WITH relationships(p) AS rs
                      UNWIND rs AS r
                      WITH DISTINCT startNode(r) AS s, endNode(r) AS t
                      RETURN id(s) AS sid, id(t) AS tid
                    }
                    MATCH (sv:SbomVersion) WHERE id(sv)=sid
                    MATCH (tv:SbomVersion) WHERE id(tv)=tid
                    OPTIONAL MATCH (c1:SbomComponent)-[:HAS_VERSION]->(sv)
                    OPTIONAL MATCH (c2:SbomComponent)-[:HAS_VERSION]->(tv)
                    WITH sid, coalesce(c1.name,'?') AS sComp, sv.label AS sVer,
                         tid, coalesce(c2.name,'?') AS tComp, tv.label AS tVer
                    WITH DISTINCT sid, sComp, sVer, tid, tComp, tVer
                    LIMIT $limit
                    RETURN sid, sComp, sVer, tid, tComp, tVer
                    """;

        }

        var rows = neo4jClient.query(cypher).bindAll(params).fetch().all();

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();

        Map<Long, Integer> idx = new HashMap<>();
        int i = 0;


        System.out.println("ROWS size=" + rows.size());
        rows.stream().limit(5).forEach(r -> System.out.println("ROW: " + r));

        for (var row : rows) {
            Object sObj = row.get("sid");
            Object tObj = row.get("tid");
            if (!(sObj instanceof Number) || !(tObj instanceof Number)) {
                System.out.println("WARN row without sid/tid: " + row);
                continue; // skip invalide Zeilen
            }

            Long sId = ((Number) sObj).longValue();
            Long tId = ((Number) tObj).longValue();

            String sComp = String.valueOf(row.getOrDefault("sComp", "?"));
            String sVer  = String.valueOf(row.getOrDefault("sVer",  ""));

            String tComp = String.valueOf(row.getOrDefault("tComp", "?"));
            String tVer  = String.valueOf(row.getOrDefault("tVer",  ""));


            String sLabel = sComp + ":" + sVer;
            String tLabel = tComp + ":" + tVer;

            if (!idx.containsKey(sId)) { idx.put(sId, i++); nodes.add(Map.of("id", Long.toString(sId), "label", sLabel)); }
            if (!idx.containsKey(tId)) { idx.put(tId, i++); nodes.add(Map.of("id", Long.toString(tId), "label", tLabel)); }

            links.add(Map.of("source", Long.toString(sId), "target", Long.toString(tId)));
        }

        return Map.of("nodes", nodes, "links", links);
    }
}
