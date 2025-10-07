package de.kruemelnerd.learning_neo4j.repository;

import de.kruemelnerd.learning_neo4j.domain.SbomComponent;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SbomComponentRepository extends Neo4jRepository<SbomComponent, Long> {
    Optional<SbomComponent> findByName(String name);

}
