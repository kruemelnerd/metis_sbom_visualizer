package de.kruemelnerd.learning_neo4j.repository;

import de.kruemelnerd.learning_neo4j.domain.Version;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersionRepository extends Neo4jRepository<Version, Long> {

    @Query("MATCH (v:Version {label: $label}) RETURN v")
    Optional<Version> findByLabel(String label);
}
