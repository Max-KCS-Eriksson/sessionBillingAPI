package com.maxeriksson.SessionBillingAPI.service;

import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.repository.ServiceOfferingRepository;
import com.maxeriksson.SessionBillingAPI.repository.ServiceOfferingVersionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service-layer entry point for service offering workflows.
 *
 * <p>The class owns service offering creation and versioning so the controller can remain a thin
 * HTTP adapter.
 */
@org.springframework.stereotype.Service
@Transactional
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ServiceOfferingVersionRepository serviceOfferingVersionRepository;

    /**
     * Creates a service offering service backed by the existing repositories.
     *
     * @param serviceOfferingRepository persistence boundary for service offerings
     * @param serviceOfferingVersionRepository persistence boundary for service offering versions
     */
    public ServiceOfferingService(
            ServiceOfferingRepository serviceOfferingRepository,
            ServiceOfferingVersionRepository serviceOfferingVersionRepository) {
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.serviceOfferingVersionRepository = serviceOfferingVersionRepository;
    }

    /**
     * Looks up the current version for an existing service offering.
     *
     * @param name unique service offering name
     * @return current version if the offering exists and has one
     */
    @Transactional(readOnly = true)
    public Optional<ServiceOfferingVersion> findCurrentVersion(String name) {
        return serviceOfferingRepository
                .findByName(name)
                .flatMap(
                        serviceOffering ->
                                serviceOfferingVersionRepository
                                        .findFirstByServiceOfferingAndCurrentVersionTrue(
                                                serviceOffering));
    }

    /**
     * Creates a new service offering together with its first version.
     *
     * @param request service offering creation payload
     * @return the created current version
     */
    public ServiceOfferingVersion create(ServiceOfferingCreateRequest request) {
        if (serviceOfferingRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service offering already exists");
        }

        ServiceOffering serviceOffering = serviceOfferingRepository.save(new ServiceOffering(request.name()));
        ServiceOfferingVersion version =
                new ServiceOfferingVersion(
                        serviceOffering,
                        1,
                        request.hourlyChargeAmount(),
                        request.currencyCode(),
                        true);
        return serviceOfferingVersionRepository.save(version);
    }

    /**
     * Creates a new version for an existing service offering.
     *
     * @param name unique service offering name
     * @param request version payload
     * @return the newly created current version
     */
    public ServiceOfferingVersion createVersion(String name, ServiceOfferingVersionRequest request) {
        ServiceOffering serviceOffering =
                serviceOfferingRepository
                        .findByName(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Optional<ServiceOfferingVersion> currentVersion =
                serviceOfferingVersionRepository.findFirstByServiceOfferingAndCurrentVersionTrue(
                        serviceOffering);
        currentVersion.ifPresent(
                version -> {
                    version.setCurrentVersion(false);
                    serviceOfferingVersionRepository.save(version);
                });

        int nextVersionNumber =
                serviceOfferingVersionRepository
                        .findByServiceOfferingOrderByVersionNumberDesc(serviceOffering)
                        .stream()
                        .findFirst()
                        .map(ServiceOfferingVersion::getVersionNumber)
                        .orElse(0)
                        + 1;

        ServiceOfferingVersion version =
                new ServiceOfferingVersion(
                        serviceOffering,
                        nextVersionNumber,
                        request.hourlyChargeAmount(),
                        request.currencyCode(),
                        true);
        return serviceOfferingVersionRepository.save(version);
    }

    /**
     * Request payload for creating a new service offering.
     *
     * @param name unique service offering name
     * @param hourlyChargeAmount hourly charge amount
     * @param currencyCode currency code
     */
    public record ServiceOfferingCreateRequest(
            String name, BigDecimal hourlyChargeAmount, String currencyCode) {}

    /**
     * Request payload for creating a new service offering version.
     *
     * @param hourlyChargeAmount hourly charge amount
     * @param currencyCode currency code
     */
    public record ServiceOfferingVersionRequest(
            BigDecimal hourlyChargeAmount, String currencyCode) {}
}
