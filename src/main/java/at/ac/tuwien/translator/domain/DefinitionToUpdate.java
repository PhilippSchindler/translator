package at.ac.tuwien.translator.domain;

import java.util.List;

public class DefinitionToUpdate {
    private Long definitionId;
    private List<TranslationToUpdate> translations;

    public Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    public List<TranslationToUpdate> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TranslationToUpdate> translations) {
        this.translations = translations;
    }
}
