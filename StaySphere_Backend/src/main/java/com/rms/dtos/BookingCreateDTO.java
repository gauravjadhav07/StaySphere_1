package com.rms.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateDTO {

    @NotNull(message = "Property id is required")
    private Long propertyId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}