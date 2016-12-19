package at.ac.tuwien.translator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Translation.
 */
@Entity
@Table(name = "translation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Translation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "text", nullable = false)
    private String text;

    @NotNull
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "translation_language",
               joinColumns = @JoinColumn(name="translations_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="languages_id", referencedColumnName="ID"))
    private Set<Language> languages = new HashSet<>();

    @ManyToOne
    @JsonIgnore
    private Definition definition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public Translation text(String text) {
        this.text = text;
        return this;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public Translation deleted(Boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Translation updatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Language> getLanguages() {
        return languages;
    }

    public Translation languages(Set<Language> languages) {
        this.languages = languages;
        return this;
    }

    public Translation addLanguage(Language language) {
        languages.add(language);
        return this;
    }

    public Translation removeLanguage(Language language) {
        languages.remove(language);
        return this;
    }

    public void setLanguages(Set<Language> languages) {
        this.languages = languages;
    }

    public Definition getDefinition() {
        return definition;
    }

    public Translation definition(Definition definition) {
        this.definition = definition;
        return this;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Translation translation = (Translation) o;
        if (translation.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, translation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Translation{" +
            "id=" + id +
            ", text='" + text + "'" +
            ", deleted='" + deleted + "'" +
            ", updatedAt='" + updatedAt + "'" +
            '}';
    }
}
