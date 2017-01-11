package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.domain.Language;
import at.ac.tuwien.translator.domain.Project;
import at.ac.tuwien.translator.domain.Translation;
import at.ac.tuwien.translator.repository.DefinitionRepository;
import at.ac.tuwien.translator.repository.LanguageRepository;
import at.ac.tuwien.translator.repository.ProjectRepository;
import at.ac.tuwien.translator.repository.TranslationRepository;
import at.ac.tuwien.translator.security.SecurityUtils;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImportExportService {

    @Inject
    private XStreamMarshaller xstream;

    @Inject
    private DefinitionRepository definitionRepository;
    @Inject
    private TranslationRepository translationRepository;
    @Inject
    private LanguageRepository languageRepository;
    @Inject
    private ProjectRepository projectRepository;


    public int importAndroid(Long languageId, String fileContent) {
        AndroidMessagesFile messages = null;
        try {
             messages = (AndroidMessagesFile) xstream.unmarshal(new StreamSource(new StringReader(fileContent)));
        } catch (IOException e) {
            return 0;
        }

        Project project = projectRepository.findSingleProjectByUserLogin(SecurityUtils.getCurrentUserLogin());

        Language language = null;
        if(languageId > 0)
            language = languageRepository.findOne(languageId);

        List<Definition> existingDefinitions = definitionRepository.findLatestByProject(project.getId());

        int counterSaved = 0;
        for (AndroidMessagesFileEntry entry : messages.strings) {
            List<Definition> possibleDefinition =
                existingDefinitions.stream().filter(definition -> definition.getLabel().equals(entry.name)).collect(Collectors.toList());
            if(possibleDefinition.isEmpty())
                createDefinition(project, language, entry);
            else
                updateDefinition(possibleDefinition.get(0), language, entry);

            counterSaved++;
        }


        return counterSaved;
    }

    private void createDefinition(Project project, Language language, AndroidMessagesFileEntry entry) {
        Definition definition = new Definition();
        ZonedDateTime now = ZonedDateTime.now();
        definition.setCreatedAt(now);
        definition.setUpdatedAt(now);
        definition.setLabel(entry.name);
        definition.setProject(project);

        if(language == null){
            definition.setVersion(0);
            definition.setText(entry.text);
            definitionRepository.save(definition);
        } else {
            definition.setVersion(1);
            definition.setText("");
            Definition savedDefinition = definitionRepository.save(definition);
            Translation translation = new Translation();
            translation.setText(entry.text);
            translation.setUpdatedAt(now);
            translation.setLanguage(language);
            translation.setDeleted(false);
            translation.setDefinition(savedDefinition);
            translationRepository.save(translation);
        }
    }

    private void updateDefinition(Definition original, Language language, AndroidMessagesFileEntry entry) {
        Definition newVersion = new Definition()
            .createdAt(original.getCreatedAt())
            .label(original.getLabel())
            .project(original.getProject())
            .version(original.getVersion() + 1)
            .updatedAt(ZonedDateTime.now());

        //If english
        if(language == null){
            newVersion.text(entry.text);
            definitionRepository.save(newVersion);
        } else {
            newVersion.text(original.getText());
            Definition newSavedDefinition = definitionRepository.save(newVersion);

            List<Translation> oldTranslations = translationRepository.findByDefinition(original);
            //Copy all translations, but the one for the selected language
            for (Translation oldTranslation : oldTranslations) {
                if(oldTranslation.getLanguage().equals(language))
                    continue;

                Translation newTranslation = new Translation(oldTranslation);
                newTranslation.setUpdatedAt(ZonedDateTime.now());
                newTranslation.setDefinition(newSavedDefinition);
                translationRepository.save(newTranslation);
            }

            Translation newTranslation = new Translation();
            newTranslation.setDeleted(false);
            newTranslation.setLanguage(language);
            newTranslation.setUpdatedAt(ZonedDateTime.now());
            newTranslation.setDefinition(newSavedDefinition);
            newTranslation.setText(entry.text);
            translationRepository.save(newTranslation);

        }
    }
}
