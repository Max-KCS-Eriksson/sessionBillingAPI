package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repository for historical service offering versions. */
public interface ServiceOfferingVersionRepository
        extends JpaRepository<ServiceOfferingVersion, Long> {

    /**
     * Finds the current version for a service offering.
     *
     * @param serviceOffering owning service offering
     * @return current version if one exists
     */
    Optional<ServiceOfferingVersion> findFirstByServiceOfferingAndCurrentVersionTrue(
            ServiceOffering serviceOffering);

    /**
     * Lists versions for a service offering with newest entries first.
     *
     * @param serviceOffering owning service offering
     * @return versions ordered by version number descending
     */
    List<ServiceOfferingVersion> findByServiceOfferingOrderByVersionNumberDesc(
            ServiceOffering serviceOffering);
}
