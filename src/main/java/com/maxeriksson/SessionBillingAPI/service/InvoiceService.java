package com.maxeriksson.SessionBillingAPI.service;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.domain.Invoice;
import com.maxeriksson.SessionBillingAPI.domain.InvoiceStatus;
import com.maxeriksson.SessionBillingAPI.repository.InvoiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service-layer entry point for invoice workflows.
 *
 * <p>The class creates invoices from completed bookings and enforces the business rule that unpaid
 * invoices cannot be deleted.
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
     * Lists all stored invoices.
     *
     * @return all invoices currently persisted
     */
    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    /**
     * Looks up one invoice by id.
     *
     * @param id invoice identifier
     * @return matching invoice if it exists
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
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

    /**
     * Deletes an invoice when it is not unpaid.
     *
     * @param id invoice identifier
     */
    public void delete(Long id) {
        Invoice existingInvoice =
                invoiceRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (existingInvoice.getStatus() == InvoiceStatus.UNPAID) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Unpaid invoices cannot be deleted");
        }

        invoiceRepository.delete(existingInvoice);
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
