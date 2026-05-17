package com.maxeriksson.SessionBillingAPI.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

import java.io.Serializable;
import java.time.LocalDateTime;

/** BillId */
@Embeddable
public class BillId implements Serializable {

    @MapsId("customerId")
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "customerDateOfBirth", referencedColumnName = "dateOfBirth"),
        @JoinColumn(name = "customerIdLastFour", referencedColumnName = "idLastFour"),
    })
    private Customer customer;

    @Column(name = "bookedTime")
    private LocalDateTime bookedTime;

    public BillId() {} // Required by JPA

    public BillId(Customer customer, LocalDateTime bookedTime) {
        this.customer = customer;
        this.bookedTime = bookedTime;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomerId(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getBookedTime() {
        return bookedTime;
    }

    public void setBookedTime(LocalDateTime bookedTime) {
        this.bookedTime = bookedTime;
    }

    @Override
    public String toString() {
        return "BillId [customer="
                + customer.getSocialSecurityNumber()
                + ", bookedTime="
                + bookedTime
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((customer == null) ? 0 : customer.hashCode());
        result = prime * result + ((bookedTime == null) ? 0 : bookedTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BillId other = (BillId) obj;
        if (customer == null) {
            if (other.customer != null) return false;
        } else if (!customer.equals(other.customer)) return false;
        if (bookedTime == null) {
            if (other.bookedTime != null) return false;
        } else if (!bookedTime.equals(other.bookedTime)) return false;
        return true;
    }
}
