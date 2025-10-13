package de.kruemelnerd.learning_neo4j.repository;

import de.kruemelnerd.learning_neo4j.domain.SbomVersion;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SbomVersionRepository extends Neo4jRepository<SbomVersion, Long> {

    @Query("MATCH (v:Version {label: $label}) RETURN v")
    Optional<SbomVersion> findByLabel(String label);

    @Query("""
              MATCH (c:SbomComponent {name: $componentName})-[:HAS_VERSION]->(v:SbomVersion {version: $version})
              RETURN v LIMIT 1
            """)
    Optional<SbomVersion> findByComponentAndVersion(String componentName, String version);

    @Query("""
            MATCH (v1:SbomVersion) WHERE id(v1) = $fromId
            MATCH (v2:SbomVersion) WHERE id(v2) = $toId
            MERGE (v1)-[r:DEPENDS_ON]->(v2)
            ON CREATE SET r._createdNow = true
            ON MATCH  SET r._createdNow = false
            RETURN r._createdNow AS createdNow;
            """)
    boolean mergeDependsOn(long fromId, long toId);
}