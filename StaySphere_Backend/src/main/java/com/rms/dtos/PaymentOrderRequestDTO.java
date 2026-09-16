package com.rms.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentOrderRequestDTO {
    private Long bookingId;
    private String transactionRef;
    private BigDecimal amount;
    private String payeeName;
    private String payeeUpiId;
    private String payeeBankAccountNumber;
    private String payeeIfscCode;
}