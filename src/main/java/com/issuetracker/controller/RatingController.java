package com.issuetracker.controller;

import com.issuetracker.dto.RatingRequest;
import com.issuetracker.model.Rating;
import com.issuetracker.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/api/issues/{issueId}/rating")
    public ResponseEntity<Rating> addRating(
            @PathVariable Long issueId,
            @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.addRating(issueId, request));
    }

    @GetMapping("/api/ratings/developer/{developerId}")
    public ResponseEntity<List<Rating>> getRatingsByDeveloper(@PathVariable Long developerId) {
        return ResponseEntity.ok(ratingService.getRatingsByDeveloper(developerId));
    }

    @GetMapping("/api/ratings/developer/{developerId}/average")
    public ResponseEntity<Map<String, Object>> getAverageScore(@PathVariable Long developerId) {
        return ResponseEntity.ok(Map.of("average", ratingService.getAverageScore(developerId)));
    }
}
