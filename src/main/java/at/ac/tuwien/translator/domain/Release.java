package at.ac.tuwien.translator.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Release.
 */
@Entity
@Table(name = "release")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Release implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "release_definition",
               joinColumns = @JoinColumn(name="releases_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="definitions_id", referencedColumnName="ID"))
    private Set<Definition> definitions = new HashSet<>();

    @ManyToOne
    private Project project;

    @Enumerated(EnumType.STRING)
    private ReleaseState state;

    public ReleaseState getState() {
        return state;
    }

    public void setState(ReleaseState state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Release name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Release deadline(LocalDate deadline) {
        this.deadline = deadline;
        return this;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Set<Definition> getDefinitions() {
        return definitions;
    }

    public Release definitions(Set<Definition> definitions) {
        this.definitions = definitions;
        return this;
    }

    public Release addDefinition(Definition definition) {
        definitions.add(definition);
        return this;
    }

    public Release removeDefinition(Definition definition) {
        definitions.remove(definition);
        return this;
    }

    public void setDefinitions(Set<Definition> definitions) {
        this.definitions = definitions;
    }

    public Project getProject() {
        return project;
    }

    public Release project(Project project) {
        this.project = project;
        return this;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Release release = (Release) o;
        if (release.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, release.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Release{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", deadline='" + deadline + "'" +
            '}';
    }
}
