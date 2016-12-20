package at.ac.tuwien.translator.dto;

public class LanguageNotTranslatedDto {

    private String language;

    private int missingTranslations;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getMissingTranslations() {
        return missingTranslations;
    }

    public void setMissingTranslations(int missingTranslations) {
        this.missingTranslations = missingTranslations;
    }
}
