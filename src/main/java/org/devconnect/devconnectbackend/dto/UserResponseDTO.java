package org.devconnect.devconnectbackend.dto;

import java.time.LocalDateTime;

import org.devconnect.devconnectbackend.model.User.UserRole;
import org.devconnect.devconnectbackend.model.User.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    private Integer userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private UserRole userRole;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;
    private boolean isActive;
}
