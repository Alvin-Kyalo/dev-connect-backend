package org.devconnect.devconnectbackend.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_sequence")
    @SequenceGenerator(name = "client_sequence", sequenceName = "client_sequence", allocationSize = 1)
    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name="username", nullable = true, unique = true, length = 50)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(length = 127)
    private String industry;

    @Pattern(regexp = "^https?://.*$", message = "Website must be a valid URL")
    @Column(length = 255)
    private String website;
}
