package com.issuetracker.dto;

import java.util.List;

public class AssignRequest {
    private List<Long> developerIds;
    private String dueDate;

    public AssignRequest() {}

    public List<Long> getDeveloperIds() { return developerIds; }
    public void setDeveloperIds(List<Long> developerIds) { this.developerIds = developerIds; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
