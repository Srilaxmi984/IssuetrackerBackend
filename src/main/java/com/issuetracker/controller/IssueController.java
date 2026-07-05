package com.issuetracker.controller;

import com.issuetracker.dto.AssignRequest;
import com.issuetracker.dto.IssueRequest;
import com.issuetracker.model.Issue;
import com.issuetracker.service.IssueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "http://localhost:3000")
public class IssueController {

    private final IssueService issueService;
    private final ObjectMapper objectMapper;

    public IssueController(IssueService issueService, ObjectMapper objectMapper) {
        this.issueService = issueService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<Issue> createIssue(
            @RequestPart("issue") String issueJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        IssueRequest request = objectMapper.readValue(issueJson, IssueRequest.class);
        return ResponseEntity.ok(issueService.createIssue(request, file));
    }

    @GetMapping
    public ResponseEntity<List<Issue>> getAllIssues() {
        return ResponseEntity.ok(issueService.getAllIssues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<List<Issue>> getByReporter(@PathVariable Long reporterId) {
        return ResponseEntity.ok(issueService.getIssuesByReporter(reporterId));
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<List<Issue>> getByDeveloper(@PathVariable Long developerId) {
        return ResponseEntity.ok(issueService.getIssuesByDeveloper(developerId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Issue>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(issueService.getIssuesByProject(projectId));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<Issue> assignIssue(@PathVariable Long id, @RequestBody AssignRequest request) {
        return ResponseEntity.ok(issueService.assignIssue(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Issue> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long developerId,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(issueService.updateStatus(id, status, file, developerId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Issue> approveIssue(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam Long reporterId,
            @RequestParam(required = false) String rejectionNote) {
        return ResponseEntity.ok(issueService.approveIssue(id, approved, reporterId, rejectionNote));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(issueService.getStats());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Issue>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) Long developerId) {
        if (reporterId != null) {
            return ResponseEntity.ok(issueService.searchIssuesForReporter(keyword, reporterId));
        }
        if (developerId != null) {
            return ResponseEntity.ok(issueService.searchIssuesForDeveloper(keyword, developerId));
        }
        return ResponseEntity.ok(issueService.searchAllIssues(keyword));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Issue>> filter(
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status) {
        if (priority != null && !priority.isEmpty()) return ResponseEntity.ok(issueService.filterByPriority(priority));
        if (status != null && !status.isEmpty()) return ResponseEntity.ok(issueService.filterByStatus(status));
        return ResponseEntity.ok(issueService.getAllIssues());
    }
}
