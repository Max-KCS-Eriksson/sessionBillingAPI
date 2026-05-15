package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.SocialSecurityNumber;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

/** Unit tests for the customer registry REST controller. */
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CustomerRepository customerRepository;

    @Test
    void findAllReturnsCustomers() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].socialSecurityNumber.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$[0].socialSecurityNumber.idLastFour").value("0123"))
                .andExpect(jsonPath("$[0].firstName").value("Ada"))
                .andExpect(jsonPath("$[0].lastName").value("Lovelace"))
                .andExpect(jsonPath("$[0].address").value("Example Street"));

        verify(customerRepository).findAll();
    }
}
