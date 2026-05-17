package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** REST controller exposing the current service registry. */
@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;

    /**
     * Creates a service registry controller backed by the existing repository.
     *
     * @param serviceRepository persistence boundary for service records
     */
    public ServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Lists all registered services.
     *
     * @return all service records currently persisted
     */
    @GetMapping
    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    /**
     * Finds one service by its unique name.
     *
     * @param name service name from the request path
     * @return matching service or a 404 response
     */
    @GetMapping("/{name}")
    public ResponseEntity<Service> findByName(@PathVariable String name) {
        return serviceRepository
                .findById(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new service when the name is not already registered.
     *
     * @param request service payload to persist
     * @return the created service
     */
    @PostMapping
    public ResponseEntity<Service> create(@RequestBody ServiceRequest request) {
        if (serviceRepository.existsById(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service already exists");
        }
        Service createdService = new Service(request.name(), request.sekPerHour());
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRepository.save(createdService));
    }

    /**
     * Fully replaces an existing service using strict PUT semantics.
     *
     * @param name service name from the request path
     * @param request replacement payload
     * @return the replaced service
     */
    @PutMapping("/{name}")
    public ResponseEntity<Service> replace(
            @PathVariable String name, @RequestBody ServiceRequest request) {
        Service existingService =
                serviceRepository
                        .findById(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingService.setName(request.name());
        existingService.setSekPerHour(request.sekPerHour());

        return ResponseEntity.ok(serviceRepository.save(existingService));
    }

    /**
     * Partially updates an existing service.
     *
     * @param name service name from the request path
     * @param request patch payload
     * @return the updated service
     */
    @PatchMapping("/{name}")
    public ResponseEntity<Service> patch(
            @PathVariable String name, @RequestBody ServicePatchRequest request) {
        Service existingService =
                serviceRepository
                        .findById(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingService.setSekPerHour(request.sekPerHour());

        return ResponseEntity.ok(serviceRepository.save(existingService));
    }

    /**
     * Deletes an existing service record.
     *
     * @param name service name from the request path
     * @return no content when the service is removed
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable String name) {
        Service existingService =
                serviceRepository
                        .findById(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        serviceRepository.delete(existingService);
        return ResponseEntity.noContent().build();
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
