package de.kruemelnerd.metis.integrationtests;

import de.kruemelnerd.metis.service.SbomIngestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
@SpringBootTest
public class SbomIngestServiceIT {

    private static final String password = "password1234546243523";

    @Container
    static final Neo4jContainer neo4jContainer =
            new Neo4jContainer<>("neo4j:latest")
                    .withAdminPassword(password);

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> password);
    }

    @Autowired
    SbomIngestService ingestService;

    @Autowired
    Neo4jClient neo4jClient;

    private String sbomJson;

    @BeforeEach
    void loadJson() {
        sbomJson = """
                {
                   "bomFormat": "CycloneDX",
                   "specVersion": "1.6",
                   "version": 1,
                   "metadata": {
                     "timestamp": "2025-10-07T12:00:00Z",
                     "tools": [
                       {
                         "vendor": "DemoCorp",
                         "name": "demo-sbom-tool",
                         "version": "0.1.0"
                       }
                     ],
                     "component": {
                       "bom-ref": "pkg:example/demo-app@1.0.0",
                       "type": "application",
                       "name": "demo-app",
                       "version": "1.0.0",
                       "purl": "pkg:example/demo-app@1.0.0"
                     }
                   },
                   "components": [
                     {
                       "bom-ref": "pkg:example/lib-foo@2.2.0",
                       "type": "library",
                       "name": "lib-foo",
                       "version": "2.2.0",
                       "purl": "pkg:example/lib-foo@2.2.0",
                       "hashes": [
                         {
                           "alg": "SHA-256",
                           "content": "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                         }
                       ],
                       "licenses": [
                         {
                           "license": {
                             "id": "MIT"
                           }
                         }
                       ]
                     },
                     {
                       "bom-ref": "pkg:example/lib-bar@1.0.0",
                       "type": "library",
                       "name": "lib-bar",
                       "version": "1.0.0",
                       "purl": "pkg:example/lib-bar@1.0.0",
                       "hashes": [
                         {
                           "alg": "SHA-256",
                           "content": "abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd"
                         }
                       ],
                       "licenses": [
                         {
                           "license": {
                             "id": "Apache-2.0"
                           }
                         }
                       ]
                     },
                     {
                       "bom-ref": "pkg:example/lib-baz@3.1.0",
                       "type": "library",
                       "name": "lib-baz",
                       "version": "3.1.0",
                       "purl": "pkg:example/lib-baz@3.1.0",
                       "hashes": [
                         {
                           "alg": "SHA-256",
                           "content": "fedcbafedcbafedcbafedcbafedcbafedcbafedcbafedcbafedcbafedcbafedc"
                         }
                       ],
                       "licenses": [
                         {
                           "license": {
                             "id": "BSD-3-Clause"
                           }
                         }
                       ]
                     },
                     {
                       "bom-ref": "pkg:example/lib-qux@0.9.0",
                       "type": "library",
                       "name": "lib-qux",
                       "version": "0.9.0",
                       "purl": "pkg:example/lib-qux@0.9.0",
                       "hashes": [
                         {
                           "alg": "SHA-256",
                           "content": "1111111111111111111111111111111111111111111111111111111111111111"
                         }
                       ],
                       "licenses": [
                         {
                           "license": {
                             "id": "GPL-3.0-or-later"
                           }
                         }
                       ]
                     },
                     {
                       "bom-ref": "pkg:example/lib-qux@1.0.0",
                       "type": "library",
                       "name": "lib-qux",
                       "version": "0.9.0",
                       "purl": "pkg:example/lib-qux@1.0.0",
                       "hashes": [
                         {
                           "alg": "SHA-256",
                           "content": "1111111111111111111111111111111111111111111111111111111111111111"
                         }
                       ],
                       "licenses": [
                         {
                           "license": {
                             "id": "GPL-3.0-or-later"
                           }
                         }
                       ]
                     },
                     {
                       "bom-ref": "pkg:example/lib-utils@1.0.0",
                       "type": "library",
                       "name": "lib-utils",
                       "version": "1.0.0",
                       "purl": "pkg:example/lib-utils@1.0.0",
                       "hashes": [
                         {
                           "alg": "SHA-256",
                           "content": "2222222222222222222222222222222222222222222222222222222222222222"
                         }
                       ],
                       "licenses": [
                         {
                           "license": {
                             "id": "MIT"
                           }
                         }
                       ]
                     },
                     {
                       "bom-ref": "pkg:example/lib-core@4.5.0",
                       "type": "library",
                       "name": "lib-core",
                       "version": "4.5.0",
                       "purl": "pkg:example/lib-core@4.5.0",
                       "hashes": [
                         {
                           "alg": "SHA-256",
                           "content": "3333333333333333333333333333333333333333333333333333333333333333"
                         }
                       ],
                       "licenses": [
                         {
                           "license": {
                             "id": "Apache-2.0"
                           }
                         }
                       ]
                     }
                   ],
                   "dependencies": [
                     {
                       "ref": "pkg:example/demo-app@1.0.0",
                       "dependsOn": [
                         "pkg:example/lib-foo@2.2.0",
                         "pkg:example/lib-bar@1.0.0"
                       ]
                     },
                     {
                       "ref": "pkg:example/lib-foo@2.2.0",
                       "dependsOn": [
                         "pkg:example/lib-qux@1.0.0"
                       ]
                     },
                     {
                       "ref": "pkg:example/lib-bar@1.0.0",
                       "dependsOn": [
                         "pkg:example/lib-baz@3.1.0"
                       ]
                     },
                     {
                       "ref": "pkg:example/lib-baz@3.1.0",
                       "dependsOn": [
                         "pkg:example/lib-qux@0.9.0"
                       ]
                     },
                     {
                       "ref": "pkg:example/lib-qux@0.9.0",
                       "dependsOn": [
                         "pkg:example/lib-utils@1.0.0",
                         "pkg:example/lib-core@4.5.0"
                       ]
                     }
                   ]
                 }
                """;


    }

    @Test
    @DisplayName("Ingest creates nodes and edges correctly (including metadata.component) and is idempotent.")
    void ingest_creates_nodes_and_edges() throws Exception {

        deleteDB();

        // first Import
        var stats1 = ingestService.ingest(
                new ByteArrayInputStream(sbomJson.getBytes(StandardCharsets.UTF_8)));

        assertEquals(7, stats1.get("createdComponents"));
        assertEquals(7, stats1.get("createdVersions"));
        assertEquals(7, stats1.get("createdDeps"));

        long compCount = singleLong("MATCH (c:SbomComponent) RETURN count(c) AS n");
        long versionCount = singleLong("MATCH (v:SbomVersion) RETURN count(v) AS n");
        long dependencyCount = singleLong("MATCH ()-[r:DEPENDS_ON]->() RETURN count(r) AS n");

        assertEquals(7, compCount);
        assertEquals(7, versionCount);
        assertEquals(7, dependencyCount);


        long pathCount = singleLong("""
                MATCH (c1:SbomComponent {name:'demo-app'})-[:HAS_VERSION]->(v1:SbomVersion {version:'1.0.0'}),
                                    (c2:SbomComponent {name:'lib-core'})-[:HAS_VERSION]->(v2:SbomVersion {version:'4.5.0'})
                MATCH p = (v1)-[:DEPENDS_ON*1..5]->(v2)
                RETURN count(p) AS n
                """);
        assertTrue(pathCount > 0, "Expected path count for v1 -> v2 to be greater than 0");

        // second Import (nothing should change)
        var stats2 = ingestService.ingest(new ByteArrayInputStream(sbomJson.getBytes(StandardCharsets.UTF_8)));

        assertEquals(0, stats2.get("createdComponents"));
        assertEquals(0, stats2.get("createdVersions"));
        assertEquals(0, stats2.get("createdDeps"));

        assertEquals(7, singleLong("MATCH (c:SbomComponent) RETURN count(c) AS n"));
        assertEquals(7, singleLong("MATCH (v:SbomVersion) RETURN count(v) AS n"));
        assertEquals(7, singleLong("MATCH ()-[r:DEPENDS_ON]->() RETURN count(r) AS n"));
    }

    private void deleteDB() {
        neo4jClient.query("MATCH (n) DETACH DELETE n");
    }

    private long singleLong(String cypher) {
        return neo4jClient.query(cypher)
                .fetchAs(Long.class)
                .one()
                .orElseThrow(() -> new AssertionError("No result found for: " + cypher));
    }
}
