package com.maxeriksson.SessionBillingAPI.service;

import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Service-layer entry point for the service catalog workflow.
 *
 * <p>The class centralizes service persistence rules so REST controllers can move away from direct
 * repository writes when the migration progresses.
 */
@org.springframework.stereotype.Service
@Transactional
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    /**
     * Creates a service catalog backed by the existing repository.
     *
     * @param serviceRepository persistence boundary for service records
     */
    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Lists all stored services.
     *
     * @return all services currently persisted
     */
    @Transactional(readOnly = true)
    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    /**
     * Looks up one service by name.
     *
     * @param name unique service name
     * @return the matching service if it exists
     */
    @Transactional(readOnly = true)
    public Optional<Service> findByName(String name) {
        return serviceRepository.findById(name);
    }

    /**
     * Creates a new service when the name is not already registered.
     *
     * @param request service creation payload
     * @return the created service
     */
    public Service create(ServiceRequest request) {
        if (serviceRepository.existsById(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service already exists");
        }

        Service createdService = new Service(request.name(), request.sekPerHour());
        return serviceRepository.save(createdService);
    }

    /**
     * Fully replaces an existing service using strict PUT semantics.
     *
     * @param name service name from the request path
     * @param request replacement payload
     * @return the replaced service
     */
    public Service replace(String name, ServiceRequest request) {
        Service existingService =
                serviceRepository
                        .findById(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!name.equals(request.name()) && serviceRepository.existsById(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service already exists");
        }

        Service replacement = new Service(request.name(), request.sekPerHour());
        if (!name.equals(request.name())) {
            serviceRepository.delete(existingService);
        }

        return serviceRepository.save(replacement);
    }

    /**
     * Partially updates an existing service.
     *
     * @param name service name from the request path
     * @param request patch payload
     * @return the updated service
     */
    public Service patch(String name, ServicePatchRequest request) {
        Service existingService =
                serviceRepository
                        .findById(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingService.setSekPerHour(request.sekPerHour());
        return serviceRepository.save(existingService);
    }

    /**
     * Deletes an existing service record.
     *
     * @param name service name from the request path
     */
    public void delete(String name) {
        Service existingService =
                serviceRepository
                        .findById(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        serviceRepository.delete(existingService);
    }

    /**
     * Request payload for creating or fully replacing a service.
     *
     * @param name unique service name
     * @param sekPerHour service hourly rate in SEK
     */
    public record ServiceRequest(String name, int sekPerHour) {}

    /**
     * Request payload for patching a service.
     *
     * @param sekPerHour updated service hourly rate in SEK
     */
    public record ServicePatchRequest(int sekPerHour) {}
}
