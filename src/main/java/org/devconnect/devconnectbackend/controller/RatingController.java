package org.devconnect.devconnectbackend.controller;

import java.util.List;
import java.util.Map;

import org.devconnect.devconnectbackend.model.Rating;
import org.devconnect.devconnectbackend.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/create")
    public ResponseEntity<Rating> createRating(@RequestBody Map<String, Object> request) {
        Long clientId = Long.parseLong(request.get("clientId").toString());
        Long developerId = Long.parseLong(request.get("developerId").toString());
        Integer rating = Integer.parseInt(request.get("rating").toString());
        String comment = request.get("comment") != null ? request.get("comment").toString() : null;

        Rating createdRating = ratingService.createRating(clientId, developerId, rating, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRating);
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<List<Rating>> getRatingsByDeveloper(@PathVariable Long developerId) {
        List<Rating> ratings = ratingService.getRatingsByDeveloperId(developerId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/developer/{developerId}/average")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long developerId) {
        Map<String, Object> averageData = ratingService.getAverageRating(developerId);
        return ResponseEntity.ok(averageData);
    }
}
