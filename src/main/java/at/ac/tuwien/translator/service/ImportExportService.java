package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.repository.*;
import at.ac.tuwien.translator.security.SecurityUtils;
import at.ac.tuwien.translator.web.rest.errors.TranslatorException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    private final Logger log = LoggerFactory.getLogger(ImportExportService.class);

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
    private LogEntryRepository logEntryRepository;
    @Inject
    private UserService userService;
    @Inject
    private ReleaseRepository releaseRepository;


    public int importAndroid(Long languageId, String fileContent) {
        AndroidMessagesFile messages = null;
        try {
             messages = (AndroidMessagesFile) xstream.unmarshal(new StreamSource(new StringReader(fileContent)));
        } catch (Exception e) {
            throw new TranslatorException(String.format(
                "Android-Import Fehler: XML-Dateiformat fehlerhaft."));
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
            if(possibleDefinition.isEmpty()) {
                createDefinition(project, language, entry.name, entry.text);
                LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Defintion " + entry.name + " importiert (Android).", "erfolgreich", userService.getUserWithAuthorities(), project);
                logEntryRepository.save(logEntry);
            } else {
                updateDefinition(possibleDefinition.get(0), language, entry.text);
                LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Defintion " + entry.name + " durch import geändert (Android)." , "erfolgreich", userService.getUserWithAuthorities(), project);
                logEntryRepository.save(logEntry);
            }
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

                if (!valid)
                    throw new TranslatorException(String.format(
                        "Globalize-Import Fehler: Sprache %s ist im Projekt nicht vorhanden."));

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
            throw new TranslatorException(String.format(
                "Globalize-Import Fehler: JSON-Dateiformat fehlerhaft."));
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

                if (existingDefinition == null) {
                    existingDefinitions.add(
                        createDefinition(project, importedLanguage, importedLabel, importedValue)
                    );
                    LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Defintion " + importedLabel + " importiert (Globalize)." , "erfolgreich", userService.getUserWithAuthorities(), project);
                    logEntryRepository.save(logEntry);
                } else {
                    updateDefinition(existingDefinition, importedLanguage, importedValue);
                    LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Defintion " + importedLabel + " durch import geändert (Globalize)." , "erfolgreich", userService.getUserWithAuthorities(), project);
                    logEntryRepository.save(logEntry);
                }
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
            LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Release " + releaseId + " exportiert (Android)." , "erfolgreich", userService.getUserWithAuthorities(), projectRepository.findSingleProjectByUserLogin(SecurityUtils.getCurrentUserLogin()));
            logEntryRepository.save(logEntry);
            return stringWriter.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new TranslatorException("Android-Export Fehler");
        }
    }

    public String exportGlobalize(Long releaseId) {
        try {
            JSONObject jsonObj = new JSONObject();

            for (Definition definition : getDefinitionsOfRelease(releaseId)) {

                Set<Translation> translations = new HashSet<>(definition.getTranslations());
                Translation tEnglish = new Translation();
                tEnglish.setLanguage(Language.EN);
                tEnglish.setText(definition.getLabel());

                for (Translation translation : translations) {

                    String languageCode = translation.getLanguage().getShortName();
                    JSONObject jsonLanguageObj;


                    if (jsonObj.has(languageCode))
                        jsonLanguageObj = jsonObj.getJSONObject(languageCode);
                    else {
                        jsonLanguageObj = new JSONObject();
                        jsonObj.put(languageCode, jsonLanguageObj);
                    }

                    jsonLanguageObj.put(definition.getLabel(), translation.getText());
                }
            }
            LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Release " + releaseId + " exportiert (Globalize)." , "erfolgreich", userService.getUserWithAuthorities(), projectRepository.findSingleProjectByUserLogin(SecurityUtils.getCurrentUserLogin()));
            logEntryRepository.save(logEntry);
            return jsonObj.toString(4);
        }

        catch (JSONException e) {
            throw new TranslatorException("Globalize-Export Fehler");
        }
    }

    private Set<Definition> getDefinitionsOfRelease(Long releaseId){
        return releaseRepository.findOne(releaseId).getDefinitions();
    }
}
