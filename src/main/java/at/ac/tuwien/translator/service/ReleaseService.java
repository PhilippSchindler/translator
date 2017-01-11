package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.domain.Release;
import at.ac.tuwien.translator.dto.SelectedVersion;
import at.ac.tuwien.translator.dto.SelectedVersions;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.ReleaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
        for (SelectedVersion selectedVersion : selectedVersions.getSelectedVersions()) {
            String label = selectedVersion.getLabel();
            Integer version = selectedVersion.getVersion();
            Definition definition;
            if (version == -1) {
                definition = definitionRepository.findByLabelAndVersion(label, -1);
                if(definition == null) {
                    definition = definitionRepository.save(Definition.getNewestVersionPlaceholder(label));
                }
            } else {
                definition = definitionRepository.findByProject_idAndLabelAndVersion(release.getProject().getId(), label, version);
            }
            if (definition == null) {
                throw new IllegalStateException("Did not find definition for projectId=" + release.getProject().getId() + ", label=" + label + ", version=" + version);
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
        return new SelectedVersions(release.getDefinitions());
    }

}
