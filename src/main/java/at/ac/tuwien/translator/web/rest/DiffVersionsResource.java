package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.repository.DefinitionRepository;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DiffVersionsResource {

    @Inject
    private DefinitionRepository definitionRepository;

    private final Logger log = LoggerFactory.getLogger(DiffVersionsResource.class);


    @GetMapping("/diffVersions/versions/{projectId}")
    @Timed
    public List<Integer> getVersionsFromProject(@PathVariable Long projectId) throws URISyntaxException {
        log.debug("REST request to get the versions from projet with the id" + projectId);

        return definitionRepository.listOfVersions(projectId);
    }

}
