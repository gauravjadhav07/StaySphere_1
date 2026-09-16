package com.rms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rms.entity.Booking;
import com.rms.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    List<Booking> findAllByTenant_UserId(Long tenantId);

    List<Booking> findAllByProperty_PropertyId(Long propertyId);

    @Query(value = "SELECT b FROM Booking b LEFT JOIN FETCH b.property LEFT JOIN FETCH b.tenant",
           countQuery = "SELECT count(b) FROM Booking b")
    Page<Booking> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"property", "tenant"})
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.property.propertyId = :propertyId " +
           "AND b.bookingStatus IN :activeStatuses " +
           "AND (b.endDate IS NULL OR b.endDate >= :startDate) " +
           "AND (:endDate IS NULL OR b.startDate <= :endDate)")
    List<Booking> findOverlappingBookings(@Param("propertyId") Long propertyId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("activeStatuses") List<BookingStatus> activeStatuses);
}