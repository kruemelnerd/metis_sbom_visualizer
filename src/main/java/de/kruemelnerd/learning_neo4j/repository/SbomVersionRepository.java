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
      MATCH (c:SbomComponent {name: $componentName})-[:HAS_VERSION]->(v:SbomVersion {label: $label})
      RETURN v LIMIT 1
    """)
    Optional<SbomVersion> findByComponentAndLabel(String componentName, String label);
}
