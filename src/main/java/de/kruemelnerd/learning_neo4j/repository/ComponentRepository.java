package de.kruemelnerd.learning_neo4j.repository;

import de.kruemelnerd.learning_neo4j.domain.Artefakt;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComponentRepository extends Neo4jRepository<Artefakt, Long> {
    Optional<Artefakt> findByName(String name);

}
