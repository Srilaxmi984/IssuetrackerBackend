package com.issuetracker.repository;

import com.issuetracker.model.Issue;
import com.issuetracker.enums.Priority;
import com.issuetracker.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByReporterId(Long reporterId);

    @Query("SELECT DISTINCT i FROM Issue i JOIN i.developers d WHERE d.id = :developerId")
    List<Issue> findByDeveloperId(@Param("developerId") Long developerId);

    @Query("SELECT DISTINCT i FROM Issue i LEFT JOIN i.developers d WHERE i.project.id = :projectId")
    List<Issue> findByProjectId(@Param("projectId") Long projectId);

    List<Issue> findByStatus(Status status);
    List<Issue> findByPriority(Priority priority);

    @Query("SELECT DISTINCT i FROM Issue i " +
           "LEFT JOIN i.developers d " +
           "LEFT JOIN i.project p " +
           "WHERE LOWER(i.title) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(COALESCE(i.description,'')) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(COALESCE(d.username,'')) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(COALESCE(p.name,'')) LIKE LOWER(CONCAT('%',:kw,'%'))")
    List<Issue> searchByKeyword(@Param("kw") String keyword);

    @Query("SELECT DISTINCT i FROM Issue i " +
           "LEFT JOIN i.developers d " +
           "LEFT JOIN i.project p " +
           "WHERE i.reporter.id = :reporterId " +
           "AND (LOWER(i.title) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(COALESCE(d.username,'')) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(COALESCE(p.name,'')) LIKE LOWER(CONCAT('%',:kw,'%')))")
    List<Issue> searchByKeywordForReporter(@Param("kw") String keyword, @Param("reporterId") Long reporterId);

    @Query("SELECT DISTINCT i FROM Issue i " +
           "JOIN i.developers d2 " +
           "LEFT JOIN i.developers d " +
           "LEFT JOIN i.project p " +
           "WHERE d2.id = :developerId " +
           "AND (LOWER(i.title) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(COALESCE(d.username,'')) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(COALESCE(p.name,'')) LIKE LOWER(CONCAT('%',:kw,'%')))")
    List<Issue> searchByKeywordForDeveloper(@Param("kw") String keyword, @Param("developerId") Long developerId);

    long countByStatus(Status status);

    @Query("SELECT i FROM Issue i WHERE i.developers IS EMPTY")
    List<Issue> findUnassignedIssues();
}
