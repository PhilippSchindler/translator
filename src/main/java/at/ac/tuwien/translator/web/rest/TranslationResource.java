package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.repository.*;
import at.ac.tuwien.translator.security.SecurityUtils;
import at.ac.tuwien.translator.service.ReleaseService;
import at.ac.tuwien.translator.service.TranslationService;
import at.ac.tuwien.translator.service.UserService;
import at.ac.tuwien.translator.web.rest.errors.TranslatorException;
import com.codahale.metrics.annotation.Timed;

import at.ac.tuwien.translator.web.rest.util.HeaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Translation.
 */
@RestController
@RequestMapping("/api")
public class TranslationResource {

    private final Logger log = LoggerFactory.getLogger(TranslationResource.class);

    @Inject
    private TranslationRepository translationRepository;
    @Inject
    private TranslationService translationService;
    @Inject
    private UserService userService;
    @Inject
    private LogEntryRepository logEntryRepository;
    @Inject
    private DefinitionRepository definitionRepository;
    @Inject
    private ReleaseService releaseService;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private LanguageRepository languageRepository;
    @Inject
    private ReleaseRepository releaseRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private PasswordEncoder passwordEncoder;

    /**
     * POST  /translations : Create a new translation.
     *
     * @param translation the translation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new translation, or with status 400 (Bad Request) if the translation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/translations")
    @Timed
    public ResponseEntity<Translation> createTranslation(@Valid @RequestBody Translation translation) throws URISyntaxException {
        log.debug("REST request to save Translation : {}", translation);
        if (translation.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("translation", "idexists", "A new translation cannot already have an ID")).body(null);
        }
        Translation result = translationRepository.save(translation);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Definition " + result.getDefinition().getLabel() + " in die Sprache " + result.getLanguage().getName() + " übersetzt" , "erfolgreich", userService.getUserWithAuthorities(), result.getDefinition().getProject());
        logEntryRepository.save(logEntry);

        return ResponseEntity.created(new URI("/api/translations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("translation", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /translations : Updates an existing translation.
     *
     * @param translation the translation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated translation,
     * or with status 400 (Bad Request) if the translation is not valid,
     * or with status 500 (Internal Server Error) if the translation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/translations")
    @Timed
    public ResponseEntity<Translation> updateTranslation(@Valid @RequestBody Translation translation) throws URISyntaxException {
        log.debug("REST request to update Translation : {}", translation);
        if (translation.getId() == null) {
            return createTranslation(translation);
        }
        Translation result = translationRepository.save(translation);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Definition " + result.getDefinition().getLabel() + " für die Sprache " + result.getLanguage().getName() + " geändert" , "erfolgreich", userService.getUserWithAuthorities(), result.getDefinition().getProject());
        logEntryRepository.save(logEntry);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("translation", translation.getId().toString()))
            .body(result);
    }

    @PutMapping("/translations/updateChangedTranslations")
    @Timed
    public ResponseEntity<Void> updateChangedTranslations(@RequestBody List<DefinitionToUpdate> definitions) throws URISyntaxException {
        translationService.updateChangedTranslations(definitions);
        releaseService.tryToFinishAllOpenReleases();

        for(DefinitionToUpdate def : definitions) {
            Definition definition = definitionRepository.findOne(def.getDefinitionId());
            LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Übersetzungen für die Definition " + definition.getText() + " geändert" , "erfolgreich", userService.getUserWithAuthorities(), definition.getProject());
            logEntryRepository.save(logEntry);
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/translations/import/{format}/{languageId}")
    @Timed
    public ResponseEntity<String> importTranslations(@PathVariable String format, @PathVariable Long languageId, @RequestBody String fileContent) throws URISyntaxException {
        try {
            int saved = translationService.importTranslations(format, languageId, fileContent);
            return ResponseEntity.ok().body("{\"numberOfImportedTranslations\": " + saved + "}");
        }
        catch (TranslatorException e){
            LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Import fehlgeschlagen. " + e.getMessage() , "fehlgeschlagen", userService.getUserWithAuthorities(), projectRepository.findSingleProjectByUserLogin(SecurityUtils.getCurrentUserLogin()));
            logEntryRepository.save(logEntry);
            throw e;
        }
    }

    @GetMapping("translations/export/{format}/{languageId}/{releaseId}")
    public ResponseEntity<String> exportTranslations(@PathVariable String format, @PathVariable Long languageId,
                                                     @PathVariable Long releaseId, HttpServletResponse response){
        try {
            String fileContent = translationService.exportTranslations(format, languageId, releaseId);
            return ResponseEntity.ok().body(fileContent);
        }
        catch (TranslatorException e){
            LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Release " + releaseId + " konnte nicht exportiert werden. " + e.getMessage() , "fehlgeschlagen", userService.getUserWithAuthorities(), projectRepository.findSingleProjectByUserLogin(SecurityUtils.getCurrentUserLogin()));
            logEntryRepository.save(logEntry);
            throw e;
        }

    }

    @GetMapping("translations/exportDirect/{format}/{languageName}/{releaseName}")
    @Transactional
    public ResponseEntity<String> exportTranslationDirectAPI(@PathVariable String format, @PathVariable String languageName,
                                                             @PathVariable String releaseName,
                                                             @RequestHeader("Authorization") String authorization,
                                                             HttpServletResponse response){

        User callingUser = null;
        try {
            if(authorization == null || authorization.isEmpty() || !authorization.matches("Basic \\S+"))
                throw new IllegalArgumentException();
            String usernamePasswordBase64 = authorization.split(" ")[1];
            String usernamePassword = new String(Base64.getDecoder().decode(usernamePasswordBase64));
            if(!usernamePassword.matches("\\S+:\\S+"))
                throw new IllegalArgumentException();
            String[] splitUsernamePassword = usernamePassword.split(":");
            Optional<User> user = userRepository.findOneByLogin(splitUsernamePassword[0]);
            if(!user.isPresent())
                throw new IllegalArgumentException();

            if(!passwordEncoder.matches(splitUsernamePassword[1], user.get().getPassword()))
                throw new IllegalArgumentException();

            if(!user.get().getAuthorities().stream().anyMatch(a -> a.getName().equals("ROLE_DEVELOPER")))
                throw new IllegalArgumentException();

            callingUser = user.get();
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid authorization!");
        }

        Project project = callingUser.getProjects().get(0);
        Optional<Language> languageOptional = project.getLanguages().stream().filter(l -> l.getName().equals(languageName)).findFirst();
        Optional<Release> releaseOptional = project.getReleases().stream().filter(r -> r.getName().equals(releaseName)).findFirst();

        if(!languageOptional.isPresent() || !releaseOptional.isPresent())
            return ResponseEntity.badRequest().body("Language or release invalid");

        return exportTranslations(format, languageOptional.get().getId(), releaseOptional.get().getId(), response);
    }

    /**
     * GET  /translations : get all the translations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of translations in body
     */
    @GetMapping("/translations")
    @Timed
    public List<Translation> getAllTranslations() {
        log.debug("REST request to get all Translations");
        List<Translation> translations = translationRepository.findAllWithEagerRelationships();
        return translations;
    }

    /**
     * GET  /translations/:id : get the "id" translation.
     *
     * @param id the id of the translation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the translation, or with status 404 (Not Found)
     */
    @GetMapping("/translations/{id}")
    @Timed
    public ResponseEntity<Translation> getTranslation(@PathVariable Long id) {
        log.debug("REST request to get Translation : {}", id);
        Translation translation = translationRepository.findOneWithEagerRelationships(id);
        return Optional.ofNullable(translation)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /translations/:id : delete the "id" translation.
     *
     * @param id the id of the translation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/translations/{id}")
    @Timed
    public ResponseEntity<Void> deleteTranslation(@PathVariable Long id) {
        log.debug("REST request to delete Translation : {}", id);

        Translation result = translationRepository.findOne(id);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Definition " + result.getDefinition().getLabel() + " für die Sprache " + result.getLanguage().getName() + " gelöscht" , "erfolgreich", userService.getUserWithAuthorities(), result.getDefinition().getProject());
        logEntryRepository.save(logEntry);

        translationRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("translation", id.toString())).build();
    }

}
