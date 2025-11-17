package org.devconnect.devconnectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.devconnect.devconnectbackend.model.Project;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDTO {
    private Long projectId;
    private String projectName;
    private Long devId;
    private Long clientId;
    private String description;
    private Project.ProjectStatus status;
    private BigDecimal projectBudget;
    private LocalDateTime timeline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
