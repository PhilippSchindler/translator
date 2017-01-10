package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Definition;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.HashSet;
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
        "LEFT JOIN FETCH d.translations t " +
        "WHERE d2.version IS NULL AND d.project.id = (:projectId)")
    List<Definition> findLatestByProject(@Param("projectId") Long projectId);

    @Query("SELECT d1 FROM Definition d1 WHERE d1.project.id = :projectId AND d1.version = (SELECT MAX(d2.version) FROM Definition d2 WHERE d1.label = d2.label)")
    List<Definition> findForProject(@Param("projectId") Long projectId);

    List<Definition> findByLabel(String label);

    @Query("SELECT distinct d.version FROM Definition d where d.project.id = (:projectId) order by d.version")
    List<Integer> listOfVersions(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT d FROM Definition d LEFT JOIN FETCH d.translations t WHERE d.project.id = :projectId AND d.version = (select max(d2.version) from Definition d2 where d2.project.id = :projectId and d2.label = d.label and d2.version <= :version group by d2.project.id, d2.label)")
    List<Definition> findDefinitionsWithVersion(@Param("projectId") Long projectId, @Param("version") Integer version);

}
