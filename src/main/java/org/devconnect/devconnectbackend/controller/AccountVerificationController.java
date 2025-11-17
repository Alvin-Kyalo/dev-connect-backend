package org.devconnect.devconnectbackend.controller;

import jakarta.validation.Valid;
import org.devconnect.devconnectbackend.dto.VerifyCodeDTO;
import org.devconnect.devconnectbackend.service.AccountVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account-verification")
public class AccountVerificationController {

    @Autowired
    AccountVerificationService accountVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@Valid @RequestBody VerifyCodeDTO verifyCodeDTO) {
        accountVerificationService.verifyAccount(verifyCodeDTO.getEmail(), verifyCodeDTO.getVerificationCode());
        return ResponseEntity.ok("Account verified successfully.");
    }

    @PostMapping("/resend-code")
    public ResponseEntity<String> resendVerificationCode(@Valid @RequestBody VerifyCodeDTO verifyCodeDTO) {
        accountVerificationService.resendVerificationCode(verifyCodeDTO.getEmail());
        return ResponseEntity.ok("Verification code resent successfully.");
    }
}
