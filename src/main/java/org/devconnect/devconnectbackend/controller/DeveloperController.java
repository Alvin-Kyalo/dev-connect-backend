package org.devconnect.devconnectbackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.devconnect.devconnectbackend.model.Developer;
import org.devconnect.devconnectbackend.repository.DeveloperRepository;
import org.devconnect.devconnectbackend.repository.ProjectRepository;
import org.devconnect.devconnectbackend.repository.RatingRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperRepository developerRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RatingRepository ratingRepository;

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllDevelopers() {
        List<Developer> developers = developerRepository.findAll();

        List<Map<String, Object>> response = developers.stream().map(dev -> {
            Map<String, Object> devMap = new HashMap<>();
            devMap.put("id", dev.getDeveloperId());
            devMap.put("userId", dev.getUserId());
            devMap.put("username", dev.getUsername());
            devMap.put("bio", dev.getBio());
            devMap.put("skills", dev.getSkills());
            devMap.put("hourlyRate", dev.getHourlyRate());
            devMap.put("githubUrl", dev.getGithubUrl());
            devMap.put("linkedinUrl", dev.getLinkedinUrl());
            devMap.put("portfolioUrl", dev.getPortfolioUrl());
            devMap.put("rating", dev.getAverageRating());
            devMap.put("projectsCount", dev.getTotalProjectsCompleted());

            // Get user email
            userRepository.findById(dev.getUserId()).ifPresent(user -> {
                devMap.put("email", user.getEmail());
            });

            return devMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-with-stats")
    public ResponseEntity<List<Map<String, Object>>> getAllDevelopersWithStats() {
        // Single optimized query with JOINs - no N+1 problem
        List<Map<String, Object>> developers = developerRepository.findAllDevelopersWithStats();
        return ResponseEntity.ok(developers);
    }
}
