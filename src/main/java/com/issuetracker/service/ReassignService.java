package com.issuetracker.service;

import com.issuetracker.dto.AssignRequest;
import com.issuetracker.dto.ReassignRequestDto;
import com.issuetracker.exception.BadRequestException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.Issue;
import com.issuetracker.model.ReassignRequest;
import com.issuetracker.model.User;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.ReassignRequestRepository;
import com.issuetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReassignService {

    private final ReassignRequestRepository reassignRepo;
    private final IssueRepository issueRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public ReassignService(ReassignRequestRepository reassignRepo, IssueRepository issueRepo,
                           UserRepository userRepo, NotificationService notificationService) {
        this.reassignRepo = reassignRepo;
        this.issueRepo = issueRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    public ReassignRequest createRequest(ReassignRequestDto dto) {
        Issue issue = issueRepo.findById(dto.getIssueId())
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
        User developer = userRepo.findById(dto.getDeveloperId())
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        if (reassignRepo.existsByIssue_IdAndDeveloper_IdAndStatus(
                dto.getIssueId(), dto.getDeveloperId(), ReassignRequest.RequestStatus.PENDING)) {
            throw new BadRequestException("A pending reassign request already exists for this issue.");
        }

        ReassignRequest req = new ReassignRequest();
        req.setReason(dto.getReason());
        req.setIssue(issue);
        req.setDeveloper(developer);
        ReassignRequest saved = reassignRepo.save(req);

        if (issue.getProject() != null && issue.getProject().getManager() != null) {
            notificationService.createNotification(
                "🔁 Developer \"" + developer.getUsername() + "\" requested reassignment for issue \""
                + issue.getTitle() + "\". Reason: " + dto.getReason(),
                issue.getProject().getManager(), issue);
        }
        return saved;
    }

    public List<ReassignRequest> getRequestsByDeveloper(Long developerId) {
        return reassignRepo.findByDeveloperId(developerId);
    }

    public List<ReassignRequest> getAllForManager(Long managerId) {
        return reassignRepo.findAllForManager(managerId);
    }

    /** Approve or Reject the request — does NOT reassign yet */
    public ReassignRequest handleRequest(Long requestId, boolean approved) {
        ReassignRequest req = reassignRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        req.setStatus(approved ? ReassignRequest.RequestStatus.APPROVED : ReassignRequest.RequestStatus.REJECTED);
        ReassignRequest saved = reassignRepo.save(req);

        String msg = approved
            ? "✅ Your reassignment request for issue \"" + req.getIssue().getTitle()
              + "\" was APPROVED. The manager will now reassign it to another developer."
            : "❌ Your reassignment request for issue \"" + req.getIssue().getTitle()
              + "\" was REJECTED. Please continue working on it.";
        notificationService.createNotification(msg, req.getDeveloper(), req.getIssue());

        return saved;
    }

    /** Actually reassign the issue to new developers after approval */
    public Issue doReassign(Long requestId, List<Long> newDeveloperIds, String dueDate) {
        ReassignRequest req = reassignRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (req.getStatus() != ReassignRequest.RequestStatus.APPROVED) {
            throw new BadRequestException("Request must be approved before reassigning.");
        }

        Issue issue = req.getIssue();
        Set<User> newDevs = new HashSet<>();
        for (Long devId : newDeveloperIds) {
            User dev = userRepo.findById(devId)
                    .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + devId));
            newDevs.add(dev);
        }

        // Remove old developer, add new ones
        issue.setDevelopers(newDevs);
        if (dueDate != null && !dueDate.isBlank()) {
            issue.setDueDate(LocalDate.parse(dueDate));
        }
        Issue saved = issueRepo.save(issue);

        // Mark request as completed
        req.setStatus(ReassignRequest.RequestStatus.COMPLETED);
        reassignRepo.save(req);

        // Notify old developer
        notificationService.createNotification(
            "🔁 You have been removed from issue \"" + issue.getTitle() + "\" and it has been reassigned.",
            req.getDeveloper(), issue);

        // Notify new developers
        for (User dev : newDevs) {
            notificationService.createNotification(
                "📋 You have been reassigned to issue: \"" + issue.getTitle() + "\""
                + (issue.getDueDate() != null ? " | Due: " + issue.getDueDate() : ""),
                dev, issue);
        }

        // Notify reporter
        if (issue.getReporter() != null) {
            String devNames = newDevs.stream().map(User::getUsername).reduce((a, b) -> a + ", " + b).orElse("developers");
            notificationService.createNotification(
                "🔁 Your issue \"" + issue.getTitle() + "\" has been reassigned to: " + devNames,
                issue.getReporter(), issue);
        }

        return saved;
    }
}
