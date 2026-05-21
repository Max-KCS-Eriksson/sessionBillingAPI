package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.Invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository for invoice records. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Finds an invoice created for a booking.
     *
     * @param booking completed booking
     * @return matching invoice if it exists
     */
    Optional<Invoice> findByBooking(Booking booking);
}
