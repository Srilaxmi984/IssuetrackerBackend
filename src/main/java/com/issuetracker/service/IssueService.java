package com.issuetracker.service;

import com.issuetracker.dto.AssignRequest;
import com.issuetracker.dto.IssueRequest;
import com.issuetracker.enums.Priority;
import com.issuetracker.enums.Status;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.Issue;
import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.ProjectRepository;
import com.issuetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public IssueService(IssueRepository issueRepository,
                        UserRepository userRepository,
                        ProjectRepository projectRepository,
                        NotificationService notificationService) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
    }

    public Issue createIssue(IssueRequest request, MultipartFile file) throws IOException {
        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setIssueType(request.getIssueType());
        issue.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));

        if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
            issue.setDueDate(LocalDate.parse(request.getDueDate()));
        }
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            issue.setProject(project);
        }
        if (request.getReporterId() != null) {
            User reporter = userRepository.findById(request.getReporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));
            issue.setReporter(reporter);
        }
        if (file != null && !file.isEmpty()) {
            issue.setFilePath(saveFile(file));
        }

        Issue saved = issueRepository.save(issue);

        // ✅ FIXED: Notify ONLY the manager of the issue's project (not all managers)
        if (project != null && project.getManager() != null) {
            notificationService.createNotification(
                "🐛 New issue reported in your project \"" + project.getName() + "\": \""
                + saved.getTitle() + "\" — awaiting assignment.",
                project.getManager(), saved);
        }

        return saved;
    }

    public List<Issue> getAllIssues() { return issueRepository.findAll(); }

    public Issue getIssueById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + id));
    }

    public List<Issue> getIssuesByReporter(Long reporterId) {
        return issueRepository.findByReporterId(reporterId);
    }

    public List<Issue> getIssuesByDeveloper(Long developerId) {
        return issueRepository.findByDeveloperId(developerId);
    }

    public List<Issue> getIssuesByProject(Long projectId) {
        return issueRepository.findByProjectId(projectId);
    }

    public Issue assignIssue(Long issueId, AssignRequest request) {
        Issue issue = getIssueById(issueId);
        Set<User> developers = new HashSet<>();
        for (Long devId : request.getDeveloperIds()) {
            User dev = userRepository.findById(devId)
                    .orElseThrow(() -> new ResourceNotFoundException("Developer not found: " + devId));
            developers.add(dev);
        }
        issue.setDevelopers(developers);
        if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
            issue.setDueDate(LocalDate.parse(request.getDueDate()));
        }
        Issue saved = issueRepository.save(issue);

        // Notify each assigned developer
        for (User dev : developers) {
            notificationService.createNotification(
                "📋 You have been assigned to issue: \"" + issue.getTitle() + "\""
                + (issue.getDueDate() != null ? " | Due: " + issue.getDueDate() : ""),
                dev, issue);
        }
        // Notify reporter
        if (issue.getReporter() != null) {
            String devNames = developers.stream().map(User::getUsername)
                    .reduce((a, b) -> a + ", " + b).orElse("developers");
            notificationService.createNotification(
                "Your issue \"" + issue.getTitle() + "\" has been assigned to: " + devNames,
                issue.getReporter(), issue);
        }
        return saved;
    }

    public Issue updateStatus(Long issueId, String status, MultipartFile resolvedFile, Long developerId) throws IOException {
        Issue issue = getIssueById(issueId);
        Status newStatus = Status.valueOf(status.toUpperCase());
        Status oldStatus = issue.getStatus();
        issue.setStatus(newStatus);
        if (resolvedFile != null && !resolvedFile.isEmpty()) {
            issue.setResolvedFilePath(saveFile(resolvedFile));
        }
        Issue saved = issueRepository.save(issue);

        String devName = "A developer";
        if (developerId != null) {
            devName = userRepository.findById(developerId).map(User::getUsername).orElse("A developer");
        }
        if (issue.getReporter() != null) {
            String msg = newStatus == Status.RESOLVED
                ? "✅ Issue \"" + issue.getTitle() + "\" marked RESOLVED by " + devName + ". Please review."
                : "🔄 Issue \"" + issue.getTitle() + "\" changed from " + oldStatus.name().replace("_"," ")
                  + " to " + newStatus.name().replace("_"," ") + " by " + devName;
            notificationService.createNotification(msg, issue.getReporter(), issue);
        }
        return saved;
    }

    public Issue approveIssue(Long issueId, boolean approved, Long reporterId, String rejectionNote) {
        Issue issue = getIssueById(issueId);
        String reporterName = userRepository.findById(reporterId).map(User::getUsername).orElse("Reporter");
        if (approved) {
            issue.setStatus(Status.CLOSED);
            for (User dev : issue.getDevelopers()) {
                notificationService.createNotification(
                    "🎉 Issue \"" + issue.getTitle() + "\" was marked SOLVED and closed by " + reporterName + ".",
                    dev, issue);
            }
        } else {
            issue.setStatus(Status.REOPENED);
            String note = (rejectionNote != null && !rejectionNote.isBlank()) ? " | Reason: " + rejectionNote : "";
            for (User dev : issue.getDevelopers()) {
                notificationService.createNotification(
                    "❌ Issue \"" + issue.getTitle() + "\" was marked NOT SOLVED by " + reporterName
                    + ". Please rework." + note, dev, issue);
            }
        }
        return issueRepository.save(issue);
    }

    public List<Issue> searchAllIssues(String keyword) {
        if (keyword == null || keyword.isBlank()) return issueRepository.findAll();
        return issueRepository.searchByKeyword(keyword);
    }

    public List<Issue> searchIssuesForReporter(String keyword, Long reporterId) {
        if (keyword == null || keyword.isBlank()) return issueRepository.findByReporterId(reporterId);
        return issueRepository.searchByKeywordForReporter(keyword, reporterId);
    }

    public List<Issue> searchIssuesForDeveloper(String keyword, Long developerId) {
        if (keyword == null || keyword.isBlank()) return issueRepository.findByDeveloperId(developerId);
        return issueRepository.searchByKeywordForDeveloper(keyword, developerId);
    }

    public List<Issue> filterByPriority(String priority) {
        return issueRepository.findByPriority(Priority.valueOf(priority.toUpperCase()));
    }

    public List<Issue> filterByStatus(String status) {
        return issueRepository.findByStatus(Status.valueOf(status.toUpperCase()));
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", issueRepository.count());
        stats.put("opened", issueRepository.countByStatus(Status.OPENED));
        stats.put("inProgress", issueRepository.countByStatus(Status.IN_PROGRESS));
        stats.put("resolved", issueRepository.countByStatus(Status.RESOLVED));
        stats.put("closed", issueRepository.countByStatus(Status.CLOSED));
        stats.put("reopened", issueRepository.countByStatus(Status.REOPENED));
        return stats;
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
        return fileName;
    }
}
