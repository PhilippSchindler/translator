package at.ac.tuwien.translator.domain;

public class TranslationToUpdate {
    private Long langId;
    private String text;

    public Long getLangId() {
        return langId;
    }

    public void setLangId(Long langId) {
        this.langId = langId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
