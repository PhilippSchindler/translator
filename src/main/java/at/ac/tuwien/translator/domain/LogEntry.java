package at.ac.tuwien.translator.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A LogEntry.
 */
@Entity
@Table(name = "log_entry")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private ZonedDateTime timestamp;

    @NotNull
    @Column(name = "message", nullable = false)
    private String message;

    @NotNull
    @Column(name = "result", nullable = false)
    private String result;

    @ManyToOne
    private User user;

    @ManyToOne
    private Project project;

    public LogEntry() {
    }

    public LogEntry(ZonedDateTime timestamp, String message, String result, User user, Project project) {
        this.timestamp = timestamp;
        this.message = message;
        this.result = result;
        this.user = user;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public LogEntry timestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public LogEntry message(String message) {
        this.message = message;
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public LogEntry result(String result) {
        this.result = result;
        return this;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public User getUser() {
        return user;
    }

    public LogEntry user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntry logEntry = (LogEntry) o;

        if (id != null ? !id.equals(logEntry.id) : logEntry.id != null) return false;
        if (timestamp != null ? !timestamp.equals(logEntry.timestamp) : logEntry.timestamp != null) return false;
        if (message != null ? !message.equals(logEntry.message) : logEntry.message != null) return false;
        if (result != null ? !result.equals(logEntry.result) : logEntry.result != null) return false;
        if (user != null ? !user.equals(logEntry.user) : logEntry.user != null) return false;
        return project != null ? project.equals(logEntry.project) : logEntry.project == null;

    }

    @Override
    public int hashCode() {
        int result1 = id != null ? id.hashCode() : 0;
        result1 = 31 * result1 + (timestamp != null ? timestamp.hashCode() : 0);
        result1 = 31 * result1 + (message != null ? message.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (user != null ? user.hashCode() : 0);
        result1 = 31 * result1 + (project != null ? project.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
            "id=" + id +
            ", timestamp=" + timestamp +
            ", message='" + message + '\'' +
            ", result='" + result + '\'' +
            ", user=" + user +
            ", project=" + project +
            '}';
    }
}
