package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
