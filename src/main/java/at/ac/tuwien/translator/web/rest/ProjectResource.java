package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.domain.LogEntry;
import at.ac.tuwien.translator.domain.User;
import at.ac.tuwien.translator.repository.LogEntryRepository;
import at.ac.tuwien.translator.repository.UserRepository;
import at.ac.tuwien.translator.security.SecurityUtils;
import at.ac.tuwien.translator.service.UserService;
import com.codahale.metrics.annotation.Timed;
import at.ac.tuwien.translator.domain.Project;

import at.ac.tuwien.translator.repository.ProjectRepository;
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Project.
 */
@RestController
@RequestMapping("/api")
public class ProjectResource {

    private final Logger log = LoggerFactory.getLogger(ProjectResource.class);

    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private UserService userService;
    @Inject
    private LogEntryRepository logEntryRepository;

    /**
     * POST  /projects : Create a new project.
     *
     * @param project the project to create
     * @return the ResponseEntity with status 201 (Created) and with body the new project, or with status 400 (Bad Request) if the project has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/projects")
    @Timed
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) throws URISyntaxException {
        log.debug("REST request to save Project : {}", project);
        if (project.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("project", "idexists", "A new project cannot already have an ID")).body(null);
        }
        Optional<User> userOptional = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        if(userOptional.isPresent()) {
            project.addUser(userOptional.get());
        }
        Project result = projectRepository.save(project);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Projekt " + result.getName() + " erstellt." , "erfolgreich", userService.getUserWithAuthorities(), result);
        logEntryRepository.save(logEntry);

        return ResponseEntity.created(new URI("/api/projects/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("project", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /projects : Updates an existing project.
     *
     * @param project the project to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated project,
     * or with status 400 (Bad Request) if the project is not valid,
     * or with status 500 (Internal Server Error) if the project couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/projects")
    @Timed
    public ResponseEntity<Project> updateProject(@Valid @RequestBody Project project) throws URISyntaxException {
        log.debug("REST request to update Project : {}", project);
        if (project.getId() == null) {
            return createProject(project);
        }
        Project result = projectRepository.save(project);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Projekt " + result.getName() + " geändert." , "erfolgreich", userService.getUserWithAuthorities(), result);
        logEntryRepository.save(logEntry);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("project", project.getId().toString()))
            .body(result);
    }

    /**
     * GET  /projects : get all the projects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of projects in body
     */
    @GetMapping("/projects")
    @Timed
    public List<Project> getAllProjects() {
        log.debug("REST request to get all Projects");
        List<Project> projects;

        User loggedInUser = userService.getUserWithAuthorities();

        if (loggedInUser == null || loggedInUser.isAdmin()) // loggedInUser == null for integration tests
            projects = projectRepository.findAllWithEagerRelationships();
        else
            projects = projectRepository.findAllWithEagerRelationshipsByUser(loggedInUser.getId());

        return projects;
    }

    /**
     * GET  /projects : get all the projects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of projects in body
     */
    @GetMapping("users/{userLogin}/singleproject")
    @Timed
    public Project getSingleProjectByUser(@PathVariable String userLogin) {
        return projectRepository.findSingleProjectByUserLogin(userLogin);
    }

    /**
     * GET  /projects/:id : get the "id" project.
     *
     * @param id the id of the project to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the project, or with status 404 (Not Found)
     */
    @GetMapping("/projects/{id}")
    @Timed
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        log.debug("REST request to get Project : {}", id);
        Project project = projectRepository.findOneWithEagerRelationships(id);
        return Optional.ofNullable(project)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /projects/:id : delete the "id" project.
     *
     * @param id the id of the project to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/projects/{id}")
    @Timed
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.debug("REST request to delete Project : {}", id);

        Project project = projectRepository.findOne(id);

        LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Projekt " + project.getName() + " geändert." , "erfolgreich", userService.getUserWithAuthorities(), project);
        logEntryRepository.save(logEntry);

        projectRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("project", id.toString())).build();
    }


    @GetMapping("/project/{projectId}/log")
    @Timed
    public List<LogEntry> getLogEntriesForProject(@PathVariable Long projectId) {
        log.debug("REST request to get all LogEntries for project with id " + projectId);
        List<LogEntry> logEntries = logEntryRepository.findByProject(projectId);
        return logEntries;
    }

}
