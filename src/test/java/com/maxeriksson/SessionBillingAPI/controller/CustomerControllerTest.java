package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import com.maxeriksson.SessionBillingAPI.service.CustomerService;

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

    @MockBean private CustomerService customerService;

    @Test
    void findAllReturnsCustomers() throws Exception {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerService.findAll()).thenReturn(List.of(customer));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personalId.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$[0].personalId.idLastFour").value("0123"))
                .andExpect(jsonPath("$[0].firstName").value("Ada"))
                .andExpect(jsonPath("$[0].lastName").value("Lovelace"))
                .andExpect(jsonPath("$[0].address").value("Example Street"));

        verify(customerService).findAll();
    }

    @Test
    void findByPersonalIdReturnsCustomer() throws Exception {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerService.findByPersonalId("19900102-0123")).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/customers/19900102-0123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalId.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$.personalId.idLastFour").value("0123"));

        verify(customerService).findByPersonalId("19900102-0123");
    }

    @Test
    void findByPersonalIdReturnsNotFound() throws Exception {
        when(customerService.findByPersonalId("19900102-0123")).thenReturn(Optional.empty());

        mockMvc.perform(get("/customers/19900102-0123")).andExpect(status().isNotFound());

        verify(customerService).findByPersonalId("19900102-0123");
    }

    @Test
    void findByPersonalIdReturnsBadRequestForInvalidFormat() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid personal id"))
                .when(customerService)
                .findByPersonalId("invalid");

        mockMvc.perform(get("/customers/invalid")).andExpect(status().isBadRequest());

        verify(customerService).findByPersonalId("invalid");
    }

    @Test
    void createReturnsCreatedWhenCustomerDoesNotExist() throws Exception {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerService.create(any(CustomerService.CustomerCreateRequest.class)))
                .thenReturn(customer);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"dateOfBirth\":\"1990-01-02\",\"idLastFour\":123,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"address\":\"Example Street\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personalId.dateOfBirth").value("1990-01-02"))
                .andExpect(jsonPath("$.personalId.idLastFour").value("0123"));

        verify(customerService).create(any(CustomerService.CustomerCreateRequest.class));
    }

    @Test
    void createReturnsConflictWhenCustomerExists() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Customer already exists"))
                .when(customerService)
                .create(any(CustomerService.CustomerCreateRequest.class));

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"dateOfBirth\":\"1990-01-02\",\"idLastFour\":123,\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"address\":\"Example Street\"}"))
                .andExpect(status().isConflict());

        verify(customerService).create(any(CustomerService.CustomerCreateRequest.class));
    }

    @Test
    void replaceReturnsOkWhenCustomerExists() throws Exception {
        Customer updatedCustomer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Grace",
                        "Hopper",
                        "Navy Street");
        when(customerService.replace(
                        "19900102-0123",
                        new CustomerService.CustomerReplaceRequest(
                                "Grace", "Hopper", "Navy Street")))
                .thenReturn(updatedCustomer);

        mockMvc.perform(put("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Grace\",\"lastName\":\"Hopper\",\"address\":\"Navy Street\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Grace"))
                .andExpect(jsonPath("$.lastName").value("Hopper"))
                .andExpect(jsonPath("$.address").value("Navy Street"));

        verify(customerService)
                .replace(
                        "19900102-0123",
                        new CustomerService.CustomerReplaceRequest(
                                "Grace", "Hopper", "Navy Street"));
    }

    @Test
    void replaceReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(customerService)
                .replace(
                        "19900102-0123",
                        new CustomerService.CustomerReplaceRequest(
                                "Grace", "Hopper", "Navy Street"));

        mockMvc.perform(put("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Grace\",\"lastName\":\"Hopper\",\"address\":\"Navy Street\"}"))
                .andExpect(status().isNotFound());

        verify(customerService)
                .replace(
                        "19900102-0123",
                        new CustomerService.CustomerReplaceRequest(
                                "Grace", "Hopper", "Navy Street"));
    }

    @Test
    void patchReturnsOkWhenCustomerExists() throws Exception {
        Customer updatedCustomer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Updated Street");
        when(customerService.patch(
                        "19900102-0123",
                        new CustomerService.CustomerPatchRequest(null, null, "Updated Street")))
                .thenReturn(updatedCustomer);

        mockMvc.perform(patch("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"Updated Street\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Updated Street"));

        verify(customerService)
                .patch(
                        "19900102-0123",
                        new CustomerService.CustomerPatchRequest(null, null, "Updated Street"));
    }

    @Test
    void patchReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(customerService)
                .patch(
                        "19900102-0123",
                        new CustomerService.CustomerPatchRequest(null, null, "Updated Street"));

        mockMvc.perform(patch("/customers/19900102-0123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"Updated Street\"}"))
                .andExpect(status().isNotFound());

        verify(customerService)
                .patch(
                        "19900102-0123",
                        new CustomerService.CustomerPatchRequest(null, null, "Updated Street"));
    }

    @Test
    void deleteReturnsNoContentWhenCustomerExists() throws Exception {
        mockMvc.perform(delete("/customers/19900102-0123")).andExpect(status().isNoContent());

        verify(customerService).delete("19900102-0123");
    }

    @Test
    void deleteReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(customerService)
                .delete("19900102-0123");

        mockMvc.perform(delete("/customers/19900102-0123")).andExpect(status().isNotFound());

        verify(customerService).delete("19900102-0123");
    }
}
