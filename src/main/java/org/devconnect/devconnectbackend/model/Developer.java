package org.devconnect.devconnectbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "developers")
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "developer_seq_gen")
    @SequenceGenerator(name = "developer_seq_gen", sequenceName = "developer_seq", allocationSize = 1)
    @Column(name = "developer_id")
    private Integer developerId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name="username", nullable = true, unique = true, length = 50)
    private String username;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "skills", nullable = true, columnDefinition = "TEXT")
    private  String skills;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Pattern(regexp = "^https?://.*$", message = "Website must be a valid URL")
    @Column(name = "github_url", nullable = true, length = 500)
    private String githubUrl;

    @Pattern(regexp = "^https?://.*$", message = "Website must be a valid URL")
    @Column(name = "linkedin_url", nullable = true, length = 500)
    private String linkedinUrl;

    @Pattern(regexp = "^https?://.*$", message = "Website must be a valid URL")
    @Column(name = "portfolio_url", nullable = true, length = 500)
    private String portfolioUrl;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_projects_completed")
    private Integer totalProjectsCompleted = 0;
}
