package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.domain.Release;
import at.ac.tuwien.translator.dto.SelectedVersions;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.ReleaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ReleaseService {

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private DefinitionRepository definitionRepository;

    public void updateDefinitions(Long releaseId, SelectedVersions selectedVersions) {
        Release release = releaseRepository.findOne(releaseId);
        if (release == null) {
            throw new IllegalStateException("Did not find release for id=" + releaseId);
        }
        Set<Definition> definitions = new HashSet<>();
        for (Map.Entry<String, Integer> entry : selectedVersions.getSelectedVersions().entrySet()) {
            Definition definition;
            if (entry.getValue() == -1) {
                definition = definitionRepository.save(Definition.getNewestVersionPlaceholder(entry.getKey()));
            } else {
                definition = definitionRepository.findByProject_idAndLabelAndVersion(release.getProject().getId(), entry.getKey(), entry.getValue());
            }
            if (definition == null) {
                throw new IllegalStateException("Did not find definition for projectId=" + release.getProject().getId() + ", label=" + entry.getKey() + ", version=" + entry.getValue());
            }
            definitions.add(definition);
        }
        release.setDefinitions(definitions);
        releaseRepository.save(release);
    }

    public SelectedVersions loadAndTransformDefinitionsFor(Long releaseId) {
        Release release = releaseRepository.findOneWithEagerRelationships(releaseId);
        if (release == null) {
            throw new IllegalStateException("Did not find release for id=" + releaseId);
        }
        Map<String, Integer> definitions = new HashMap<>();
        for(Definition definition : release.getDefinitions()) {
            definitions.put(definition.getLabel(), definition.getVersion());
        }
        return new SelectedVersions(definitions.size() > 0 ? definitions : null);
    }

}
