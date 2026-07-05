package com.issuetracker.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private String message;
    private String createdBy;
    private LocalDateTime createdDate;
    private Long issueId;

    public CommentResponse() {}

    public CommentResponse(Long id, String message, String createdBy, LocalDateTime createdDate, Long issueId) {
        this.id = id;
        this.message = message;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.issueId = issueId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
}
