package com.maxeriksson.SessionBillingAPI.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Legacy bill aggregate.
 *
 * <p>The entity keeps the original bill identifier as a natural key but uses a generated primary
 * key so the model can move away from the prototype composite-id shape.
 */
@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long databaseId;

    @Embedded
    private BillId billId;

    @ManyToOne()
    @JoinColumn(name = "service")
    private Service service;

    @Column(name = "hours")
    private int hours;

    @Column(name = "isPaid")
    private boolean isPaid;

    public Bill() {}

    public Bill(BillId id, Service service, int hours) {
        this(id, service, hours, false);
    }

    public Bill(BillId id, Service service, int hours, boolean isPaid) {
        setId(id);
        this.service = service;
        setHours(hours);
        this.isPaid = isPaid;
    }

    public BillId getId() {
        return billId;
    }

    public void setId(BillId id) {
        this.billId = id;
    }

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Long databaseId) {
        this.databaseId = databaseId;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Invalid input - must be above 0. Try again.");
        }
        this.hours = hours;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    @Override
    public String toString() {
        return "Bill [customer="
                + billId.getCustomer().getPersonalId()
                + ", booking="
                + billId.getBookedTime()
                + ", service="
                + service
                + ", hours="
                + hours
                + ", isPaid="
                + isPaid
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((billId == null) ? 0 : billId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Bill other = (Bill) obj;
        if (billId == null) {
            if (other.billId != null) return false;
        } else if (!billId.equals(other.billId)) return false;
        return true;
    }
}
