package com.issuetracker.controller;

import com.issuetracker.dto.ReassignRequestDto;
import com.issuetracker.model.Issue;
import com.issuetracker.model.ReassignRequest;
import com.issuetracker.service.ReassignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reassign")
@CrossOrigin(origins = "http://localhost:3000")
public class ReassignController {

    private final ReassignService reassignService;

    public ReassignController(ReassignService reassignService) {
        this.reassignService = reassignService;
    }

    @PostMapping
    public ResponseEntity<ReassignRequest> createRequest(@RequestBody ReassignRequestDto dto) {
        return ResponseEntity.ok(reassignService.createRequest(dto));
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<List<ReassignRequest>> getByDeveloper(@PathVariable Long developerId) {
        return ResponseEntity.ok(reassignService.getRequestsByDeveloper(developerId));
    }

    @GetMapping("/manager/{managerId}/all")
    public ResponseEntity<List<ReassignRequest>> getAllForManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(reassignService.getAllForManager(managerId));
    }

    // Keep old endpoint for compatibility
    @GetMapping("/manager/{managerId}/pending")
    public ResponseEntity<List<ReassignRequest>> getPendingForManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(reassignService.getAllForManager(managerId));
    }

    @PutMapping("/{id}/handle")
    public ResponseEntity<ReassignRequest> handleRequest(
            @PathVariable Long id,
            @RequestParam boolean approved) {
        return ResponseEntity.ok(reassignService.handleRequest(id, approved));
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<Issue> doReassign(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> devIdInts = (List<Integer>) body.get("developerIds");
        List<Long> devIds = devIdInts.stream().map(Integer::longValue).toList();
        String dueDate = (String) body.get("dueDate");
        return ResponseEntity.ok(reassignService.doReassign(id, devIds, dueDate));
    }
}
