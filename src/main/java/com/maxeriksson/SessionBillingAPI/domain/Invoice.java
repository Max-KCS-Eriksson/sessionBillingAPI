package com.maxeriksson.SessionBillingAPI.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Invoice aggregate root.
 *
 * <p>The entity stores the generated payment request for one completed booking.
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    public Invoice() {}

    /**
     * Creates an invoice.
     *
     * @param booking completed booking to invoice
     * @param invoiceNumber generated invoice number
     * @param status invoice payment status
     * @param issueDate invoice issue date
     * @param totalAmount invoice total amount
     * @param currencyCode invoice currency code
     */
    public Invoice(
            Booking booking,
            String invoiceNumber,
            InvoiceStatus status,
            LocalDateTime issueDate,
            BigDecimal totalAmount,
            String currencyCode) {
        setBooking(booking);
        setInvoiceNumber(invoiceNumber);
        setStatus(status);
        setIssueDate(issueDate);
        setTotalAmount(totalAmount);
        setCurrencyCode(currencyCode);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking is required");
        }
        this.booking = booking;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("Invoice number is required");
        }
        this.invoiceNumber = invoiceNumber;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        this.status = status;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        if (issueDate == null) {
            throw new IllegalArgumentException("Issue date is required");
        }
        this.issueDate = issueDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.signum() < 0) {
            throw new IllegalArgumentException("Total amount is required");
        }
        this.totalAmount = totalAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency code is required");
        }
        this.currencyCode = currencyCode;
    }
}
