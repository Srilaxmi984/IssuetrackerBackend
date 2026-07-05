package com.issuetracker.dto;

public class CommentRequest {
    private String message;
    private String createdBy;

    public CommentRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
