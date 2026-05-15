package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.SocialSecurityNumber;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
     * Creates a new customer when the social security number is not already registered.
     *
     * @param request customer payload to persist
     * @return the created customer
     */
    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody CustomerCreateRequest request) {
        SocialSecurityNumber socialSecurityNumber =
                new SocialSecurityNumber(request.dateOfBirth(), request.idLastFour());
        if (customerRepository.existsById(socialSecurityNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already exists");
        }

        Customer createdCustomer =
                new Customer(
                        socialSecurityNumber,
                        request.firstName(),
                        request.lastName(),
                        request.address());

        return ResponseEntity.status(HttpStatus.CREATED).body(customerRepository.save(createdCustomer));
    }

    /**
     * Fully replaces an existing customer using strict PUT semantics.
     *
     * @param socialSecurityNumber customer identifier from the request path
     * @param request replacement payload
     * @return the replaced customer
     */
    @PutMapping("/{socialSecurityNumber}")
    public ResponseEntity<Customer> replace(
            @PathVariable String socialSecurityNumber, @RequestBody CustomerReplaceRequest request) {
        SocialSecurityNumber id = toSocialSecurityNumber(socialSecurityNumber);

        Customer existingCustomer =
                customerRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingCustomer.setFirstName(request.firstName());
        existingCustomer.setLastName(request.lastName());
        existingCustomer.setAddress(request.address());

        return ResponseEntity.ok(customerRepository.save(existingCustomer));
    }

    /**
     * Partially updates an existing customer.
     *
     * @param socialSecurityNumber customer identifier from the request path
     * @param request patch payload
     * @return the updated customer
     */
    @PatchMapping("/{socialSecurityNumber}")
    public ResponseEntity<Customer> patch(
            @PathVariable String socialSecurityNumber, @RequestBody CustomerPatchRequest request) {
        SocialSecurityNumber id = toSocialSecurityNumber(socialSecurityNumber);

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
     * Parses a social security number path token in the format yyyyMMdd-xxxx.
     *
     * @param socialSecurityNumber customer identifier from request path
     * @return parsed social security number object
     */
    private SocialSecurityNumber toSocialSecurityNumber(String socialSecurityNumber) {
        String[] parts = socialSecurityNumber.split("-");
        if (parts.length != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid social security number");
        }

        try {
            LocalDate dateOfBirth =
                    LocalDate.of(
                            Integer.parseInt(parts[0].substring(0, 4)),
                            Integer.parseInt(parts[0].substring(4, 6)),
                            Integer.parseInt(parts[0].substring(6, 8)));
            Integer idLastFour = Integer.parseInt(parts[1]);
            return new SocialSecurityNumber(dateOfBirth, idLastFour);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid social security number");
        }
    }

    /**
     * Request payload for creating a customer.
     *
     * @param dateOfBirth customer date of birth
     * @param idLastFour customer social security number suffix
     * @param firstName customer first name
     * @param lastName customer last name
     * @param address customer address
     */
    public record CustomerCreateRequest(
            LocalDate dateOfBirth,
            Integer idLastFour,
            String firstName,
            String lastName,
            String address) {}

    /**
     * Request payload for fully replacing a customer.
     *
     * @param firstName customer first name
     * @param lastName customer last name
     * @param address customer address
     */
    public record CustomerReplaceRequest(String firstName, String lastName, String address) {}

    /**
     * Request payload for patching a customer.
     *
     * @param firstName customer first name
     * @param lastName customer last name
     * @param address customer address
     */
    public record CustomerPatchRequest(String firstName, String lastName, String address) {}
}
