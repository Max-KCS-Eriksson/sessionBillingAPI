package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.service.BookingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** REST controller exposing booking creation and status transitions. */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Creates a booking controller backed by the service layer.
     *
     * @param bookingService service-layer boundary for bookings
     */
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Lists all registered bookings.
     *
     * @return all booking records currently persisted
     */
    @GetMapping
    public List<Booking> findAll() {
        return bookingService.findAll();
    }

    /**
     * Creates a booking in the BOOKED state.
     *
     * @param request booking payload
     * @return the created booking
     */
    @PostMapping
    public ResponseEntity<Booking> create(@RequestBody BookingCreateRequest request) {
        Booking createdBooking =
                bookingService.create(
                        new BookingService.BookingCreateRequest(
                                request.customerPersonalId(),
                                request.sessionTypeName(),
                                request.bookedTime()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    /**
     * Updates the status of an existing booking.
     *
     * @param id booking identifier from the request path
     * @param request booking status payload
     * @return the updated booking
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Booking> updateStatus(
            @PathVariable Long id, @RequestBody BookingStatusRequest request) {
        Booking updatedBooking =
                bookingService.updateStatus(id, new BookingService.BookingStatusRequest(request.status()));
        return ResponseEntity.ok(updatedBooking);
    }

    /**
     * Request payload for creating a booking.
     *
     * @param customerPersonalId customer identifier from the request path
     * @param sessionTypeName session type name used to resolve the current session type version
     * @param bookedTime booking time
     */
    public record BookingCreateRequest(
            String customerPersonalId, String sessionTypeName, LocalDateTime bookedTime) {}

    /**
     * Request payload for updating a booking status.
     *
     * @param status booking lifecycle status
     */
    public record BookingStatusRequest(BookingStatus status) {}
}
