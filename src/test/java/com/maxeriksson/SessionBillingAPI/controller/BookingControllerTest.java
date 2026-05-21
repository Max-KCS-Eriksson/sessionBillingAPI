package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.service.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Unit tests for booking creation and status transitions REST endpoints. */
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BookingService bookingService;

    @Test
    void findAllReturnsBookings() throws Exception {
        when(bookingService.findAll())
                .thenReturn(
                        List.of(
                                new Booking(
                                        sampleCustomer(),
                                        sampleSessionTypeVersion(),
                                        LocalDateTime.of(2026, 1, 1, 10, 0),
                                        BookingStatus.BOOKED)));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("BOOKED"));

        verify(bookingService).findAll();
    }

    @Test
    void createReturnsCreatedBookingWhenCustomerAndSessionTypeExist() throws Exception {
        Booking createdBooking =
                new Booking(
                        sampleCustomer(),
                        sampleSessionTypeVersion(),
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        BookingStatus.BOOKED);
        when(bookingService.create(any(BookingService.BookingCreateRequest.class)))
                .thenReturn(createdBooking);

        mockMvc.perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerPersonalId\":\"19900102-0123\",\"sessionTypeName\":\"GroupSession\",\"bookedTime\":\"2026-01-01T10:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"));

        verify(bookingService).create(any(BookingService.BookingCreateRequest.class));
    }

    @Test
    void createReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(bookingService)
                .create(any(BookingService.BookingCreateRequest.class));

        mockMvc.perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerPersonalId\":\"19900102-0123\",\"sessionTypeName\":\"GroupSession\",\"bookedTime\":\"2026-01-01T10:00:00\"}"))
                .andExpect(status().isNotFound());

        verify(bookingService).create(any(BookingService.BookingCreateRequest.class));
    }

    @Test
    void updateStatusReturnsOkWhenBookingExists() throws Exception {
        Booking updatedBooking =
                new Booking(
                        sampleCustomer(),
                        sampleSessionTypeVersion(),
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        BookingStatus.COMPLETED);
        when(bookingService.updateStatus(1L, new BookingService.BookingStatusRequest(BookingStatus.COMPLETED)))
                .thenReturn(updatedBooking);

        mockMvc.perform(
                        patch("/bookings/1/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(bookingService)
                .updateStatus(1L, new BookingService.BookingStatusRequest(BookingStatus.COMPLETED));
    }

    @Test
    void updateStatusReturnsNotFoundWhenBookingDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(bookingService)
                .updateStatus(1L, new BookingService.BookingStatusRequest(BookingStatus.CANCELLED));

        mockMvc.perform(
                        patch("/bookings/1/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isNotFound());

        verify(bookingService)
                .updateStatus(1L, new BookingService.BookingStatusRequest(BookingStatus.CANCELLED));
    }

    private Customer sampleCustomer() {
        return new Customer(
                new com.maxeriksson.SessionBillingAPI.model.PersonalId(LocalDate.of(1990, 1, 2), 123),
                "Ada",
                "Lovelace",
                "Example Street");
    }

    private SessionTypeVersion sampleSessionTypeVersion() {
        return new SessionTypeVersion(
                new SessionType("GroupSession"),
                new ServiceOfferingVersion(
                        new ServiceOffering("Coaching"),
                        1,
                        new BigDecimal("500.00"),
                        "SEK",
                        true),
                1,
                90,
                true);
    }
}
