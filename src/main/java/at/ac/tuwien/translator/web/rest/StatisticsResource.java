package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.domain.Language;
import at.ac.tuwien.translator.dto.LanguageNotTranslatedDto;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.LanguageRepository;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for statistics
 */
@RestController
@RequestMapping("/api/projects/{projectId}/statistics")
public class StatisticsResource {

    private final Logger log = LoggerFactory.getLogger(StatisticsResource.class);

    @Inject
    private LanguageRepository languageRepository;

    @Inject
    private DefinitionRepository definitionRepository;

    @GetMapping("/notTranslatedTexts")
    @Timed
    public List<LanguageNotTranslatedDto> getNoTranslatedStatistics(@PathVariable Long projectId) {
        log.debug("REST request to get statistics about not translated texts for project : {}", projectId);

        List<Language> languages = languageRepository.findByProjects_id(projectId);
        List<Definition> definitions = definitionRepository.findLatestByProject(projectId);

        if (languages == null || definitions == null) {
            return new ArrayList<>();
        }

        return languages.stream().map(language -> calculateItem(language, definitions)).collect(Collectors.toList());
    }

    private LanguageNotTranslatedDto calculateItem(Language language, List<Definition> definitions) {
        long translations = getTranslationsCountFor(language, definitions);
        long missingTranslations = definitions.size() - translations;
        return createLanguageNotTranslatedDto(language.getName(), missingTranslations);
    }

    private long getTranslationsCountFor(Language language, List<Definition> definitions) {
        return definitions
            .stream()
            .filter(definition -> definition
                .getTranslations()
                .stream()
                .anyMatch(translation -> translation.getLanguage().equals(language) && !StringUtils.isEmpty(translation.getText()))
            )
            .count();
    }

    private LanguageNotTranslatedDto createLanguageNotTranslatedDto(String language, long notTranslated) {
        LanguageNotTranslatedDto dto = new LanguageNotTranslatedDto();
        dto.setLanguage(language);
        dto.setMissingTranslations(notTranslated);
        return dto;
    }


}
