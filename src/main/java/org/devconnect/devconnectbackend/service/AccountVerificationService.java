package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AccountVerificationService {

    @Lazy
    UserRepository userRepository;

    @Lazy
    EmailService emailService;

    public void verifyAccount(String email, String verificationCode) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Check if the verification code matches
        if (user.isVerified()) {
            throw new RuntimeException("Account is already verified");
        }

        // Check if Code Exists
        if (user.getAuthCode() == null) {
            throw new RuntimeException("No verification code found. Please request a new code.");
        }

        // Check if the code is expired
        if (user.getAuthCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired. Please request a new code.");
        }

        // Check if the code matches
        if (!user.getAuthCode().equals(verificationCode)) {
            throw new RuntimeException("Invalid verification code.");
        }

        // Mark user as verified
        user.setVerified(true);
        user.setAuthCode(null); // Clear the auth code
        user.setAuthCodeExpiry(null); // Clear the auth code expiry
        userRepository.save(user);

        emailService.sendAccountVerificationSuccessEmail(email);
    }

    public void resendVerificationCode(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Check if already verified
        if (user.isVerified()) {
            throw new RuntimeException("Account is already verified");
        }

        // Generate new verification code and expiry
        String newCode = String.format("%06d", new Random().nextInt(999999));
        user.setAuthCode(newCode);
        user.setAuthCodeExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        emailService.sendResendVerificationCodeEmail(email, newCode, 15);
    }
}
