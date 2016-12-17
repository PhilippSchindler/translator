package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Translation;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Translation entity.
 */
@SuppressWarnings("unused")
public interface TranslationRepository extends JpaRepository<Translation,Long> {

    @Query("select distinct translation from Translation translation left join fetch translation.languages")
    List<Translation> findAllWithEagerRelationships();

    @Query("select translation from Translation translation left join fetch translation.languages where translation.id =:id")
    Translation findOneWithEagerRelationships(@Param("id") Long id);

}
