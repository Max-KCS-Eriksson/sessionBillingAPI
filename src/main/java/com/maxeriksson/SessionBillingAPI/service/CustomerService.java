package com.maxeriksson.SessionBillingAPI.service;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service-layer entry point for customer workflows.
 *
 * <p>The class centralizes customer persistence rules and personal id parsing so the customer
 * controller can delegate HTTP requests to a single domain-focused boundary.
 */
@org.springframework.stereotype.Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Creates a customer service backed by the existing repository.
     *
     * @param customerRepository persistence boundary for customer records
     */
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Lists all stored customers.
     *
     * @return all customers currently persisted
     */
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * Looks up one customer by personal id.
     *
     * @param personalId customer identifier from the request path
     * @return the matching customer if it exists
     */
    @Transactional(readOnly = true)
    public Optional<Customer> findByPersonalId(String personalId) {
        return customerRepository.findById(toPersonalId(personalId));
    }

    /**
     * Creates a new customer when the personal id is not already registered.
     *
     * @param request customer creation payload
     * @return the created customer
     */
    public Customer create(CustomerCreateRequest request) {
        PersonalId personalId = new PersonalId(request.dateOfBirth(), request.idLastFour());
        if (customerRepository.existsById(personalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already exists");
        }

        Customer createdCustomer =
                new Customer(
                        personalId, request.firstName(), request.lastName(), request.address());
        return customerRepository.save(createdCustomer);
    }

    /**
     * Fully replaces an existing customer using strict PUT semantics.
     *
     * @param personalId customer identifier from the request path
     * @param request replacement payload
     * @return the replaced customer
     */
    public Customer replace(String personalId, CustomerReplaceRequest request) {
        Customer existingCustomer =
                customerRepository
                        .findById(toPersonalId(personalId))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingCustomer.setFirstName(request.firstName());
        existingCustomer.setLastName(request.lastName());
        existingCustomer.setAddress(request.address());

        return customerRepository.save(existingCustomer);
    }

    /**
     * Partially updates an existing customer.
     *
     * @param personalId customer identifier from the request path
     * @param request patch payload
     * @return the updated customer
     */
    public Customer patch(String personalId, CustomerPatchRequest request) {
        Customer existingCustomer =
                customerRepository
                        .findById(toPersonalId(personalId))
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

        return customerRepository.save(existingCustomer);
    }

    /**
     * Deletes an existing customer record.
     *
     * @param personalId customer identifier from the request path
     */
    public void delete(String personalId) {
        Customer existingCustomer =
                customerRepository
                        .findById(toPersonalId(personalId))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        customerRepository.delete(existingCustomer);
    }

    /**
     * Request payload for creating a customer.
     *
     * @param dateOfBirth customer birth date
     * @param idLastFour last four digits of the personal id
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
     * Request payload for partially updating a customer.
     *
     * @param firstName customer first name
     * @param lastName customer last name
     * @param address customer address
     */
    public record CustomerPatchRequest(String firstName, String lastName, String address) {}

    /**
     * Parses the legacy personal id format used by the prototype.
     *
     * @param personalId customer identifier from the request
     * @return parsed identifier value object
     */
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
}
