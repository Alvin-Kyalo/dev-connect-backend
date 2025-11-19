package org.devconnect.devconnectbackend.service;

import lombok.RequiredArgsConstructor;
import org.devconnect.devconnectbackend.model.Rating;
import org.devconnect.devconnectbackend.repository.RatingRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;

    public Rating createRating(Long clientId, Long developerId, Integer rating, String comment) {
        // Validate rating is between 1-5
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Rating newRating = new Rating();
        newRating.setClientId(clientId);
        newRating.setDeveloperId(developerId);
        newRating.setRating(rating);
        newRating.setComment(comment);

        return ratingRepository.save(newRating);
    }

    public List<Rating> getRatingsByDeveloperId(Long developerId) {
        return ratingRepository.findByDeveloperId(developerId);
    }

    public Map<String, Object> getAverageRating(Long developerId) {
        Double average = ratingRepository.getAverageRatingByDeveloperId(developerId);
        Long totalRatings = ratingRepository.countByDeveloperId(developerId);

        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", average != null ? average : 0.0);
        response.put("totalRatings", totalRatings);

        return response;
    }
}
