package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Definition;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Definition entity.
 */
@SuppressWarnings("unused")
public interface DefinitionRepository extends JpaRepository<Definition,Long> {

}
