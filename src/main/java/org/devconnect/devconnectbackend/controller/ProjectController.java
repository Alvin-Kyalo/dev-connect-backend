package org.devconnect.devconnectbackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devconnect.devconnectbackend.dto.ProjectRequestDTO;
import org.devconnect.devconnectbackend.dto.ProjectResponseDTO;
import org.devconnect.devconnectbackend.model.Developer;
import org.devconnect.devconnectbackend.model.Project;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.DeveloperRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.devconnect.devconnectbackend.service.JWTService;
import org.devconnect.devconnectbackend.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final DeveloperRepository developerRepository;

    // Create a new project
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectRequestDTO requestDTO) {
        try {
            System.out.println("Creating project with data: " + requestDTO);
            ProjectResponseDTO response = projectService.addProject(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("Error creating project: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Update an existing project
    @PutMapping("/update/{projectId}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectRequestDTO requestDTO) {
        ProjectResponseDTO response = projectService.updateProject(projectId, requestDTO);
        return ResponseEntity.ok(response);
    }

    // Delete a project
    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Project deleted successfully");
        return ResponseEntity.ok(response);
    }

    // Mark project as completed
    @PatchMapping("/{projectId}/complete")
    public ResponseEntity<ProjectResponseDTO> markProjectAsCompleted(@PathVariable Long projectId) {
        ProjectResponseDTO response = projectService.markProjectAsCompleted(projectId);
        return ResponseEntity.ok(response);
    }

    // Update project status (PATCH with query param)
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ProjectResponseDTO> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestParam Project.ProjectStatus status) {
        ProjectResponseDTO response = projectService.updateProjectStatus(projectId, status);
        return ResponseEntity.ok(response);
    }
    
    // Update project status (PUT with request body)
    @PutMapping("/{projectId}/status")
    public ResponseEntity<ProjectResponseDTO> updateProjectStatusWithBody(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String statusStr = requestBody.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                throw new RuntimeException("Status is required");
            }
            
            Project.ProjectStatus status = Project.ProjectStatus.valueOf(statusStr.toUpperCase());
            ProjectResponseDTO response = projectService.updateProjectStatus(projectId, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value. Valid values are: PENDING, IN_PROGRESS, COMPLETED");
        }
    }

    // Get project by ID
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long projectId) {
        ProjectResponseDTO response = projectService.getProjectById(projectId);
        return ResponseEntity.ok(response);
    }

    // Get all projects
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    // Get projects for the authenticated developer (must come before /{devId} to avoid path conflict)
    @GetMapping("/my-developer-projects")
    public ResponseEntity<?> getMyProjects(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println(">>> HIT MY-DEVELOPER-PROJECTS ENDPOINT");
            
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Authorization header is missing");
            }
            
            String jwt = token.substring(7); // Remove "Bearer " prefix
            String email = jwtService.extractEmail(jwt);
            
            System.out.println(">>> Extracted email: " + email);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            System.out.println(">>> Found user ID: " + user.getUserId());
            
            Developer developer = developerRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Developer profile not found"));
            
            System.out.println(">>> Getting projects for authenticated developer ID: " + developer.getDeveloperId());
            
            List<ProjectResponseDTO> projects = projectService.getProjectsByDevId(developer.getDeveloperId().longValue());
            
            System.out.println(">>> Returning " + projects.size() + " projects for authenticated developer");
            
            return ResponseEntity.ok(projects);
        } catch (RuntimeException e) {
            System.err.println(">>> ERROR in my-developer-projects: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Get projects by developer ID - handle both numeric IDs and special keywords
    @GetMapping("/developer/{devId}")
    public ResponseEntity<?> getProjectsByDevId(
            @PathVariable String devId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        System.out.println(">>> ProjectController: Getting projects for developer ID: " + devId);
        
        // If devId is "my-developer-projects" or "my-projects", get authenticated user's projects
        if ("my-developer-projects".equals(devId) || "my-projects".equals(devId)) {
            try {
                if (token == null || token.isEmpty()) {
                    throw new RuntimeException("Authorization header is missing");
                }
                
                String jwt = token.substring(7);
                String email = jwtService.extractEmail(jwt);
                
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                Developer developer = developerRepository.findByUserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Developer profile not found"));
                
                System.out.println(">>> Getting projects for authenticated developer ID: " + developer.getDeveloperId());
                
                List<ProjectResponseDTO> projects = projectService.getProjectsByDevId(developer.getDeveloperId().longValue());
                
                System.out.println(">>> Returning " + projects.size() + " projects");
                
                return ResponseEntity.ok(projects);
            } catch (RuntimeException e) {
                System.err.println(">>> ERROR: " + e.getMessage());
                Map<String, String> error = new HashMap<>();
                error.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
        }
        
        // Otherwise, parse as numeric developer ID
        try {
            Long developerId = Long.parseLong(devId);
            List<ProjectResponseDTO> projects = projectService.getProjectsByDevId(developerId);
            System.out.println(">>> ProjectController: Returning " + projects.size() + " projects");
            return ResponseEntity.ok(projects);
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid developer ID: " + devId);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get projects by client ID
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByClientId(@PathVariable Long clientId) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByClientId(clientId);
        return ResponseEntity.ok(projects);
    }

    // Get projects by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByStatus(@PathVariable Project.ProjectStatus status) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }

    // Get pending projects (for marketplace)
    @GetMapping("/pending")
    public ResponseEntity<List<ProjectResponseDTO>> getPendingProjects() {
        List<ProjectResponseDTO> projects = projectService.getProjectsByStatus(Project.ProjectStatus.PENDING);
        return ResponseEntity.ok(projects);
    }

    // Get all projects (alias for marketplace)
    @GetMapping("/all")
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjectsForMarketplace() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    // Claim a project (assign developer to project)
    @PostMapping("/{projectId}/claim")
    public ResponseEntity<?> claimProject(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String token) {
        try {
            // Extract email from JWT token
            String jwt = token.substring(7); // Remove "Bearer " prefix
            String email = jwtService.extractEmail(jwt);
            
            // Get user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            System.out.println(">>> Claim Project - User ID: " + user.getUserId() + ", Email: " + email);
            
            // Get developer ID from the developers table
            Developer developer = developerRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Developer profile not found"));
            
            System.out.println(">>> Claim Project - Developer ID: " + developer.getDeveloperId());
            
            ProjectResponseDTO response = projectService.claimProject(projectId, developer.getDeveloperId().longValue());
            
            System.out.println(">>> Claim Project - Successfully claimed project " + projectId + " for developer " + developer.getDeveloperId());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
