package at.ac.tuwien.translator.service;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.HashSet;

@XStreamAlias("resources")
public class AndroidMessagesFile {

    @XStreamImplicit
    public ArrayList<AndroidMessagesFileEntry> strings;

    public HashSet<String> getLabels(){
        HashSet<String> labels = new HashSet<>();
        if(strings != null){
            for (AndroidMessagesFileEntry string : strings) {
                labels.add(string.name);
            }
        }
        return labels;
    }
}
