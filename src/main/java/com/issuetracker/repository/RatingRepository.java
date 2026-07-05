package com.issuetracker.repository;

import com.issuetracker.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByDeveloperId(Long developerId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.developer.id = :developerId")
    Double getAverageScoreByDeveloperId(@Param("developerId") Long developerId);

    boolean existsByIssueIdAndReporterId(Long issueId, Long reporterId);
}
