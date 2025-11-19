package org.devconnect.devconnectbackend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.devconnect.devconnectbackend.dto.ProjectRequestDTO;
import org.devconnect.devconnectbackend.dto.ProjectResponseDTO;
import org.devconnect.devconnectbackend.model.Project;
import org.devconnect.devconnectbackend.repository.ProjectRepository;
import org.devconnect.devconnectbackend.utills.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    // Add a new project
    @Transactional
    public ProjectResponseDTO addProject(ProjectRequestDTO requestDTO) {
        Project project = projectMapper.toEntity(requestDTO);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(savedProject);
    }

    // Update an existing project
    @Transactional
    public ProjectResponseDTO updateProject(Long projectId, ProjectRequestDTO requestDTO) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        projectMapper.updateEntityFromDTO(requestDTO, project);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }

    // Delete a project
    @Transactional
    public void deleteProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }
        projectRepository.deleteById(projectId);
    }

    // Mark project as completed
    @Transactional
    public ProjectResponseDTO markProjectAsCompleted(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        project.setStatus(Project.ProjectStatus.COMPLETED);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }

    // Update project status
    @Transactional
    public ProjectResponseDTO updateProjectStatus(Long projectId, Project.ProjectStatus status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }

    // Get project by ID
    public ProjectResponseDTO getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        return projectMapper.toResponseDTO(project);
    }

    // Get all projects
    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get projects by developer ID
    public List<ProjectResponseDTO> getProjectsByDevId(Long devId) {
        System.out.println("========================================");
        System.out.println("Fetching projects for developer ID: " + devId);
        List<Project> projects = projectRepository.findByDevId(devId);
        System.out.println("Found " + projects.size() + " projects for developer ID: " + devId);
        for (Project p : projects) {
            System.out.println("Project: " + p.getProjectId() + ", Name: " + p.getProjectName() + ", DevId: " + p.getDevId() + ", Status: " + p.getStatus());
        }
        
        // Let's also check what projects exist for developer ID 21
        if (devId == 64) {
            System.out.println("--- CHECKING FOR DEVELOPER ID 21 (the actual developer ID) ---");
            List<Project> dev21Projects = projectRepository.findByDevId(21L);
            System.out.println("Found " + dev21Projects.size() + " projects for developer ID 21:");
            for (Project p : dev21Projects) {
                System.out.println("Project: " + p.getProjectId() + ", Name: " + p.getProjectName() + ", DevId: " + p.getDevId() + ", Status: " + p.getStatus());
            }
        }
        
        System.out.println("========================================");
        return projects.stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get projects by developer ID
    public List<ProjectResponseDTO> getProjectsByClientId(Long clientId) {
        return projectRepository.findByClientId(clientId).stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get projects by status
    public List<ProjectResponseDTO> getProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Claim a project (assign developer to pending project)
    @Transactional
    public ProjectResponseDTO claimProject(Long projectId, Long developerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        // Check if project is still pending
        if (project.getStatus() != Project.ProjectStatus.PENDING) {
            throw new RuntimeException("Project is not available for claiming. Current status: " + project.getStatus());
        }

        // Check if project already has a developer assigned
        if (project.getDevId() != null) {
            throw new RuntimeException("Project has already been claimed by another developer");
        }

        // Assign developer and update status
        project.setDevId(developerId);
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }
}
