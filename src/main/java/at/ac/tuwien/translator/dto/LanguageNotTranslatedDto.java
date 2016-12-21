package at.ac.tuwien.translator.dto;

public class LanguageNotTranslatedDto {

    private String language;

    private long missingTranslations;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getMissingTranslations() {
        return missingTranslations;
    }

    public void setMissingTranslations(long missingTranslations) {
        this.missingTranslations = missingTranslations;
    }
}
