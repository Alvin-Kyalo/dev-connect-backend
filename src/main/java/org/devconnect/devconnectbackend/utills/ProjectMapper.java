package org.devconnect.devconnectbackend.utills;

import org.devconnect.devconnectbackend.dto.ProjectRequestDTO;
import org.devconnect.devconnectbackend.dto.ProjectResponseDTO;
import org.devconnect.devconnectbackend.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    /**
     * Map ProjectRequestDTO to Project entity
     */
    public Project toEntity(ProjectRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Project project = new Project();
        project.setProjectName(dto.getProjectName());
        project.setDevId(dto.getDevId());
        project.setClientId(dto.getClientId());
        project.setDescription(dto.getDescription());
        project.setProjectBudget(dto.getProjectBudget());
        project.setTimeline(dto.getTimeline());
        project.setStatus(Project.ProjectStatus.PENDING);

        return project;
    }

    /**
     * Map Project entity to ProjectResponseDTO
     */
    public ProjectResponseDTO toResponseDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setProjectId(project.getProjectId());
        dto.setProjectName(project.getProjectName());
        dto.setDevId(project.getDevId());
        dto.setClientId(project.getClientId());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setProjectBudget(project.getProjectBudget());
        dto.setTimeline(project.getTimeline());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        return dto;
    }

    /**
     * Update existing Project entity from ProjectRequestDTO
     */
    public void updateEntityFromDTO(ProjectRequestDTO dto, Project project) {
        if (dto == null || project == null) {
            return;
        }

        if (dto.getProjectName() != null) {
            project.setProjectName(dto.getProjectName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        if (dto.getProjectBudget() != null) {
            project.setProjectBudget(dto.getProjectBudget());
        }
        if (dto.getTimeline() != null) {
            project.setTimeline(dto.getTimeline());
        }
    }
}
