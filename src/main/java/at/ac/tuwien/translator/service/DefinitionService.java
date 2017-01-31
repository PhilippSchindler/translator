package at.ac.tuwien.translator.service;

import at.ac.tuwien.translator.domain.Project;
import at.ac.tuwien.translator.domain.User;
import at.ac.tuwien.translator.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class DefinitionService {

    private static final String SUBJECT_NEW_DEFINITION = "Neue Defintion zum Übersetzen";
    private static final String MESSAGE_NEW_DEFINITION = "Hallo %s,\n\ndu erhältst diese Benachrichtigung, da du am Projekt %s als Übersetzer beteiligt bist.\n" +
        "Es wurde soeben ein neuer Text angelegt, welcher nun zu Übersetzen ist.\n\nMit freundlichen Grüßen,\nDas Translator-Team";

    @Inject
    private UserRepository userRepository;
    @Inject
    private NotificationService notificationService;

    public void sendMailToTranslatorForNewDefinition(Project project) {
        List<User> translators = userRepository.findByProjectIdAndAuthority(project.getId(), "ROLE_TRANSLATOR");

        for (User translator : translators) {
            String text = String.format(MESSAGE_NEW_DEFINITION, translator.getFirstName() + " " + translator.getLastName(), project.getName());
            notificationService.sendEmail(translator.getEmail(), SUBJECT_NEW_DEFINITION, text);
        }

    }
}
