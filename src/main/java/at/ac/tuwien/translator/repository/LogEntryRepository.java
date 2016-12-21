package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.LogEntry;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the LogEntry entity.
 */
@SuppressWarnings("unused")
public interface LogEntryRepository extends JpaRepository<LogEntry,Long> {

    @Query("select logEntry from LogEntry logEntry where logEntry.user.login = ?#{principal.username}")
    List<LogEntry> findByUserIsCurrentUser();

}
