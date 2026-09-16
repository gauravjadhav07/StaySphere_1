package com.rms.dtos;

import com.rms.enums.BookingPaymentStatus;
import com.rms.enums.BookingStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingResponseDTO {
    private Long bookingId;
    private Long propertyId;
    private String propertyTitle;
    private Long tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private BigDecimal totalAmount;   
    private BigDecimal depositAmount;
    private BookingStatus bookingStatus;
    private LocalDateTime requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalPayable;      
    private BigDecimal amountPaid;       
    private BigDecimal amountPending;     
    private BookingPaymentStatus paymentStatus; 
    private BigDecimal tokenPaid;         
    private BigDecimal depositPaid;       
    private BigDecimal rentPaid;          
    private BigDecimal remainingDeposit;  
    private BigDecimal tokenAmount;       
    }