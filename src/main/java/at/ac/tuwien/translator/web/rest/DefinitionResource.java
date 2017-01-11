package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.dto.GroupedDefinitions;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.TranslationRepository;
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
import java.util.stream.Collectors;

/**
 * REST controller for managing Definition.
 */
@RestController
@RequestMapping("/api")
public class DefinitionResource {

    private static final int INITIAL_VERSION = 0;
    private final Logger log = LoggerFactory.getLogger(DefinitionResource.class);

    @Inject
    private DefinitionRepository definitionRepository;

    @Inject
    private TranslationRepository translationRepository;

    /**
     * POST  /definitions : Create a new definition.
     *
     * @param definition the definition to create
     * @return the ResponseEntity with status 201 (Created) and with body the new definition, or with status 400 (Bad Request) if the definition has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/definitions")
    @Timed
    public ResponseEntity<Definition> createDefinition(@Valid @RequestBody Definition definition) throws URISyntaxException {
        log.debug("REST request to save Definition : {}", definition);
        if (definition.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("definition", "idexists", "A new definition cannot already have an ID")).body(null);
        }

        List<Definition> foundDefinitions = definitionRepository.findByLabel(definition.getLabel());
        if (foundDefinitions != null && foundDefinitions.size() > 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("definition", "labelexists", "A definition with this label exists already")).body(null);
        }

        ZonedDateTime now = ZonedDateTime.now();
        definition.setCreatedAt(now);
        definition.setUpdatedAt(now);
        definition.setVersion(INITIAL_VERSION);

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
    public ResponseEntity<Definition> updateDefinition(@Valid @RequestBody Definition definition) throws URISyntaxException {
        log.debug("REST request to update Definition : {}", definition);
        if (definition.getId() == null) {
            return createDefinition(definition);
        }

        Definition original = definitionRepository.findOne(definition.getId());

        if (original.getText().equals(definition.getText())) {
            return ResponseEntity.ok(original);
        }

        Definition newVersion = new Definition()
            .createdAt(original.getCreatedAt())
            .label(original.getLabel())
            .project(original.getProject())
            .version(original.getVersion() + 1)
            .updatedAt(ZonedDateTime.now())
            .text(definition.getText());

        Definition result = definitionRepository.save(newVersion);
        return ResponseEntity.created(new URI("/api/definitions/" + result.getId()))
            .headers(HeaderUtil.createEntityUpdateAlert("definition", result.getId().toString()))
            .body(result);
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
        return definitionRepository.findAll();
    }

    @GetMapping("/projects/{projectId}/definitions/latest")
    public List<Definition> getDefinitionsByProject(@PathVariable Long projectId) {
        List<Definition> definitions = definitionRepository.findLatestByProject(projectId);
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
        Definition definition = definitionRepository.findOne(id);
        List<Definition> definitions = definitionRepository.findByLabel(definition.getLabel());
        for (Definition def : definitions) {
            definitionRepository.delete(def);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("definition", id.toString())).build();
    }

    @GetMapping("/project/{projectId}/definitions")
    @Timed
    public List<Definition> getDefinitionsForProject(@PathVariable Long projectId) {
        log.debug("REST request to get all Definitions for project: {]", projectId);
        return definitionRepository.findForProject(projectId);
    }

    @GetMapping("/project/{projectId}/groupedDefinitions")
    @Timed
    public ResponseEntity<GroupedDefinitions> getGroupedDefinitionsFor(@PathVariable Long projectId) {
        log.debug("REST request to get ALL Versions of all Definitions for project: {]", projectId);
        List<Definition> definitionList = definitionRepository.findByProject_id(projectId);
        if (definitionList == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        definitionList.forEach(this::loadTranslations);
        log.debug("Loaded definitions: {}, detail: {}", definitionList.size(), definitionList);
        return new ResponseEntity<>(new GroupedDefinitions(definitionList), HttpStatus.OK);
    }

    private void loadTranslations(Definition definition) {
        definition.setTranslations(translationRepository.findByDefinition(definition).stream().collect(Collectors.toSet()));
    }

}
