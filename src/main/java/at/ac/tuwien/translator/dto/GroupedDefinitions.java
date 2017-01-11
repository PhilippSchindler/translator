package at.ac.tuwien.translator.dto;

import at.ac.tuwien.translator.domain.Definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupedDefinitions {

    private Map<String, List<Definition>> definitions = new HashMap<>();

    public GroupedDefinitions() {
    }

    public GroupedDefinitions(List<Definition> definitionList) {
        definitionList.forEach(this::addDefinition);
    }

    public Map<String, List<Definition>> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, List<Definition>> definitions) {
        this.definitions = definitions;
    }

    public void addDefinition(Definition definition) {
        List<Definition> list = this.definitions.get(definition.getLabel());
        if(list == null) {
            list = new ArrayList<>();
        }
        list.add(definition);
        this.definitions.put(definition.getLabel(), list);
    }
}
