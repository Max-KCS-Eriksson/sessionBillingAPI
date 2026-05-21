package com.maxeriksson.SessionBillingAPI.service;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.domain.Invoice;
import com.maxeriksson.SessionBillingAPI.domain.InvoiceStatus;
import com.maxeriksson.SessionBillingAPI.repository.InvoiceRepository;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Service-layer entry point for invoice generation.
 *
 * <p>The class creates one invoice for each completed booking and prevents duplicate invoices for
 * the same booking.
 */
@org.springframework.stereotype.Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Creates an invoice service backed by the existing repository.
     *
     * @param invoiceRepository persistence boundary for invoice records
     */
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Generates an invoice for a completed booking when one does not already exist.
     *
     * @param booking completed booking
     * @return existing or newly created invoice
     */
    public Invoice generateForCompletedBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed bookings can generate invoices");
        }

        return invoiceRepository
                .findByBooking(booking)
                .orElseGet(
                        () -> {
                            String invoiceNumber = buildInvoiceNumber(booking);
                            Invoice invoice =
                                    new Invoice(
                                            booking,
                                            invoiceNumber,
                                            InvoiceStatus.UNPAID,
                                            LocalDateTime.now(),
                                            calculateTotalAmount(booking),
                                            booking
                                                    .getSessionTypeVersion()
                                                    .getServiceOfferingVersion()
                                                    .getCurrencyCode());
                            return invoiceRepository.save(invoice);
                        });
    }

    private BigDecimal calculateTotalAmount(Booking booking) {
        BigDecimal hourlyCharge =
                booking.getSessionTypeVersion().getServiceOfferingVersion().getHourlyChargeAmount();
        BigDecimal durationFactor =
                BigDecimal.valueOf(booking.getSessionTypeVersion().getDurationMinutes())
                        .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return hourlyCharge.multiply(durationFactor).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildInvoiceNumber(Booking booking) {
        if (booking.getId() != null) {
            return "INV-" + String.format("%06d", booking.getId());
        }
        return "INV-" + booking.getCustomer().getPersonalId() + "-" + booking.getBookedTime();
    }
}
