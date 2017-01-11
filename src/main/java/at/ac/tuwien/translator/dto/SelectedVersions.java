package at.ac.tuwien.translator.dto;

import at.ac.tuwien.translator.domain.Definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectedVersions {

    private List<SelectedVersion> selectedVersions;

    public SelectedVersions() {
    }

    public SelectedVersions(Set<Definition> definitions) {
        selectedVersions = new ArrayList<>();
        for(Definition definition : definitions) {
            selectedVersions.add(new SelectedVersion(definition));
        }
    }

    public List<SelectedVersion> getSelectedVersions() {
        return selectedVersions;
    }

    public void setSelectedVersions(List<SelectedVersion> selectedVersions) {
        this.selectedVersions = selectedVersions;
    }
}
