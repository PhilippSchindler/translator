package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.domain.Language;
import at.ac.tuwien.translator.domain.Project;
import at.ac.tuwien.translator.domain.Translation;
import at.ac.tuwien.translator.repository.*;
import at.ac.tuwien.translator.security.SecurityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.*;
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
    @Inject
    private ReleaseRepository releaseRepository;


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
                createDefinition(project, language, entry.name, entry.text);
            else
                updateDefinition(possibleDefinition.get(0), language, entry.text);

            counterSaved++;
        }


        return counterSaved;
    }

    private Definition createDefinition(Project project, Language language, String label, String text) {
        Definition definition = new Definition();
        ZonedDateTime now = ZonedDateTime.now();
        definition.setCreatedAt(now);
        definition.setUpdatedAt(now);
        definition.setLabel(label);
        definition.setProject(project);

        if(language == null || language == Language.EN){
            definition.setVersion(0);
            definition.setText(text);
            definitionRepository.save(definition);
        } else {
            definition.setVersion(1);
            definition.setText("");
            Definition savedDefinition = definitionRepository.save(definition);
            Translation translation = new Translation();
            translation.setText(text);
            translation.setUpdatedAt(now);
            translation.setLanguage(language);
            translation.setDeleted(false);
            translation.setDefinition(savedDefinition);
            translationRepository.save(translation);
        }

        return definition;
    }
    private void updateDefinition(Definition original, Language language, String text) {
        Definition newVersion = new Definition()
            .createdAt(original.getCreatedAt())
            .label(original.getLabel())
            .project(original.getProject())
            .version(original.getVersion() + 1)
            .updatedAt(ZonedDateTime.now());

        //If english
        if(language == null || language == Language.EN){
            newVersion.text(text);
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
            newTranslation.setText(text);
            translationRepository.save(newTranslation);

        }
    }


    public int importGlobalize(String fileContent)
    {
        JSONObject obj;
        List<String> importedLanguages = new ArrayList<>();
        List<String> importedLabels = new ArrayList<>();
        List<String> importedValues = new ArrayList<>();

        Project project = projectRepository.findSingleProjectByUserLogin(SecurityUtils.getCurrentUserLogin());
        Map<String, Language> languageMap = new HashMap<>(); // maps from imported langcode to Language obj
        Set<Language> validLanguages = new HashSet<>(project.getLanguages());
        validLanguages.add(Language.EN);

        // read json file, abort on syntax error in json file
        // store contents in imported lists
        // check of languages, abort if languages are present in the file but not assign to the existing project
        try {
            obj = new JSONObject(fileContent);
            Iterator<?> langIt = obj.keys();
            while( langIt.hasNext() ) {
                String importedLanguage = (String) langIt.next();

                boolean valid = false;
                for (Language validLanguage : validLanguages)
                    if (importedLanguage.equalsIgnoreCase(validLanguage.getShortName()))
                    {
                        valid = true;
                        languageMap.put(importedLanguage, validLanguage);
                        break;
                    }

                if (!valid)     // TODO error message, invalid language code...
                    return 0;

                JSONObject langObj = obj.getJSONObject(importedLanguage);
                Iterator<?> labelIt = langObj.keys();
                while( labelIt.hasNext() ) {
                    String label = (String) labelIt.next();
                    String value = langObj.getString(label);
                    importedLanguages.add(importedLanguage);
                    importedLabels.add(label);
                    importedValues.add(value);
                }
            }
        } catch (JSONException e) {
            return 0;
        }

        // parsed data is looking good
        // now create/update definitions

        List<Definition> existingDefinitions = new ArrayList<>(definitionRepository.findLatestByProject(project.getId()));

        for (int L = 0; L < 2; L++) {

            for (int i = 0; i < importedValues.size(); i++) {
                Language importedLanguage = languageMap.get(importedLanguages.get(i));

                // make sure english is imported first
                if (L == 0 && importedLanguage != Language.EN) continue;
                if (L == 1 && importedLanguage == Language.EN) continue;

                String importedLabel = importedLabels.get(i);
                String importedValue = importedValues.get(i);

                Definition existingDefinition = null;
                for (Definition d : existingDefinitions)
                    if (d.getLabel().equals(importedLabel)) {
                        existingDefinition = d;
                        break;
                    }

                if (existingDefinition == null)
                    existingDefinitions.add(
                        createDefinition(project, importedLanguage, importedLabel, importedValue)
                    );
                else
                    updateDefinition(existingDefinition, importedLanguage, importedValue);
            }
        }

        return importedValues.size();
    }

    public String exportAndroid(Long languageId, Long releaseId) {
        Set<Definition> definitions = getDefinitionsOfRelease(releaseId);

        AndroidMessagesFile androidMessagesFile = new AndroidMessagesFile();
        androidMessagesFile.strings = new ArrayList<>();

        if(languageId == 0) {//English
            for (Definition definition : definitions) {
                androidMessagesFile.strings.add(new AndroidMessagesFileEntry(definition.getLabel(), definition.getText()));
            }
        } else {
            Language language = languageRepository.findOne(languageId);
            List<Translation> translations = translationRepository.findByLanguageAndDefinitionIn(language, definitions);
            for (Translation translation : translations) {
                androidMessagesFile.strings.add(new AndroidMessagesFileEntry(translation.getDefinition().getLabel(), translation.getText()));
            }
        }

        StringWriter stringWriter = new StringWriter();
        try {
            xstream.marshal(androidMessagesFile, new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String exportGlobalize(Long releaseId) {
        return "{\"is\": \"awesome\"}";
    }

    private Set<Definition> getDefinitionsOfRelease(Long releaseId){
        return releaseRepository.findOne(releaseId).getDefinitions();
    }
}
