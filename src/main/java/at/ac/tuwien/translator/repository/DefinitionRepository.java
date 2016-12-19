package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Definition;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Definition entity.
 */
@SuppressWarnings("unused")
public interface DefinitionRepository extends JpaRepository<Definition,Long> {

    @Query("SELECT DISTINCT d " +
        "FROM Definition d " +
        "LEFT JOIN Definition d2 " +
        "ON d.label = d2.label AND d.version < d2.version " +
        "JOIN FETCH d.translations t " +
        "JOIN FETCH t.languages " +
        "WHERE d2.version IS NULL AND d.project.id = (:projectId)")
    public List<Definition> findLatestByProject(@Param("projectId") Long projectId);
}
