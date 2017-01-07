package at.ac.tuwien.translator.config;

import at.ac.tuwien.translator.service.AndroidMessagesFile;
import at.ac.tuwien.translator.service.AndroidMessagesFileEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.xstream.XStreamMarshaller;

@Configuration
public class XStreamConfig {
    @Bean
    public XStreamMarshaller getXStreamMarshaller() {
        XStreamMarshaller xstreamMarshaller = new XStreamMarshaller();
        xstreamMarshaller.setAnnotatedClasses(AndroidMessagesFile.class, AndroidMessagesFileEntry.class);
        return xstreamMarshaller;
    }
}
