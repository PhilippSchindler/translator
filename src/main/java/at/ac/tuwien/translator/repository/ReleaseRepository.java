package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Project;
import at.ac.tuwien.translator.domain.Release;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Release entity.
 */
@SuppressWarnings("unused")
public interface ReleaseRepository extends JpaRepository<Release,Long> {

    @Query("select distinct release from Release release left join fetch release.definitions")
    List<Release> findAllWithEagerRelationships();

    @Query("select release from Release release left join fetch release.definitions where release.id =:id")
    Release findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select release from Release release where release.project.id = :projectId")
    List<Release> findByProjectId(@Param("projectId") Long projectId);

}
