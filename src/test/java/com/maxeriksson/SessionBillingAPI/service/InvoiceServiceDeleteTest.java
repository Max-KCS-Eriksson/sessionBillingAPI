package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Unit tests for invoice deletion protection. */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceDeleteTest {

    @Mock private InvoiceRepository invoiceRepository;

    @InjectMocks private InvoiceService invoiceService;

    @Test
    void findAllReturnsInvoices() {
        Invoice invoice = paidInvoice();
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));

        List<Invoice> invoices = invoiceService.findAll();

        assertEquals(1, invoices.size());
        assertEquals(invoice, invoices.get(0));
        verify(invoiceRepository).findAll();
    }

    @Test
    void deleteRemovesPaidInvoice() {
        Invoice invoice = paidInvoice();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        invoiceService.delete(1L);

        verify(invoiceRepository).findById(1L);
        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void deleteThrowsConflictForUnpaidInvoice() {
        Invoice invoice = unpaidInvoice();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> invoiceService.delete(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Unpaid invoices cannot be deleted", exception.getReason());
        verify(invoiceRepository).findById(1L);
        verifyNoMoreInteractions(invoiceRepository);
    }

    @Test
    void deleteThrowsNotFoundWhenInvoiceDoesNotExist() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> invoiceService.delete(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(invoiceRepository).findById(1L);
        verifyNoMoreInteractions(invoiceRepository);
    }

    private Invoice paidInvoice() {
        Booking booking =
                new Booking(
                        new com.maxeriksson.SessionBillingAPI.model.Customer(
                                new com.maxeriksson.SessionBillingAPI.model.PersonalId(
                                        LocalDate.of(1990, 1, 2), 123),
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
        Invoice invoice =
                new Invoice(
                        booking,
                        "INV-000001",
                        InvoiceStatus.PAID,
                        LocalDateTime.of(2026, 1, 1, 12, 0),
                        new BigDecimal("750.00"),
                        "SEK");
        invoice.setId(1L);
        return invoice;
    }

    private Invoice unpaidInvoice() {
        Booking booking =
                new Booking(
                        new com.maxeriksson.SessionBillingAPI.model.Customer(
                                new com.maxeriksson.SessionBillingAPI.model.PersonalId(
                                        LocalDate.of(1990, 1, 2), 123),
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
        Invoice invoice =
                new Invoice(
                        booking,
                        "INV-000001",
                        InvoiceStatus.UNPAID,
                        LocalDateTime.of(2026, 1, 1, 12, 0),
                        new BigDecimal("750.00"),
                        "SEK");
        invoice.setId(1L);
        return invoice;
    }
}
