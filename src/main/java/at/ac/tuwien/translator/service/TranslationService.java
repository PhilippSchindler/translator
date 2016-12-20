package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.LanguageRepository;
import at.ac.tuwien.translator.repository.TranslationRepository;
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

    public void updateChangedTranslations(List<DefinitionToUpdate> definitions) {
        for (DefinitionToUpdate definitionToUpdate : definitions) {
            Definition oldDefinition = definitionRepository.findOne(definitionToUpdate.getDefinitionId());
            Definition newDefinition = createNewDefinition(oldDefinition);
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
}
