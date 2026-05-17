package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/** REST controller exposing the current customer registry. */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    /**
     * Creates a customer registry controller backed by the existing repository.
     *
     * @param customerRepository persistence boundary for customer records
     */
    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Lists all registered customers.
     *
     * @return all customer records currently persisted
     */
    @GetMapping
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * Finds one customer by personal id.
     *
     * @param personalId customer identifier from request path
     * @return matching customer or 404
     */
    @GetMapping("/{personalId}")
    public ResponseEntity<Customer> findByPersonalId(@PathVariable String personalId) {
        PersonalId id = toPersonalId(personalId);
        return customerRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody CustomerCreateRequest request) {
        PersonalId personalId =
                new PersonalId(request.dateOfBirth(), request.idLastFour());
        if (customerRepository.existsById(personalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already exists");
        }

        Customer createdCustomer =
                new Customer(
                        personalId,
                        request.firstName(),
                        request.lastName(),
                        request.address());

        return ResponseEntity.status(HttpStatus.CREATED).body(customerRepository.save(createdCustomer));
    }

    @PutMapping("/{personalId}")
    public ResponseEntity<Customer> replace(
            @PathVariable String personalId, @RequestBody CustomerReplaceRequest request) {
        PersonalId id = toPersonalId(personalId);

        Customer existingCustomer =
                customerRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingCustomer.setFirstName(request.firstName());
        existingCustomer.setLastName(request.lastName());
        existingCustomer.setAddress(request.address());

        return ResponseEntity.ok(customerRepository.save(existingCustomer));
    }

    @PatchMapping("/{personalId}")
    public ResponseEntity<Customer> patch(
            @PathVariable String personalId, @RequestBody CustomerPatchRequest request) {
        PersonalId id = toPersonalId(personalId);

        Customer existingCustomer =
                customerRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (request.firstName() != null) {
            existingCustomer.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            existingCustomer.setLastName(request.lastName());
        }
        if (request.address() != null) {
            existingCustomer.setAddress(request.address());
        }

        return ResponseEntity.ok(customerRepository.save(existingCustomer));
    }

    /**
     * Deletes an existing customer record.
     *
     * @param personalId customer identifier from the request path
     * @return no content when the customer is removed
     */
    @DeleteMapping("/{personalId}")
    public ResponseEntity<Void> delete(@PathVariable String personalId) {
        PersonalId id = toPersonalId(personalId);

        Customer existingCustomer =
                customerRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        customerRepository.delete(existingCustomer);
        return ResponseEntity.noContent().build();
    }

    private PersonalId toPersonalId(String personalId) {
        String[] parts = personalId.split("-");
        if (parts.length != 2 || parts[0].length() != 8 || parts[1].length() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid personal id");
        }

        try {
            LocalDate dateOfBirth =
                    LocalDate.of(
                            Integer.parseInt(parts[0].substring(0, 4)),
                            Integer.parseInt(parts[0].substring(4, 6)),
                            Integer.parseInt(parts[0].substring(6, 8)));
            Integer idLastFour = Integer.parseInt(parts[1]);
            return new PersonalId(dateOfBirth, idLastFour);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid personal id");
        }
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
