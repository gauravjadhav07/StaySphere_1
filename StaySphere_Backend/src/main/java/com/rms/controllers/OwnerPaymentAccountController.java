package com.rms.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rms.dtos.OwnerPaymentAccountRequestDTO;
import com.rms.dtos.OwnerPaymentAccountResponseDTO;
import com.rms.service.OwnerPaymentAccountService;

@RestController
@RequestMapping("/api/owner/payment-account")
@RequiredArgsConstructor
public class OwnerPaymentAccountController {

    private final OwnerPaymentAccountService ownerPaymentAccountService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerPaymentAccountResponseDTO> getMyAccount(Authentication authentication) {
        return ResponseEntity.ok(ownerPaymentAccountService.getMyAccount(authentication.getName()));
    }

    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerPaymentAccountResponseDTO> upsertMyAccount(
            @Valid @RequestBody OwnerPaymentAccountRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(ownerPaymentAccountService.upsertMyAccount(authentication.getName(), dto));
    }
}