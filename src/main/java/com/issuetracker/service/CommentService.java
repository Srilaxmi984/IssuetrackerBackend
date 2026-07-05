package com.issuetracker.service;

import com.issuetracker.dto.CommentRequest;
import com.issuetracker.dto.CommentResponse;
import com.issuetracker.exception.BadRequestException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.Comment;
import com.issuetracker.model.Issue;
import com.issuetracker.repository.CommentRepository;
import com.issuetracker.repository.IssueRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;

    public CommentService(CommentRepository commentRepository, IssueRepository issueRepository) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
    }

    public CommentResponse addComment(Long issueId, CommentRequest request) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new BadRequestException("Comment message cannot be empty");
        }

        Comment comment = new Comment();
        comment.setMessage(request.getMessage().trim());
        comment.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy().trim() : "Unknown");
        comment.setIssue(issue);

        Comment saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    public List<CommentResponse> getCommentsByIssue(Long issueId) {
        return commentRepository.findByIssueIdOrderByCreatedDateAsc(issueId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(
            c.getId(),
            c.getMessage(),
            c.getCreatedBy(),
            c.getCreatedDate(),
            c.getIssue() != null ? c.getIssue().getId() : null
        );
    }
}
