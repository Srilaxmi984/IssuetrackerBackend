package com.issuetracker.service;

import com.issuetracker.dto.RatingRequest;
import com.issuetracker.exception.BadRequestException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.Issue;
import com.issuetracker.model.Rating;
import com.issuetracker.model.User;
import com.issuetracker.repository.IssueRepository;
import com.issuetracker.repository.RatingRepository;
import com.issuetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public RatingService(RatingRepository ratingRepository,
                         IssueRepository issueRepository,
                         UserRepository userRepository,
                         NotificationService notificationService) {
        this.ratingRepository = ratingRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Rating addRating(Long issueId, RatingRequest request) {
        if (ratingRepository.existsByIssueIdAndReporterId(issueId, request.getReporterId())) {
            throw new BadRequestException("You have already rated this issue");
        }
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
        User reporter = userRepository.findById(request.getReporterId())
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));
        User developer = userRepository.findById(request.getDeveloperId())
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found"));

        Rating rating = new Rating();
        rating.setScore(request.getScore());
        rating.setFeedback(request.getFeedback());
        rating.setIssue(issue);
        rating.setReporter(reporter);
        rating.setDeveloper(developer);
        Rating saved = ratingRepository.save(rating);

        // Notify the developer about the rating
        String stars = "⭐".repeat(request.getScore());
        String feedbackNote = (request.getFeedback() != null && !request.getFeedback().isBlank())
                ? " | Feedback: \"" + request.getFeedback() + "\""
                : "";
        notificationService.createNotification(
            stars + " You received a " + request.getScore() + "/5 rating from " + reporter.getUsername() +
            " for issue \"" + issue.getTitle() + "\"" + feedbackNote,
            developer, issue);

        return saved;
    }

    public List<Rating> getRatingsByDeveloper(Long developerId) {
        return ratingRepository.findByDeveloperId(developerId);
    }

    public Double getAverageScore(Long developerId) {
        Double avg = ratingRepository.getAverageScoreByDeveloperId(developerId);
        return avg != null ? avg : 0.0;
    }
}
