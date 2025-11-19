package org.devconnect.devconnectbackend.repository;

import java.util.List;

import org.devconnect.devconnectbackend.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByDeveloperId(Long developerId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.developerId = :developerId")
    Double getAverageRatingByDeveloperId(Long developerId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.developerId = :developerId")
    Long countByDeveloperId(Long developerId);
}
