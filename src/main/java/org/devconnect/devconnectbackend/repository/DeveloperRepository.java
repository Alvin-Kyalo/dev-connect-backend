package org.devconnect.devconnectbackend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.devconnect.devconnectbackend.model.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Integer> {

    Optional<Developer> findByUserId(Integer userId);

    List<Developer> findByHourlyRateGreaterThanEqual(BigDecimal hourlyRate);

    List<Developer> findByHourlyRateLessThanEqual(BigDecimal hourlyRate);

    List<Developer> findByAverageRatingGreaterThanEqual(BigDecimal averageRating);

    List<Developer> findByAverageRatingLessThanEqual(BigDecimal averageRating);

    List<Developer> findByTotalProjectsCompletedGreaterThanEqual(Integer totalProjectsCompleted);

    List<Developer> findByTotalProjectsCompletedLessThanEqual(Integer totalProjectsCompleted);

    @Query("""
        SELECT d.developerId as id,
               d.userId as userId,
               d.username as username,
               u.email as email,
               d.bio as bio,
               COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as completedProjects,
               COALESCE(AVG(r.rating), 0.0) as averageRating
        FROM Developer d
        LEFT JOIN User u ON d.userId = u.userId
        LEFT JOIN Project p ON d.developerId = p.devId
        LEFT JOIN Rating r ON d.developerId = r.developerId
        GROUP BY d.developerId, d.userId, d.username, u.email, d.bio
        """)
    List<Map<String, Object>> findAllDevelopersWithStats();
}
