package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.domain.Booking;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for booking records. */
public interface BookingRepository extends JpaRepository<Booking, Long> {}
