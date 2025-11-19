package org.devconnect.devconnectbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;

    @NotBlank(message = "First name is required")
    @Size(max = 127, message = "First name must not exceed 127 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 127, message = "Last name must not exceed 127 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 15, message = "Telephone must not exceed 15 characters")
    private String telephone;
}
