package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Project;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
public interface ProjectRepository extends JpaRepository<Project,Long> {

    @Query("select distinct project from Project project left join fetch project.users left join fetch project.platforms left join fetch project.languages")
    List<Project> findAllWithEagerRelationships();

    @Query("select project from Project project left join fetch project.users left join fetch project.platforms left join fetch project.languages where project.id =:id")
    Project findOneWithEagerRelationships(@Param("id") Long id);

    @Query("SELECT p FROM Project p JOIN p.users u WHERE u.login = (:userLogin)")
    Project findSingleProjectByUserLogin(@Param("userLogin") String userLogin);



    @Query("select distinct project from Project project " +
               "left join fetch project.users as u " +
               "left join fetch project.platforms " +
               "left join fetch project.languages " +
            "where u.id = (:userId) ")
    List<Project> findAllWithEagerRelationshipsByUser(@Param("userId")Long userId);
}
