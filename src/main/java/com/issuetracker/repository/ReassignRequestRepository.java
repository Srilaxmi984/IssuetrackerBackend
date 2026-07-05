package com.issuetracker.repository;

import com.issuetracker.model.ReassignRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReassignRequestRepository extends JpaRepository<ReassignRequest, Long> {

    List<ReassignRequest> findByDeveloperId(Long developerId);

    @Query("SELECT r FROM ReassignRequest r WHERE r.issue.project.manager.id = :managerId ORDER BY r.createdDate DESC")
    List<ReassignRequest> findPendingForManager(@Param("managerId") Long managerId);

    @Query("SELECT r FROM ReassignRequest r WHERE r.issue.project.manager.id = :managerId ORDER BY r.createdDate DESC")
    List<ReassignRequest> findAllForManager(@Param("managerId") Long managerId);

    boolean existsByIssue_IdAndDeveloper_IdAndStatus(Long issueId, Long developerId, ReassignRequest.RequestStatus status);
}
