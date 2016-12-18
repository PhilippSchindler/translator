package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.web.rest.util.HeaderUtil;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Definition.
 */
@RestController
@RequestMapping("/api")
public class DefinitionResource {

    public static final int INITIAL_VERSION = 0;
    private final Logger log = LoggerFactory.getLogger(DefinitionResource.class);

    @Inject
    private DefinitionRepository definitionRepository;

    /**
     * POST  /definitions : Create a new definition.
     *
     * @param definition the definition to create
     * @return the ResponseEntity with status 201 (Created) and with body the new definition, or with status 400 (Bad Request) if the definition has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/definitions")
    @Timed
    public ResponseEntity<Definition> createDefinition(@RequestBody Definition definition) throws URISyntaxException {
        log.debug("REST request to save Definition : {}", definition);
        if (definition.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("definition", "idexists", "A new definition cannot already have an ID")).body(null);
        }

        ZonedDateTime now = ZonedDateTime.now();
        definition.setCreatedAt(now);
        definition.setUpdatedAt(now);
        definition.setVersion(INITIAL_VERSION);

        return createValidDefinition(definition);
    }

    private ResponseEntity<Definition> createValidDefinition(@Valid Definition definition) throws URISyntaxException {
        Definition result = definitionRepository.save(definition);
        return ResponseEntity.created(new URI("/api/definitions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("definition", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /definitions : Updates an existing definition.
     *
     * @param definition the definition to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated definition,
     * or with status 400 (Bad Request) if the definition is not valid,
     * or with status 500 (Internal Server Error) if the definition couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/definitions")
    @Timed
    public ResponseEntity<Definition> updateDefinition(@RequestBody Definition definition) throws URISyntaxException {
        log.debug("REST request to update Definition : {}", definition);
        if (definition.getId() == null) {
            return createDefinition(definition);
        }
        return updateValidDefinition(definition);
    }

    private ResponseEntity<Definition> updateValidDefinition(@Valid Definition definition) throws URISyntaxException {
        Definition newVersion = new Definition();
        newVersion.setLabel(definition.getLabel());
        newVersion.setText(definition.getText());
        newVersion.setVersion(definition.getVersion() + 1);
        newVersion.setCreatedAt(definition.getCreatedAt());
        newVersion.setUpdatedAt(ZonedDateTime.now());
        newVersion.setProject(definition.getProject());

        return createValidDefinition(newVersion);
    }

    /**
     * GET  /definitions : get all the definitions.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of definitions in body
     */
    @GetMapping("/definitions")
    @Timed
    public List<Definition> getAllDefinitions() {
        log.debug("REST request to get all Definitions");
        List<Definition> definitions = definitionRepository.findAll();
        return definitions;
    }

    /**
     * GET  /definitions/:id : get the "id" definition.
     *
     * @param id the id of the definition to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the definition, or with status 404 (Not Found)
     */
    @GetMapping("/definitions/{id}")
    @Timed
    public ResponseEntity<Definition> getDefinition(@PathVariable Long id) {
        log.debug("REST request to get Definition : {}", id);
        Definition definition = definitionRepository.findOne(id);
        return Optional.ofNullable(definition)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /definitions/:id : delete the "id" definition.
     *
     * @param id the id of the definition to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/definitions/{id}")
    @Timed
    public ResponseEntity<Void> deleteDefinition(@PathVariable Long id) {
        log.debug("REST request to delete Definition : {}", id);
        definitionRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("definition", id.toString())).build();
    }

    @GetMapping("/project/{projectId}/definitions")
    @Timed
    public List<Definition> getDefinitionsForProject(@PathVariable Long projectId) {
        log.debug("REST request to get all Definitions for project: {]", projectId);
        return definitionRepository.findByProject_id(projectId);
    }

}
