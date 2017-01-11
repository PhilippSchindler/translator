package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.domain.Translation;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for the Translation entity.
 */
@SuppressWarnings("unused")
public interface TranslationRepository extends JpaRepository<Translation,Long> {

    @Query("select distinct translation from Translation translation")
    List<Translation> findAllWithEagerRelationships();

    @Query("select translation from Translation translation where translation.id =:id")
    Translation findOneWithEagerRelationships(@Param("id") Long id);

    List<Translation> findByDefinition(Definition definition);
}
