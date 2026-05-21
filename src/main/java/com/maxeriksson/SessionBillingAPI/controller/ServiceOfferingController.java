package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.service.ServiceOfferingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/** REST controller exposing service offering creation and versioning. */
@RestController
@RequestMapping("/service-offerings")
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    /**
     * Creates a service offering controller backed by the service layer.
     *
     * @param serviceOfferingService service-layer boundary for service offerings
     */
    public ServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    /**
     * Creates a new service offering together with its first version.
     *
     * @param request creation payload
     * @return the created current version
     */
    @PostMapping
    public ResponseEntity<ServiceOfferingVersion> create(
            @RequestBody ServiceOfferingCreateRequest request) {
        ServiceOfferingVersion createdVersion =
                serviceOfferingService.create(
                        new ServiceOfferingService.ServiceOfferingCreateRequest(
                                request.name(),
                                request.hourlyChargeAmount(),
                                request.currencyCode()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
    }

    /**
     * Creates a new version for an existing service offering.
     *
     * @param name service offering name from the request path
     * @param request version payload
     * @return the newly created current version
     */
    @PostMapping("/{name}/versions")
    public ResponseEntity<ServiceOfferingVersion> createVersion(
            @PathVariable String name, @RequestBody ServiceOfferingVersionRequest request) {
        ServiceOfferingVersion createdVersion =
                serviceOfferingService.createVersion(
                        name,
                        new ServiceOfferingService.ServiceOfferingVersionRequest(
                                request.hourlyChargeAmount(), request.currencyCode()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
    }

    /**
     * Request payload for creating a service offering.
     *
     * @param name unique service offering name
     * @param hourlyChargeAmount hourly charge amount
     * @param currencyCode currency code
     */
    public record ServiceOfferingCreateRequest(
            String name, BigDecimal hourlyChargeAmount, String currencyCode) {}

    /**
     * Request payload for creating a service offering version.
     *
     * @param hourlyChargeAmount hourly charge amount
     * @param currencyCode currency code
     */
    public record ServiceOfferingVersionRequest(
            BigDecimal hourlyChargeAmount, String currencyCode) {}
}
