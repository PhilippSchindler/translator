package at.ac.tuwien.translator.dto;

import at.ac.tuwien.translator.domain.Definition;

public class SelectedVersion {
    private String label;

    private Integer version;

    public SelectedVersion() {
    }

    public SelectedVersion(String label, Integer version) {
        this.label = label;
        this.version = version;
    }

    public SelectedVersion(Definition definition) {
        label = definition.getLabel();
        version = definition.getVersion();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
