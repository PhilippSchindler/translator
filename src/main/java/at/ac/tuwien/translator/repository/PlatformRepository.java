package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Platform;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Platform entity.
 */
@SuppressWarnings("unused")
public interface PlatformRepository extends JpaRepository<Platform,Long> {

}
