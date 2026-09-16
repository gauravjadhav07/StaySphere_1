package com.staysphere.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentOrderRequestDTO {

    @NotNull(message = "Booking id is required")
    private Long bookingId;

    @NotBlank(message = "Transaction reference is required")
    @Size(max = 100)
    private String transactionRef;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Payee name is required")
    @Size(max = 150)
    private String payeeName;

    @Size(max = 100)
    private String payeeUpiId;

    @Size(max = 30)
    private String payeeBankAccountNumber;

    @Size(max = 15)
    private String payeeIfscCode;

}