package at.ac.tuwien.translator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Project.
 */
@Entity
@Table(name = "project")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Definition> definitions = new HashSet<>();

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Release> releases = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "project_user",
               joinColumns = @JoinColumn(name="projects_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="users_id", referencedColumnName="ID"))
    private Set<User> users = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "project_platform",
               joinColumns = @JoinColumn(name="projects_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="platforms_id", referencedColumnName="ID"))
    private Set<Platform> platforms = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "project_language",
               joinColumns = @JoinColumn(name="projects_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="languages_id", referencedColumnName="ID"))
    private Set<Language> languages = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Project name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Project description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Definition> getDefinitions() {
        return definitions;
    }

    public Project definitions(Set<Definition> definitions) {
        this.definitions = definitions;
        return this;
    }

    public Project addDefinition(Definition definition) {
        definitions.add(definition);
        definition.setProject(this);
        return this;
    }

    public Project removeDefinition(Definition definition) {
        definitions.remove(definition);
        definition.setProject(null);
        return this;
    }

    public void setDefinitions(Set<Definition> definitions) {
        this.definitions = definitions;
    }

    public Set<Release> getReleases() {
        return releases;
    }

    public Project releases(Set<Release> releases) {
        this.releases = releases;
        return this;
    }

    public Project addRelease(Release release) {
        releases.add(release);
        release.setProject(this);
        return this;
    }

    public Project removeRelease(Release release) {
        releases.remove(release);
        release.setProject(null);
        return this;
    }

    public void setReleases(Set<Release> releases) {
        this.releases = releases;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Project users(Set<User> users) {
        this.users = users;
        return this;
    }

    public Project addUser(User user) {
        users.add(user);
        return this;
    }

    public Project removeUser(User user) {
        users.remove(user);
        return this;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Platform> getPlatforms() {
        return platforms;
    }

    public Project platforms(Set<Platform> platforms) {
        this.platforms = platforms;
        return this;
    }

    public Project addPlatform(Platform platform) {
        platforms.add(platform);
        return this;
    }

    public Project removePlatform(Platform platform) {
        platforms.remove(platform);
        return this;
    }

    public void setPlatforms(Set<Platform> platforms) {
        this.platforms = platforms;
    }

    public Set<Language> getLanguages() {
        return languages;
    }

    public Project languages(Set<Language> languages) {
        this.languages = languages;
        return this;
    }

    public Project addLanguage(Language language) {
        languages.add(language);
        return this;
    }

    public Project removeLanguage(Language language) {
        languages.remove(language);
        return this;
    }

    public void setLanguages(Set<Language> languages) {
        this.languages = languages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        if (project.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Project{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", description='" + description + "'" +
            '}';
    }
}
