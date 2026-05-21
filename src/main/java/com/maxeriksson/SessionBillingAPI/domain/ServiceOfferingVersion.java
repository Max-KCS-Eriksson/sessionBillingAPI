package com.maxeriksson.SessionBillingAPI.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Historical version of a service offering.
 *
 * <p>The version stores the price data that must remain stable after later updates.
 */
@Entity
@Table(name = "service_offering_versions")
public class ServiceOfferingVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_offering_id", nullable = false)
    private ServiceOffering serviceOffering;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "hourly_charge_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyChargeAmount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "is_current_version", nullable = false)
    private boolean currentVersion;

    public ServiceOfferingVersion() {}

    /**
     * Creates a service offering version.
     *
     * @param serviceOffering owning service offering
     * @param versionNumber sequential version number starting at 1
     * @param hourlyChargeAmount hourly charge amount
     * @param currencyCode currency code used for the price
     * @param currentVersion whether the version is the active one
     */
    public ServiceOfferingVersion(
            ServiceOffering serviceOffering,
            int versionNumber,
            BigDecimal hourlyChargeAmount,
            String currencyCode,
            boolean currentVersion) {
        setServiceOffering(serviceOffering);
        setVersionNumber(versionNumber);
        setHourlyChargeAmount(hourlyChargeAmount);
        setCurrencyCode(currencyCode);
        setCurrentVersion(currentVersion);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceOffering getServiceOffering() {
        return serviceOffering;
    }

    public void setServiceOffering(ServiceOffering serviceOffering) {
        if (serviceOffering == null) {
            throw new IllegalArgumentException("Service offering is required");
        }
        this.serviceOffering = serviceOffering;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        if (versionNumber <= 0) {
            throw new IllegalArgumentException("Version number must be above 0");
        }
        this.versionNumber = versionNumber;
    }

    public BigDecimal getHourlyChargeAmount() {
        return hourlyChargeAmount;
    }

    public void setHourlyChargeAmount(BigDecimal hourlyChargeAmount) {
        if (hourlyChargeAmount == null || hourlyChargeAmount.signum() <= 0) {
            throw new IllegalArgumentException("Hourly charge amount must be above 0");
        }
        this.hourlyChargeAmount = hourlyChargeAmount;
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

    public boolean isCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(boolean currentVersion) {
        this.currentVersion = currentVersion;
    }
}
