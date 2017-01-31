package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.*;
import at.ac.tuwien.translator.dto.SelectedVersion;
import at.ac.tuwien.translator.dto.SelectedVersions;
import at.ac.tuwien.translator.repository.*;
import at.ac.tuwien.translator.security.AuthoritiesConstants;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReleaseService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);
    private static final String SUBJECT_RELEASE_FINISHED = "Release '%s' ist abgeschlossen";
    private static final String MESSAGE_RELEASE_FINISHED = "Hallo %s,\n\ndie Übersetzungsarbeiten für den Release '%s' wurden abgeschlossen.\n" +
        "Damit ist dieser Release nun fertig.\n\nMit freundlichen Grüßen,\ndas Translator-Team";


    private static final String SUBJECT_TRANSLATIONS_MISSING = "Fehlende Übersetzungen im Projekt '%s'";
    private static final String MESSAGE_TRANSLATIONS_MISSING =
        "Hallo %s %s,\n\n" +
        "du erhälst diese Benachrichtung, da du am Projekt '%s' als Übersetzer beteiligt bist.\n" +
        "Am %s ist die Deadline für das Release '%s' und es sind noch Übersetzungen ausständig.\n\n" +
        "Mit freundlichen Grüßen,\n" +
        "das Translator-Team";

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DefinitionRepository definitionRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

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
        if (definitions.size() > 0) {
            LogEntry logEntry = new LogEntry(ZonedDateTime.now(), "Release " + release.getName() + " wurde geändert.", "erfolgreich", userService.getUserWithAuthorities(), release.getProject());
            logEntryRepository.save(logEntry);
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

    public void finishReleaseIfPossible(final Release release) {
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
            Release savedRelease = releaseRepository.save(release);
            sendNotificationEmailForFinishedRelease(savedRelease);
        } catch (IllegalStateException e) {
            log.info("Could not finish release : {}", release, e);
        }
    }

    private void sendNotificationEmailForFinishedRelease(Release release) {
        List<User> usersToNotify = userRepository.findByProjectIdAndAuthority(release.getProject().getId(), "ROLE_CUSTOMER", "ROLE_RELEASE_MANAGER");
        for (User user : usersToNotify) {
            sendEmail(user, release);
        }
    }

    private void sendEmail(User user, Release release) {
        String subject = String.format(SUBJECT_RELEASE_FINISHED, release.getName());
        String text = String.format(MESSAGE_RELEASE_FINISHED, user.getFirstName() + " " + user.getLastName(), release.getName());
        notificationService.sendEmail(user.getEmail(), subject, text);
    }

    public void tryToFinishAllOpenReleases() {
        List<Release> releases = releaseRepository.findAllWithEagerRelationshipsInState(ReleaseState.DEFINITIONS_ASSIGNED);
        releases.forEach(this::finishReleaseIfPossible);
    }


    @Scheduled(fixedRate = 1000 * 60) // in ms => 1min derzeit
    public void checkAndSendNotificationForMissingTranslations()
    {
        System.out.println();
        System.out.println();
        System.out.println("============================================================");
        System.out.println();
        System.out.println("Running release notification task for missing translations at " + new Date());
        System.out.println();
        List<Release> releases = releaseRepository.findAll();
        System.out.println();

        for (Release r : releases) {

            if (r.getState() == ReleaseState.FINISHED) {
                System.out.println("Notification für Release '" + r.getName() + "' nicht notwendig; ReleaseState == FINISHED");
                continue;
            }

            if (r.isNotificationSent()) {
                System.out.println("Notification für Release '" + r.getName() + "' nicht notwendig; Notification bereits versendet");
                continue;
            }

            LocalDate in5Days = LocalDate.now().plusDays(5);
            if (r.getDeadline().isAfter(in5Days)) {
                System.out.println("Notification für Release '" + r.getName() + "' nicht notwendig; Deadline nicht innerhalb von 5 Tagen");
                continue;
            }

            // deadline coming close, check if all translations have been made
            // and send email if not
            // ReleaseState.FINISHED is automatic set when translations get added

//            int numNonEnglishLanguages = r.getProject().getLanguages().size();
//            boolean sendNotification = false;
//            for (Definition d : r.getDefinitions()) {
//                if (d.getTranslations().size() < numNonEnglishLanguages)
//                {
//                    sendNotification = true;
//                    break;
//                }
//            }

            System.out.println("Notification für Release '" + r.getName() + "' wird versendet");
            sendNotificationEmails(r);

            r.setNotificationSent(true);
            releaseRepository.save(r);
        }


        System.out.println("============================================================");
        System.out.println();
        System.out.println();
    }

    private void sendNotificationEmails(Release r) {

        Project p = projectRepository.findOneWithEagerRelationships(r.getProject().getId());
        Set<User> users = p.getUsers();

        Hibernate.initialize(users);
        for (User u : users) {
            for (Authority a : u.getAuthorities()) {
                if (a.getName().equals(AuthoritiesConstants.TRANSLATOR)) {
                    // this user is a translator for this project, send the email
                    notificationService.sendEmail(u.getEmail(),
                        String.format(SUBJECT_TRANSLATIONS_MISSING, r.getProject().getName()),
                        String.format(MESSAGE_TRANSLATIONS_MISSING,
                            u.getFirstName(), u.getLastName(),
                            r.getProject().getName(),
                            r.getDeadline().toString(),
                            r.getName())
                        );
                    break;
                }
            }
        }
    }
}
