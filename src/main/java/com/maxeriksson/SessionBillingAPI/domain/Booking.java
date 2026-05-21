package com.maxeriksson.SessionBillingAPI.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import com.maxeriksson.SessionBillingAPI.model.Customer;

/**
 * Booking aggregate root.
 *
 * <p>The entity stores the customer, the exact session type version used, and the current booking
 * lifecycle status.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumns({
        @JoinColumn(name = "customer_date_of_birth", referencedColumnName = "dateOfBirth"),
        @JoinColumn(name = "customer_id_last_four", referencedColumnName = "idLastFour"),
    })
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_type_version_id", nullable = false)
    private SessionTypeVersion sessionTypeVersion;

    @Column(name = "booked_time", nullable = false)
    private LocalDateTime bookedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    public Booking() {}

    /**
     * Creates a booking.
     *
     * @param customer customer to bill
     * @param sessionTypeVersion exact session type version used
     * @param bookedTime booking time
     * @param status booking lifecycle status
     */
    public Booking(
            Customer customer,
            SessionTypeVersion sessionTypeVersion,
            LocalDateTime bookedTime,
            BookingStatus status) {
        setCustomer(customer);
        setSessionTypeVersion(sessionTypeVersion);
        setBookedTime(bookedTime);
        setStatus(status);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        this.customer = customer;
    }

    public SessionTypeVersion getSessionTypeVersion() {
        return sessionTypeVersion;
    }

    public void setSessionTypeVersion(SessionTypeVersion sessionTypeVersion) {
        if (sessionTypeVersion == null) {
            throw new IllegalArgumentException("Session type version is required");
        }
        this.sessionTypeVersion = sessionTypeVersion;
    }

    public LocalDateTime getBookedTime() {
        return bookedTime;
    }

    public void setBookedTime(LocalDateTime bookedTime) {
        if (bookedTime == null) {
            throw new IllegalArgumentException("Booked time is required");
        }
        this.bookedTime = bookedTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        this.status = status;
    }
}
