package org.devconnect.devconnectbackend.repository;

import org.devconnect.devconnectbackend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDevId(Long devId);
    List<Project> findByClientId(Long clientId);
    List<Project> findByStatus(Project.ProjectStatus status);
    List<Project> findByDevIdAndStatus(Long devId, Project.ProjectStatus status);
    List<Project> findByClientIdAndStatus(Long clientId, Project.ProjectStatus status);
}
