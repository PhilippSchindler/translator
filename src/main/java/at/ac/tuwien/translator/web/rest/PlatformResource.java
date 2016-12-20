package at.ac.tuwien.translator.web.rest;

import com.codahale.metrics.annotation.Timed;
import at.ac.tuwien.translator.domain.Platform;

import at.ac.tuwien.translator.repository.PlatformRepository;
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
 * REST controller for managing Platform.
 */
@RestController
@RequestMapping("/api")
public class PlatformResource {

    private final Logger log = LoggerFactory.getLogger(PlatformResource.class);
        
    @Inject
    private PlatformRepository platformRepository;

    /**
     * POST  /platforms : Create a new platform.
     *
     * @param platform the platform to create
     * @return the ResponseEntity with status 201 (Created) and with body the new platform, or with status 400 (Bad Request) if the platform has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/platforms")
    @Timed
    public ResponseEntity<Platform> createPlatform(@Valid @RequestBody Platform platform) throws URISyntaxException {
        log.debug("REST request to save Platform : {}", platform);
        if (platform.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("platform", "idexists", "A new platform cannot already have an ID")).body(null);
        }
        Platform result = platformRepository.save(platform);
        return ResponseEntity.created(new URI("/api/platforms/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("platform", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /platforms : Updates an existing platform.
     *
     * @param platform the platform to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated platform,
     * or with status 400 (Bad Request) if the platform is not valid,
     * or with status 500 (Internal Server Error) if the platform couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/platforms")
    @Timed
    public ResponseEntity<Platform> updatePlatform(@Valid @RequestBody Platform platform) throws URISyntaxException {
        log.debug("REST request to update Platform : {}", platform);
        if (platform.getId() == null) {
            return createPlatform(platform);
        }
        Platform result = platformRepository.save(platform);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("platform", platform.getId().toString()))
            .body(result);
    }

    /**
     * GET  /platforms : get all the platforms.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of platforms in body
     */
    @GetMapping("/platforms")
    @Timed
    public List<Platform> getAllPlatforms() {
        log.debug("REST request to get all Platforms");
        List<Platform> platforms = platformRepository.findAll();
        return platforms;
    }

    /**
     * GET  /platforms/:id : get the "id" platform.
     *
     * @param id the id of the platform to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the platform, or with status 404 (Not Found)
     */
    @GetMapping("/platforms/{id}")
    @Timed
    public ResponseEntity<Platform> getPlatform(@PathVariable Long id) {
        log.debug("REST request to get Platform : {}", id);
        Platform platform = platformRepository.findOne(id);
        return Optional.ofNullable(platform)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /platforms/:id : delete the "id" platform.
     *
     * @param id the id of the platform to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/platforms/{id}")
    @Timed
    public ResponseEntity<Void> deletePlatform(@PathVariable Long id) {
        log.debug("REST request to delete Platform : {}", id);
        platformRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("platform", id.toString())).build();
    }

}
