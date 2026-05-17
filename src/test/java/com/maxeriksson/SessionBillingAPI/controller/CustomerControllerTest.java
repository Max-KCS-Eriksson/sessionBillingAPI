package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Unit tests for the customer registry REST controller. */
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CustomerRepository customerRepository;

    @Test
    void findAllReturnsCustomers() throws Exception {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personalId.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$[0].personalId.idLastFour").value("0123"))
                .andExpect(jsonPath("$[0].firstName").value("Ada"))
                .andExpect(jsonPath("$[0].lastName").value("Lovelace"))
                .andExpect(jsonPath("$[0].address").value("Example Street"));

        verify(customerRepository).findAll();
    }

    

    @Test
    void findByPersonalIdReturnsCustomer() throws Exception {
        PersonalId id = new PersonalId(LocalDate.of(1990, 1, 2), 123);
        Customer customer = new Customer(id, "Ada", "Lovelace", "Example Street");
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/customers/19900102-0123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalId.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$.personalId.idLastFour").value("0123"));

        verify(customerRepository).findById(any(PersonalId.class));
    }

    @Test
    void findByPersonalIdReturnsNotFound() throws Exception {
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/customers/19900102-0123")).andExpect(status().isNotFound());

        verify(customerRepository).findById(any(PersonalId.class));
    }

    @Test
    void findByPersonalIdReturnsBadRequestForInvalidFormat() throws Exception {
        mockMvc.perform(get("/customers/invalid")).andExpect(status().isBadRequest());
    }
@Test
    void createReturnsCreatedWhenCustomerDoesNotExist() throws Exception {
        when(customerRepository.existsById(any(PersonalId.class))).thenReturn(false);
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"dateOfBirth\":\"1990-01-02\",\"idLastFour\":123,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"address\":\"Example Street\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personalId.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$.personalId.idLastFour").value("0123"));

        verify(customerRepository).existsById(any(PersonalId.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createReturnsConflictWhenCustomerExists() throws Exception {
        when(customerRepository.existsById(any(PersonalId.class))).thenReturn(true);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"dateOfBirth\":\"1990-01-02\",\"idLastFour\":123,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"address\":\"Example Street\"}"))
                .andExpect(status().isConflict());

        verify(customerRepository).existsById(any(PersonalId.class));
    }

    @Test
    void replaceReturnsOkWhenCustomerExists() throws Exception {
        PersonalId id = new PersonalId(LocalDate.of(1990, 1, 2), 123);
        Customer existingCustomer = new Customer(id, "Ada", "Lovelace", "Example Street");

        when(customerRepository.findById(any(PersonalId.class)))
                .thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(new Customer(id, "Grace", "Hopper", "Navy Street"));

        mockMvc.perform(put("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Grace\",\"lastName\":\"Hopper\",\"address\":\"Navy Street\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Grace"))
                .andExpect(jsonPath("$.lastName").value("Hopper"))
                .andExpect(jsonPath("$.address").value("Navy Street"));

        verify(customerRepository).findById(any(PersonalId.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void replaceReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Grace\",\"lastName\":\"Hopper\",\"address\":\"Navy Street\"}"))
                .andExpect(status().isNotFound());

        verify(customerRepository).findById(any(PersonalId.class));
    }

    @Test
    void patchReturnsOkWhenCustomerExists() throws Exception {
        PersonalId id = new PersonalId(LocalDate.of(1990, 1, 2), 123);
        Customer existingCustomer = new Customer(id, "Ada", "Lovelace", "Example Street");

        when(customerRepository.findById(any(PersonalId.class)))
                .thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(new Customer(id, "Ada", "Lovelace", "Updated Street"));

        mockMvc.perform(patch("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"Updated Street\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Updated Street"));

        verify(customerRepository).findById(any(PersonalId.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void patchReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"Updated Street\"}"))
                .andExpect(status().isNotFound());

        verify(customerRepository).findById(any(PersonalId.class));
    }

    @Test
    void deleteReturnsNoContentWhenCustomerExists() throws Exception {
        PersonalId id = new PersonalId(LocalDate.of(1990, 1, 2), 123);
        Customer existingCustomer = new Customer(id, "Ada", "Lovelace", "Example Street");

        when(customerRepository.findById(any(PersonalId.class)))
                .thenReturn(Optional.of(existingCustomer));

        mockMvc.perform(delete("/customers/19900102-0123")).andExpect(status().isNoContent());

        verify(customerRepository).findById(any(PersonalId.class));
        verify(customerRepository).delete(existingCustomer);
    }

    @Test
    void deleteReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/customers/19900102-0123")).andExpect(status().isNotFound());

        verify(customerRepository).findById(any(PersonalId.class));
    }
}
