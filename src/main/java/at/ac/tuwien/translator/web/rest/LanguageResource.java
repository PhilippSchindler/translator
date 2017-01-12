package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.repository.*;
import at.ac.tuwien.translator.service.UserService;
import at.ac.tuwien.translator.web.rest.util.HeaderUtil;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.codec.language.bm.Lang;
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
import java.util.Set;

/**
 * REST controller for managing Language.
 */
@RestController
@RequestMapping("/api")
public class LanguageResource {

    private final Logger log = LoggerFactory.getLogger(LanguageResource.class);

    @Inject
    private LanguageRepository languageRepository;

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    TranslationRepository translationRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private LogEntryRepository logEntryRepository;

    /**
     * POST  /languages : Create a new language.
     *
     * @param language the language to create
     * @return the ResponseEntity with status 201 (Created) and with body the new language, or with status 400 (Bad Request) if the language has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/languages")
    @Timed
    public ResponseEntity<Language> createLanguage(@Valid @RequestBody Language language) throws URISyntaxException {
        log.debug("REST request to save Language : {}", language);
        if (language.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("language", "idexists", "A new language cannot already have an ID")).body(null);
        }

        User loggedInUser = userService.getUserWithAuthorities();
        if (loggedInUser != null)
            language.setUser(loggedInUser);

        Language result = languageRepository.save(language);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Sprache " + result.getName() + " erstellt." , "erfolgreich", userService.getUserWithAuthorities(), null);
        logEntryRepository.save(logEntry);

        return ResponseEntity.created(new URI("/api/languages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("language", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /languages : Updates an existing language.
     *
     * @param language the language to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated language,
     * or with status 400 (Bad Request) if the language is not valid,
     * or with status 500 (Internal Server Error) if the language couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/languages")
    @Timed
    public ResponseEntity<Language> updateLanguage(@Valid @RequestBody Language language) throws URISyntaxException {
        log.debug("REST request to update Language : {}", language);
        if (language.getId() == null) {
            return createLanguage(language);
        }
        Language result = languageRepository.save(language);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Sprache " + result.getName() + " geändert." , "erfolgreich", userService.getUserWithAuthorities(), null);
        logEntryRepository.save(logEntry);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("language", language.getId().toString()))
            .body(result);
    }

    @PutMapping("/languages/updateProjectAssignment/{projectId}")
    @Timed
    public ResponseEntity<Void> updateProjectAssignment(
        @PathVariable Long projectId, @Valid @RequestBody Set<Language> languages) throws URISyntaxException {

        Project project = projectRepository.findOne(projectId);

        if(project != null){
            project.setLanguages(languages);
            projectRepository.save(project);
        }

       /* for(Language lang : languages) {
            LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Sprache " + lang.getName() + " zum Projekt hinzugefügt.", "erfolgreich", userService.getUserWithAuthorities(), project);
            logEntryRepository.save(logEntry);
        }*/

        return ResponseEntity.ok().build();
    }

    /**
     * GET  /languages : get all the languages.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of languages in body
     */
    @GetMapping("/languages")
    @Timed
    public List<Language> getAllLanguages() {
        log.debug("REST request to get all Languages");
        List<Language> languages;

        User loggedInUser = userService.getUserWithAuthorities();

        if (loggedInUser == null || loggedInUser.isAdmin()) // loggedInUser == null for integration tests
            languages = languageRepository.findAll();
        else
            languages = languageRepository.findByUser(loggedInUser.getId());

        return languages;
    }

    /**
     * GET  /languages/:id : get the "id" language.
     *
     * @param id the id of the language to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the language, or with status 404 (Not Found)
     */
    @GetMapping("/languages/{id}")
    @Timed
    public ResponseEntity<Language> getLanguage(@PathVariable Long id) {
        log.debug("REST request to get Language : {}", id);
        Language language = languageRepository.findOne(id);
        return Optional.ofNullable(language)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /languages/:id : delete the "id" language.
     *
     * @param id the id of the language to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/languages/{id}")
    @Timed
    public ResponseEntity<Void> deleteLanguage(@PathVariable Long id) {
        log.debug("REST request to delete Language : {}", id);

        int numProjects = languageRepository.findNumOfLanguageUsagesInProjects(id);
        log.debug("Language " + id + " used in " + numProjects + " projects");

        int numTranslations = languageRepository.findNumOfLanguageUsagesInTranslations(id);

        if (numProjects == 0 && numTranslations == 0) {
            // language is not used an can be deleted safely
            languageRepository.delete(id);
            return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("language", id.toString())).build();
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).headers(HeaderUtil.createFailureAlert(
            "language", "languageDeleteConflict",
            "Cannot delete language, as it is used in projects or translations."
        )).build();
    }

}
