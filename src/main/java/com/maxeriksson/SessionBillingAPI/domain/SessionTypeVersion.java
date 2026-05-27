package com.maxeriksson.SessionBillingAPI.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Historical version of a session type.
 *
 * <p>The version stores the duration and the exact service offering version used for billing.
 */
@Entity
@Table(name = "session_type_versions")
public class SessionTypeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_type_id", nullable = false)
    private SessionType sessionType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_offering_version_id", nullable = false)
    private ServiceOfferingVersion serviceOfferingVersion;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "is_current_version", nullable = false)
    private boolean currentVersion;

    public SessionTypeVersion() {}

    /**
     * Creates a session type version.
     *
     * @param sessionType owning session type
     * @param serviceOfferingVersion exact service offering version used
     * @param versionNumber sequential version number starting at 1
     * @param durationMinutes session duration in minutes
     * @param currentVersion whether the version is the active one
     */
    public SessionTypeVersion(
            SessionType sessionType,
            ServiceOfferingVersion serviceOfferingVersion,
            int versionNumber,
            int durationMinutes,
            boolean currentVersion) {
        setSessionType(sessionType);
        setServiceOfferingVersion(serviceOfferingVersion);
        setVersionNumber(versionNumber);
        setDurationMinutes(durationMinutes);
        setCurrentVersion(currentVersion);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        if (sessionType == null) {
            throw new IllegalArgumentException("Session type is required");
        }
        this.sessionType = sessionType;
    }

    public ServiceOfferingVersion getServiceOfferingVersion() {
        return serviceOfferingVersion;
    }

    public void setServiceOfferingVersion(ServiceOfferingVersion serviceOfferingVersion) {
        if (serviceOfferingVersion == null) {
            throw new IllegalArgumentException("Service offering version is required");
        }
        this.serviceOfferingVersion = serviceOfferingVersion;
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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be above 0");
        }
        this.durationMinutes = durationMinutes;
    }

    public boolean isCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(boolean currentVersion) {
        this.currentVersion = currentVersion;
    }
}
