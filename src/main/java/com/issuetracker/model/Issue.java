package com.issuetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.issuetracker.enums.Priority;
import com.issuetracker.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "issues")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "issue_type", length = 100)
    private String issueType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.OPENED;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "file_path", length = 512)
    private String filePath;

    @Column(name = "resolved_file_path", length = 512)
    private String resolvedFilePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User reporter;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "issue_developers",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "developer_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private Set<User> developers = new HashSet<>();

    public Issue() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getResolvedFilePath() { return resolvedFilePath; }
    public void setResolvedFilePath(String resolvedFilePath) { this.resolvedFilePath = resolvedFilePath; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    public Set<User> getDevelopers() { return developers; }
    public void setDevelopers(Set<User> developers) { this.developers = developers; }
}
