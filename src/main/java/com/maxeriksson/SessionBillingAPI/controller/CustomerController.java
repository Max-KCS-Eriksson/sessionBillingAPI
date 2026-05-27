package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.service.CustomerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** REST controller exposing the current customer registry. */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Creates a customer registry controller backed by the existing repository.
     *
     * @param customerService service-layer boundary for customer records
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Lists all registered customers.
     *
     * @return all customer records currently persisted
     */
    @GetMapping
    public List<Customer> findAll() {
        return customerService.findAll();
    }

    /**
     * Finds one customer by personal id.
     *
     * @param personalId customer identifier from request path
     * @return matching customer or 404
     */
    @GetMapping("/{personalId}")
    public ResponseEntity<Customer> findByPersonalId(@PathVariable String personalId) {
        return customerService
                .findByPersonalId(personalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody CustomerCreateRequest request) {
        Customer createdCustomer =
                customerService.create(
                        new CustomerService.CustomerCreateRequest(
                                request.dateOfBirth(),
                                request.idLastFour(),
                                request.firstName(),
                                request.lastName(),
                                request.address()));
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{personalId}")
    public ResponseEntity<Customer> replace(
            @PathVariable String personalId, @RequestBody CustomerReplaceRequest request) {
        Customer updatedCustomer =
                customerService.replace(
                        personalId,
                        new CustomerService.CustomerReplaceRequest(
                                request.firstName(), request.lastName(), request.address()));
        return ResponseEntity.ok(updatedCustomer);
    }

    @PatchMapping("/{personalId}")
    public ResponseEntity<Customer> patch(
            @PathVariable String personalId, @RequestBody CustomerPatchRequest request) {
        Customer updatedCustomer =
                customerService.patch(
                        personalId,
                        new CustomerService.CustomerPatchRequest(
                                request.firstName(), request.lastName(), request.address()));
        return ResponseEntity.ok(updatedCustomer);
    }

    /**
     * Deletes an existing customer record.
     *
     * @param personalId customer identifier from the request path
     * @return no content when the customer is removed
     */
    @DeleteMapping("/{personalId}")
    public ResponseEntity<Void> delete(@PathVariable String personalId) {
        customerService.delete(personalId);
        return ResponseEntity.noContent().build();
    }

    public record CustomerCreateRequest(
            LocalDate dateOfBirth,
            Integer idLastFour,
            String firstName,
            String lastName,
            String address) {}

    public record CustomerReplaceRequest(String firstName, String lastName, String address) {}

    public record CustomerPatchRequest(String firstName, String lastName, String address) {}
}
