package at.ac.tuwien.translator.web.rest;

import com.codahale.metrics.annotation.Timed;
import at.ac.tuwien.translator.domain.LogEntry;

import at.ac.tuwien.translator.repository.LogEntryRepository;
import at.ac.tuwien.translator.web.rest.util.HeaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing LogEntry.
 */
@RestController
@RequestMapping("/api")
public class LogEntryResource {

    private final Logger log = LoggerFactory.getLogger(LogEntryResource.class);
        
    @Inject
    private LogEntryRepository logEntryRepository;

    /**
     * POST  /log-entries : Create a new logEntry.
     *
     * @param logEntry the logEntry to create
     * @return the ResponseEntity with status 201 (Created) and with body the new logEntry, or with status 400 (Bad Request) if the logEntry has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/log-entries")
    @Timed
    public ResponseEntity<LogEntry> createLogEntry(@Valid @RequestBody LogEntry logEntry) throws URISyntaxException {
        log.debug("REST request to save LogEntry : {}", logEntry);
        if (logEntry.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("logEntry", "idexists", "A new logEntry cannot already have an ID")).body(null);
        }
        LogEntry result = logEntryRepository.save(logEntry);
        return ResponseEntity.created(new URI("/api/log-entries/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("logEntry", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /log-entries : Updates an existing logEntry.
     *
     * @param logEntry the logEntry to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated logEntry,
     * or with status 400 (Bad Request) if the logEntry is not valid,
     * or with status 500 (Internal Server Error) if the logEntry couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/log-entries")
    @Timed
    public ResponseEntity<LogEntry> updateLogEntry(@Valid @RequestBody LogEntry logEntry) throws URISyntaxException {
        log.debug("REST request to update LogEntry : {}", logEntry);
        if (logEntry.getId() == null) {
            return createLogEntry(logEntry);
        }
        LogEntry result = logEntryRepository.save(logEntry);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("logEntry", logEntry.getId().toString()))
            .body(result);
    }

    /**
     * GET  /log-entries : get all the logEntries.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of logEntries in body
     */
    @GetMapping("/log-entries")
    @Timed
    public List<LogEntry> getAllLogEntries() {
        log.debug("REST request to get all LogEntries");
        List<LogEntry> logEntries = logEntryRepository.findAll();
        return logEntries;
    }

    /**
     * GET  /log-entries/:id : get the "id" logEntry.
     *
     * @param id the id of the logEntry to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the logEntry, or with status 404 (Not Found)
     */
    @GetMapping("/log-entries/{id}")
    @Timed
    public ResponseEntity<LogEntry> getLogEntry(@PathVariable Long id) {
        log.debug("REST request to get LogEntry : {}", id);
        LogEntry logEntry = logEntryRepository.findOne(id);
        return Optional.ofNullable(logEntry)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /log-entries/:id : delete the "id" logEntry.
     *
     * @param id the id of the logEntry to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/log-entries/{id}")
    @Timed
    public ResponseEntity<Void> deleteLogEntry(@PathVariable Long id) {
        log.debug("REST request to delete LogEntry : {}", id);
        logEntryRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("logEntry", id.toString())).build();
    }

}
