package com.issuetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reassign_requests")
public class ReassignRequest {

    public enum RequestStatus { PENDING, APPROVED, REJECTED, COMPLETED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    // Expose issue id + title only - no circular ref
    @Transient
    private Long issueId;
    @Transient
    private String issueTitle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "developer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler","password"})
    private User developer;

    public ReassignRequest() {}

    @PostLoad
    public void populateTransient() {
        if (issue != null) {
            this.issueId = issue.getId();
            this.issueTitle = issue.getTitle();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime d) { this.createdDate = d; }
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public Long getIssueId() { return issueId; }
    public String getIssueTitle() { return issueTitle; }
    public User getDeveloper() { return developer; }
    public void setDeveloper(User developer) { this.developer = developer; }
}
