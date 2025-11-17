package org.devconnect.devconnectbackend.repository;

import org.devconnect.devconnectbackend.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByUserId(Integer userId);

    List<Client> findByIndustry(String industry);

    List<Client> findByCompanyNameContainingIgnoreCase(String companyName);

}
