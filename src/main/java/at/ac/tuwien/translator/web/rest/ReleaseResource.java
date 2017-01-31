package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.repository.LogEntryRepository;
import at.ac.tuwien.translator.service.UserService;
import com.codahale.metrics.annotation.Timed;
import at.ac.tuwien.translator.dto.SelectedVersions;
import at.ac.tuwien.translator.repository.ReleaseRepository;
import at.ac.tuwien.translator.service.ReleaseService;
import at.ac.tuwien.translator.web.rest.util.HeaderUtil;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Release.
 */
@RestController
@RequestMapping("/api")
public class ReleaseResource {

    private final Logger log = LoggerFactory.getLogger(ReleaseResource.class);

    @Inject
    private ReleaseRepository releaseRepository;
    @Inject
    private UserService userService;
    @Inject
    private LogEntryRepository logEntryRepository;

    @Inject
    private ReleaseService releaseService;

    /**
     * POST  /releases : Create a new release.
     *
     * @param release the release to create
     * @return the ResponseEntity with status 201 (Created) and with body the new release, or with status 400 (Bad Request) if the release has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/releases")
    @Timed
    public ResponseEntity<Release> createRelease(@Valid @RequestBody Release release) throws URISyntaxException {
        log.debug("REST request to save Release : {}", release);
        if (release.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("release", "idexists", "A new release cannot already have an ID")).body(null);
        }
        release.setState(ReleaseState.CREATED);
        Release result = releaseRepository.save(release);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Release " + result.getName() + " erstellt." , "erfolgreich", userService.getUserWithAuthorities(), result.getProject());
        logEntryRepository.save(logEntry);

        return ResponseEntity.created(new URI("/api/releases/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("release", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /releases : Updates an existing release.
     *
     * @param release the release to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated release,
     * or with status 400 (Bad Request) if the release is not valid,
     * or with status 500 (Internal Server Error) if the release couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/releases")
    @Timed
    public ResponseEntity<Release> updateRelease(@Valid @RequestBody Release release) throws URISyntaxException {
        log.debug("REST request to update Release : {}", release);
        if (release.getId() == null) {
            return createRelease(release);
        }
        Release result = releaseRepository.save(release);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Release " + result.getName() + " geändert." , "erfolgreich", userService.getUserWithAuthorities(), result.getProject());
        logEntryRepository.save(logEntry);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("release", release.getId().toString()))
            .body(result);
    }

    /**
     * GET  /releases : get all the releases.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of releases in body
     */
    @GetMapping("/releases")
    @Timed
    public List<Release> getAllReleases() {
        log.debug("REST request to get all Releases");
        List<Release> releases = releaseRepository.findAllWithEagerRelationships();
        return releases;
    }

    /**
     * GET  /releases/:id : get the "id" release.
     *
     * @param id the id of the release to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the release, or with status 404 (Not Found)
     */
    @GetMapping("/releases/{id}")
    @Timed
    public ResponseEntity<Release> getRelease(@PathVariable Long id) {
        log.debug("REST request to get Release : {}", id);
        Release release = releaseRepository.findOneWithEagerRelationships(id);
        return Optional.ofNullable(release)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /releases/:id : delete the "id" release.
     *
     * @param id the id of the release to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/releases/{id}")
    @Timed
    public ResponseEntity<Void> deleteRelease(@PathVariable Long id) {
        log.debug("REST request to delete Release : {}", id);

        Release result = releaseRepository.findOne(id);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Release " + result.getName() + " gelöscht." , "erfolgreich", userService.getUserWithAuthorities(), result.getProject());
        logEntryRepository.save(logEntry);

        releaseRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("release", id.toString())).build();
    }

    @GetMapping("/releases/project/{projectId}")
    @Timed
    public List<Release> getReleasesByProject(@PathVariable Long projectId) {
        log.debug("REST request to get releases by project");
        return releaseRepository.findByProjectId(projectId);
    }

    @PostMapping("/releases/{releaseId}/selectedVersions")
    @Timed
    public ResponseEntity<Void> updateDefinitions(@PathVariable Long releaseId, @RequestBody SelectedVersions selectedVersions) throws URISyntaxException {
        log.debug("REST request to update definitions for Release : {}, selectedVersions : {}", releaseId, selectedVersions);
        try {
            releaseService.updateDefinitions(releaseId, selectedVersions);
        } catch (Exception e) {
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @GetMapping("/releases/{id}/selectedVersions")
    @Timed
    public SelectedVersions get(@PathVariable Long id) {
        log.debug("REST request to get selectedVersions for Release : {}" , id);
        return releaseService.loadAndTransformDefinitionsFor(id);
    }
}
