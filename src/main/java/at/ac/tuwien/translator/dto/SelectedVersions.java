package at.ac.tuwien.translator.dto;

import java.util.Map;

public class SelectedVersions {

    private Map<String, Integer> selectedVersions;

    public SelectedVersions(Map<String, Integer> selectedVersions) {
        this.selectedVersions = selectedVersions;
    }

    public Map<String, Integer> getSelectedVersions() {
        return selectedVersions;
    }

    public void setSelectedVersions(Map<String, Integer> selectedVersions) {
        this.selectedVersions = selectedVersions;
    }
}
