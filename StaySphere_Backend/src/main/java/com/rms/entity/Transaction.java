package com.rms.entity;

import com.rms.enums.PaymentSource;
import com.rms.enums.PaymentStatus;
import com.rms.enums.PaymentType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 100)
    private String transactionRef;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source", nullable = false, length = 20)
    private PaymentSource paymentSource = PaymentSource.ONLINE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_owner_id")
    private User recordedByOwner;

    @Column(name = "notes", length = 255)
    private String notes;
}