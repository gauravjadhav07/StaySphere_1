package com.staysphere.payment.controller;

import com.staysphere.payment.dto.PaymentOrderRequestDTO;
import com.staysphere.payment.dto.PaymentOrderResponseDTO;
import com.staysphere.payment.dto.PaymentResponseDTO;
import com.staysphere.payment.dto.PaymentVerifyRequestDTO;
import com.staysphere.payment.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponseDTO> createOrder(@Valid @RequestBody PaymentOrderRequestDTO dto) {
        log.info("Creating payment order for booking {} (ref {})", dto.getBookingId(), dto.getTransactionRef());
        return new ResponseEntity<>(paymentService.createOrder(dto), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDTO> verify(@Valid @RequestBody PaymentVerifyRequestDTO dto) {
        return ResponseEntity.ok(paymentService.verifyPayment(dto));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(HttpServletRequest request,
                                         @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) throws Exception {
 
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (var inputStream = request.getInputStream()) {
            inputStream.transferTo(buffer);
        }
        String rawBody = buffer.toString(StandardCharsets.UTF_8);
        paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getById(paymentId));
    }

    @GetMapping("/ref/{transactionRef}")
    public ResponseEntity<PaymentResponseDTO> getByTransactionRef(@PathVariable String transactionRef) {
        return ResponseEntity.ok(paymentService.getByTransactionRef(transactionRef));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponseDTO>> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getByBookingId(bookingId));
    }
}