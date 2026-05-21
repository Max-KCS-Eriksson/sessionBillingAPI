package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.maxeriksson.SessionBillingAPI.domain.Booking;
import com.maxeriksson.SessionBillingAPI.domain.BookingStatus;
import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.repository.BookingRepository;

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

/** Unit tests for booking creation and status transitions. */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private CustomerService customerService;
    @Mock private SessionTypeService sessionTypeService;

    @InjectMocks private BookingService bookingService;

    @Test
    void findAllReturnsBookings() {
        Booking booking = new Booking(sampleCustomer(), sampleSessionTypeVersion(), LocalDateTime.of(2026, 1, 1, 10, 0), BookingStatus.BOOKED);
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.findAll();

        assertEquals(1, bookings.size());
        verify(bookingRepository).findAll();
    }

    @Test
    void createReturnsBookedBookingWhenCustomerAndSessionTypeExist() {
        Customer customer = sampleCustomer();
        SessionTypeVersion sessionTypeVersion = sampleSessionTypeVersion();
        when(customerService.findByPersonalId("19900102-0123")).thenReturn(Optional.of(customer));
        when(sessionTypeService.findCurrentVersion("GroupSession")).thenReturn(Optional.of(sessionTypeVersion));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking created =
                bookingService.create(
                        new BookingService.BookingCreateRequest(
                                "19900102-0123",
                                "GroupSession",
                                LocalDateTime.of(2026, 1, 1, 10, 0)));

        assertEquals(customer, created.getCustomer());
        assertEquals(sessionTypeVersion, created.getSessionTypeVersion());
        assertEquals(BookingStatus.BOOKED, created.getStatus());
        verify(customerService).findByPersonalId("19900102-0123");
        verify(sessionTypeService).findCurrentVersion("GroupSession");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createThrowsNotFoundWhenCustomerIsMissing() {
        when(customerService.findByPersonalId("19900102-0123")).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                bookingService.create(
                                        new BookingService.BookingCreateRequest(
                                                "19900102-0123",
                                                "GroupSession",
                                                LocalDateTime.of(2026, 1, 1, 10, 0))));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(customerService).findByPersonalId("19900102-0123");
        verifyNoMoreInteractions(sessionTypeService);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void updateStatusChangesExistingBookingStatus() {
        Booking existingBooking =
                new Booking(
                        sampleCustomer(),
                        sampleSessionTypeVersion(),
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        BookingStatus.BOOKED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking updated = bookingService.updateStatus(1L, new BookingService.BookingStatusRequest(BookingStatus.COMPLETED));

        assertEquals(BookingStatus.COMPLETED, updated.getStatus());
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(existingBooking);
    }

    @Test
    void updateStatusThrowsNotFoundWhenBookingDoesNotExist() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> bookingService.updateStatus(1L, new BookingService.BookingStatusRequest(BookingStatus.CANCELLED)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(bookingRepository).findById(1L);
        verifyNoMoreInteractions(bookingRepository);
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
