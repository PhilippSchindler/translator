package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.dto.LanguageNotTranslatedDto;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for statistics
 */
@RestController
@RequestMapping("/api/projects/{projectId}/statistics")
public class StatisticsResource {

    private final Logger log = LoggerFactory.getLogger(StatisticsResource.class);


    @GetMapping("/notTranslatedTexts")
    @Timed
    public List<LanguageNotTranslatedDto> getNoTranslatedStatistics(@PathVariable Long projectId) {
        log.debug("REST request to get statistics about not translated texts");
        List<LanguageNotTranslatedDto> list = new ArrayList<>();
        list.add(createLanguageNotTranslatedDto("Deutsch", 15));
        list.add(createLanguageNotTranslatedDto("Französisch", 21));
        list.add(createLanguageNotTranslatedDto("Italienisch", 6));
        list.add(createLanguageNotTranslatedDto("Japanisch", 0));
        list.add(createLanguageNotTranslatedDto("Schwedisch", 27));
        return list;
    }

    private LanguageNotTranslatedDto createLanguageNotTranslatedDto(String language, int notTranslated) {
        LanguageNotTranslatedDto dto = new LanguageNotTranslatedDto();
        dto.setLanguage(language);
        dto.setMissingTranslations(notTranslated);
        return dto;
    }


}
