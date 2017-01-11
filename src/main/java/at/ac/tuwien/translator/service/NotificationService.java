package at.ac.tuwien.translator.service;

import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class NotificationService {

    private static final String sender = "translatorapp@gmx.at";
    private static final String password = "Wert1234";
    private static final String protocol = "smpt";
    private static final String host = "mail.gmx.net";
    private static final String port = "587";

    private Session mailSession;


    public NotificationService(){
        Properties properties = new Properties();

        properties.put("mail.transport.protocol", protocol);
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.user", this.sender);
        properties.put("mail.smtp.password", this.password);
        properties.put("mail.smtp.starttls.enable", "true");

        this.mailSession = Session.getInstance(properties, new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(properties.getProperty("mail.smtp.user"),
                    properties.getProperty("mail.smtp.password"));
            }
        });
    }

    public void sendEmail(String receiver, String subject, String body){
        try {
            Message message = new MimeMessage(mailSession);
            InternetAddress addressTo = new InternetAddress(receiver);
            message.setRecipient(Message.RecipientType.TO, addressTo);
            message.setFrom(new InternetAddress(sender));
            message.setSubject(subject);
            message.setContent(body, "text/plain");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new ServiceException("Email to recipient "+receiver+" could not be sent.");
        }
    }
}
