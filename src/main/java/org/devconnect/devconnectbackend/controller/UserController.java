package org.devconnect.devconnectbackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.devconnect.devconnectbackend.dto.LoginDTO;
import org.devconnect.devconnectbackend.dto.LoginResponseDTO;
import org.devconnect.devconnectbackend.dto.PasswordChangeDTO;
import org.devconnect.devconnectbackend.dto.RefreshTokenDTO;
import org.devconnect.devconnectbackend.dto.UserRegistrationDTO;
import org.devconnect.devconnectbackend.dto.UserResponseDTO;
import org.devconnect.devconnectbackend.dto.UserUpdateDTO;
import org.devconnect.devconnectbackend.model.Developer;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.DeveloperRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.devconnect.devconnectbackend.service.JWTService;
import org.devconnect.devconnectbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JWTService jwtService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DeveloperRepository developerRepository;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    // User Registration
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        UserResponseDTO createdUser = userService.registerUser(userRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // User Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(HttpServletRequest request, @Valid @RequestBody LoginDTO loginDTO) {
        // Log login attempt for debugging (do not log passwords in production)
        String origin = request.getHeader("Origin");
        logger.info("Login attempt from origin {} for email={}", origin, loginDTO.getEmail());

        LoginResponseDTO loginResponseDTO = userService.login(loginDTO);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        LoginResponseDTO response = userService.refreshToken(refreshTokenDTO.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Get All Users - must come before /{id} to avoid conflicts
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    // Get User by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        UserResponseDTO userResponseDTO = userService.getUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable User.UserRole role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // Check if email exists
    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> emailExists(@PathVariable String email) {
        return ResponseEntity.ok(userService.isEmailExists(email));
    }

    // Get User by ID - must come after specific routes
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        UserResponseDTO userResponseDTO = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }

    // Update User
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    // Update user status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Integer id, @RequestParam User.UserStatus status) {
        userService.updateUserStatus(id, status);
        return ResponseEntity.ok().build();
    }

    // Update last seen
    @PatchMapping("/{id}/last-seen")
    public ResponseEntity<Void> updateLastSeen(@PathVariable Integer id) {
        userService.updateLastSeen(id);
        return ResponseEntity.ok().build();
    }

    // Delete User
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Change password
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id, @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(id, passwordChangeDTO);
        return ResponseEntity.ok().build();
    }

    // Activate user
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Integer id) {
        userService.activateUserAccount(id);
        return ResponseEntity.ok().build();
    }

    // Deactivate user
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Integer id) {
        userService.deactivateUserAccount(id);
        return ResponseEntity.ok().build();
    }
    
    // Get current user's developer profile
    @GetMapping("/me/developer")
    public ResponseEntity<?> getCurrentDeveloperProfile(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            String email = jwtService.extractEmail(jwt);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Developer developer = developerRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Developer profile not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("developerId", developer.getDeveloperId());
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("username", developer.getUsername());
            response.put("bio", developer.getBio());
            response.put("skills", developer.getSkills());
            response.put("githubUrl", developer.getGithubUrl());
            response.put("linkedinUrl", developer.getLinkedinUrl());
            response.put("portfolioUrl", developer.getPortfolioUrl());
            response.put("hourlyRate", developer.getHourlyRate());
            response.put("averageRating", developer.getAverageRating());
            response.put("totalProjectsCompleted", developer.getTotalProjectsCompleted());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
