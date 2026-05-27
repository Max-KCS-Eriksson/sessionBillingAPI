package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.domain.Invoice;
import com.maxeriksson.SessionBillingAPI.domain.InvoiceStatus;
import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.repository.InvoiceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/** Unit tests for invoice generation from completed bookings. */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;

    @InjectMocks private InvoiceService invoiceService;

    @Test
    void generateForCompletedBookingCreatesInvoiceWhenMissing() {
        Booking booking = completedBooking();
        when(invoiceRepository.findByBooking(booking)).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Invoice.class));

        Invoice invoice = invoiceService.generateForCompletedBooking(booking);

        assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());
        assertEquals("SEK", invoice.getCurrencyCode());
        assertEquals(new BigDecimal("750.00"), invoice.getTotalAmount());
        assertEquals("INV-000001", invoice.getInvoiceNumber());
        verify(invoiceRepository).findByBooking(booking);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void generateForCompletedBookingReturnsExistingInvoice() {
        Booking booking = completedBooking();
        Invoice existingInvoice =
                new Invoice(
                        booking,
                        "INV-000001",
                        InvoiceStatus.UNPAID,
                        LocalDateTime.of(2026, 1, 1, 12, 0),
                        new BigDecimal("750.00"),
                        "SEK");
        when(invoiceRepository.findByBooking(booking)).thenReturn(Optional.of(existingInvoice));

        Invoice invoice = invoiceService.generateForCompletedBooking(booking);

        assertEquals(existingInvoice, invoice);
        verify(invoiceRepository).findByBooking(booking);
        verifyNoMoreInteractions(invoiceRepository);
    }

    @Test
    void generateForCompletedBookingThrowsWhenBookingIsNotCompleted() {
        Booking booking = completedBooking();
        booking.setStatus(BookingStatus.BOOKED);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> invoiceService.generateForCompletedBooking(booking));

        assertEquals("Only completed bookings can generate invoices", exception.getMessage());
        verifyNoMoreInteractions(invoiceRepository);
    }

    private Booking completedBooking() {
        Booking booking = new Booking(
                new com.maxeriksson.SessionBillingAPI.model.Customer(
                        new com.maxeriksson.SessionBillingAPI.model.PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street"),
                new SessionTypeVersion(
                        new SessionType("GroupSession"),
                        new ServiceOfferingVersion(
                                new ServiceOffering("Coaching"),
                                1,
                                new BigDecimal("500.00"),
                                "SEK",
                                true),
                        1,
                        90,
                        true),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                BookingStatus.COMPLETED);
        booking.setId(1L);
        return booking;
    }
}
