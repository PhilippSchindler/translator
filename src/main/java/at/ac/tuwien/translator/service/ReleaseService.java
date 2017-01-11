package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.dto.SelectedVersion;
import at.ac.tuwien.translator.dto.SelectedVersions;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.LanguageRepository;
import at.ac.tuwien.translator.repository.ReleaseRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReleaseService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private DefinitionRepository definitionRepository;

    @Autowired
    private LanguageRepository languageRepository;

    public void updateDefinitions(Long releaseId, SelectedVersions selectedVersions) {
        Release release = releaseRepository.findOne(releaseId);
        if (release == null) {
            throw new IllegalStateException("Did not find release for id=" + releaseId);
        }
        if (ReleaseState.FINISHED.equals(release.getState())) {
            throw new IllegalStateException("Release already finished!");
        }
        Set<Definition> definitions = new HashSet<>();
        for (SelectedVersion selectedVersion : selectedVersions.getSelectedVersions()) {
            String label = selectedVersion.getLabel();
            Integer version = selectedVersion.getVersion();
            Definition definition;
            if (version == -1) {
                definition = definitionRepository.findByLabelAndVersion(label, -1);
                if (definition == null) {
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
        release.setState(ReleaseState.DEFINITIONS_ASSIGNED);
        release = releaseRepository.save(release);
        finishReleaseIfPossible(release);
    }

    public SelectedVersions loadAndTransformDefinitionsFor(Long releaseId) {
        Release release = releaseRepository.findOneWithEagerRelationships(releaseId);
        if (release == null) {
            throw new IllegalStateException("Did not find release for id=" + releaseId);
        }
        return new SelectedVersions(release.getDefinitions());
    }

    public void finishReleaseIfPossible(Release release) {
        try {
            List<Language> languages = languageRepository.findByProjects_id(release.getProject().getId());
            Set<Definition> definitions = release.getDefinitions();
            definitions = definitions.stream().map(definition -> {
                if (definition.getVersion() >= 0) {
                    return definition;
                }
                log.info("Loading latest definition with projectId : {}, label : {}", release.getProject().getId(), definition.getLabel());
                Definition latestDefinition = definitionRepository.findLatestByProjectAndLabel(release.getProject().getId(), definition.getLabel());
                log.info("Found latest definition : {}, translations : {}", latestDefinition, latestDefinition.getTranslations());
                for (Language language : languages) {
                    Optional<Translation> optional = latestDefinition.getTranslations().stream().filter(translation -> translation.getLanguage().getId().equals(language.getId())).findFirst();
                    if (!optional.isPresent()) {
                        throw new IllegalStateException("Did not find translation for language : " + language + ", label : " + definition.getLabel());
                    }
                    if (StringUtils.isEmpty(optional.get().getText())) {
                        throw new IllegalStateException("Empty text for language : " + language + ", label : " + definition.getLabel());
                    }
                }
                return latestDefinition;
            }).collect(Collectors.toSet());
            release.setDefinitions(definitions);
            release.setState(ReleaseState.FINISHED);
            // TODO send email
            releaseRepository.save(release);
        } catch (IllegalStateException e) {
            log.info("Could not finish release : {}", release, e);
        }
    }

    public void tryToFinishAllOpenReleases() {
        List<Release> releases = releaseRepository.findAllWithEagerRelationshipsInState(ReleaseState.DEFINITIONS_ASSIGNED);
        releases.forEach(this::finishReleaseIfPossible);
    }

}
