package com.maxeriksson.SessionBillingAPI.service;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.repository.BookingRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service-layer entry point for booking workflows.
 *
 * <p>The class owns booking creation and status transitions while resolving the current session
 * type version at booking time.
 */
@org.springframework.stereotype.Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerService customerService;
    private final SessionTypeService sessionTypeService;

    /**
     * Creates a booking service backed by the existing collaborators.
     *
     * @param bookingRepository persistence boundary for booking records
     * @param customerService service layer used to resolve customers
     * @param sessionTypeService service layer used to resolve session type versions
     */
    public BookingService(
            BookingRepository bookingRepository,
            CustomerService customerService,
            SessionTypeService sessionTypeService) {
        this.bookingRepository = bookingRepository;
        this.customerService = customerService;
        this.sessionTypeService = sessionTypeService;
    }

    /**
     * Lists all stored bookings.
     *
     * @return all bookings currently persisted
     */
    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    /**
     * Looks up one booking by id.
     *
     * @param id booking identifier
     * @return matching booking if it exists
     */
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * Creates a booking in the BOOKED state.
     *
     * @param request booking creation payload
     * @return the created booking
     */
    public Booking create(BookingCreateRequest request) {
        Customer customer =
                customerService
                        .findByPersonalId(request.customerPersonalId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        SessionTypeVersion sessionTypeVersion =
                sessionTypeService
                        .findCurrentVersion(request.sessionTypeName())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Session type version not found"));

        Booking booking =
                new Booking(customer, sessionTypeVersion, request.bookedTime(), BookingStatus.BOOKED);
        return bookingRepository.save(booking);
    }

    /**
     * Updates the status of an existing booking.
     *
     * @param id booking identifier
     * @param request status update payload
     * @return the updated booking
     */
    public Booking updateStatus(Long id, BookingStatusRequest request) {
        Booking existingBooking =
                bookingRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingBooking.setStatus(request.status());
        return bookingRepository.save(existingBooking);
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
