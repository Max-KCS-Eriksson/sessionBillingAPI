package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
