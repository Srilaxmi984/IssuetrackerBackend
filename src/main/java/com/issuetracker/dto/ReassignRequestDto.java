package com.issuetracker.dto;

public class ReassignRequestDto {
    private String reason;
    private Long developerId;
    private Long issueId;

    public ReassignRequestDto() {}
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getDeveloperId() { return developerId; }
    public void setDeveloperId(Long developerId) { this.developerId = developerId; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
}
