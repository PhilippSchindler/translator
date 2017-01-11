package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.LanguageRepository;
import at.ac.tuwien.translator.repository.TranslationRepository;
import at.ac.tuwien.translator.web.rest.errors.TranslatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TranslationService {

    @Inject
    private DefinitionRepository definitionRepository;

    @Inject
    private TranslationRepository translationRepository;

    @Inject
    private LanguageRepository languageRepository;

    @Inject
    private ImportExportService importService;

    public void updateChangedTranslations(List<DefinitionToUpdate> definitions) {
        for (DefinitionToUpdate definitionToUpdate : definitions) {
            Definition oldDefinition = definitionRepository.findOne(definitionToUpdate.getDefinitionId());
            Definition newDefinition = createNewDefinition(oldDefinition);
            newDefinition.setText(definitionToUpdate.getDefinitionText());
            Definition savedDefinition = definitionRepository.saveAndFlush(newDefinition);

            Set<Translation> translations = new HashSet<>();

            for (TranslationToUpdate translationToUpdate : definitionToUpdate.getTranslations()) {
                Translation newTranslation = new Translation();
                newTranslation.setDefinition(savedDefinition);
                newTranslation.setUpdatedAt(ZonedDateTime.now());
                newTranslation.setDeleted(false);
                newTranslation.setText(translationToUpdate.getText());

                Language language = languageRepository.findOne(translationToUpdate.getLangId());
                newTranslation.setLanguage(language);
                translations.add(newTranslation);
            }
            translationRepository.save(translations);

        }

    }

    private Definition createNewDefinition(Definition oldDefinition) {
        Definition newDefinition = new Definition();
        newDefinition.setLabel(oldDefinition.getLabel());
        newDefinition.setProject(oldDefinition.getProject());
        newDefinition.setText(oldDefinition.getText());
        newDefinition.setCreatedAt(oldDefinition.getCreatedAt());
        newDefinition.setUpdatedAt(ZonedDateTime.now());
        newDefinition.setVersion(oldDefinition.getVersion() + 1);
        return newDefinition;
    }

    public int importTranslations(String format, Long languageId, String fileContent) {
        if(format.equals("android"))
            return importService.importAndroid(languageId, fileContent);

        else if (format.equals("globalize"))
            return importService.importGlobalize(fileContent);

        throw new TranslatorException("Import Fehler: ungültiges Import-Format!");
    }

    public String exportTranslations(String format, Long languageId, Long releaseId) {

        if(format.equals("android"))
            return importService.exportAndroid(languageId, releaseId);

        else if (format.equals("globalize"))
            return importService.exportGlobalize(releaseId);

        throw new TranslatorException("Export Fehler: ungültiges Export-Format!");
    }
}
