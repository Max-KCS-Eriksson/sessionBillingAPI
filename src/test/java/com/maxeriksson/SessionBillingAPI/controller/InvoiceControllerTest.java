package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.domain.Invoice;
import com.maxeriksson.SessionBillingAPI.domain.InvoiceStatus;
import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.service.InvoiceService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Unit tests for the invoice REST controller. */
@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private InvoiceService invoiceService;

    @Test
    void findAllReturnsInvoices() throws Exception {
        when(invoiceService.findAll()).thenReturn(List.of(paidInvoice()));

        mockMvc.perform(get("/invoices")).andExpect(status().isOk());

        verify(invoiceService).findAll();
    }

    @Test
    void deleteReturnsNoContentWhenInvoiceCanBeDeleted() throws Exception {
        mockMvc.perform(delete("/invoices/1")).andExpect(status().isNoContent());

        verify(invoiceService).delete(1L);
    }

    @Test
    void deleteReturnsConflictWhenInvoiceIsUnpaid() throws Exception {
        org.springframework.web.server.ResponseStatusException exception =
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "Unpaid invoices cannot be deleted");
        org.mockito.Mockito.doThrow(exception).when(invoiceService).delete(1L);

        mockMvc.perform(delete("/invoices/1")).andExpect(status().isConflict());

        verify(invoiceService).delete(1L);
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
}
