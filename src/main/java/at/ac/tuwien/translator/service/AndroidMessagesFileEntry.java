package at.ac.tuwien.translator.service;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("string")
@XStreamConverter(value=ToAttributedValueConverter.class, strings={"text"})
public class AndroidMessagesFileEntry {
    public String name;
    public String text;

    public AndroidMessagesFileEntry() {
    }

    public AndroidMessagesFileEntry(String name, String text) {
        this.name = name;
        this.text = text;
    }
}
