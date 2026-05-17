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

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.model.SocialSecurityNumber;
import com.maxeriksson.SessionBillingAPI.repository.BillRepository;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Unit tests for the bill registry REST controller. */
@WebMvcTest(BillController.class)
class BillControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BillRepository billRepository;
    @MockBean private CustomerRepository customerRepository;
    @MockBean private ServiceRepository serviceRepository;

    @Test
    void findAllReturnsBills() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Bill bill =
                new Bill(
                        new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0)),
                        new Service("Coaching", 500),
                        2,
                        false);
        when(billRepository.findAll()).thenReturn(List.of(bill));

        mockMvc.perform(get("/bills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.customer.firstName").value("Ada"))
                .andExpect(jsonPath("$[0].id.bookedTime").value("2026-01-01T10:00:00"))
                .andExpect(jsonPath("$[0].service.name").value("Coaching"))
                .andExpect(jsonPath("$[0].hours").value(2))
                .andExpect(jsonPath("$[0].paid").value(false));

        verify(billRepository).findAll();
    }

    @Test
    void createReturnsCreatedWhenBillDoesNotExist() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service service = new Service("Coaching", 500);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill bill = new Bill(id, service, 2, false);

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(service));
        when(billRepository.existsById(any(BillId.class))).thenReturn(false);
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        mockMvc.perform(
                        post("/bills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerSocialSecurityNumber\":\"19900102-0123\",\"bookedTime\":\"2026-01-01T10:00:00\",\"serviceName\":\"Coaching\",\"hours\":2,\"paid\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.customer.firstName").value("Ada"))
                .andExpect(jsonPath("$.id.bookedTime").value("2026-01-01T10:00:00"))
                .andExpect(jsonPath("$.service.name").value("Coaching"))
                .andExpect(jsonPath("$.hours").value(2))
                .andExpect(jsonPath("$.paid").value(false));

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(serviceRepository).findById("Coaching");
        verify(billRepository).existsById(any(BillId.class));
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void createReturnsConflictWhenBillAlreadyExists() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(serviceRepository.findById("Coaching"))
                .thenReturn(Optional.of(new Service("Coaching", 500)));
        when(billRepository.existsById(any(BillId.class))).thenReturn(true);

        mockMvc.perform(
                        post("/bills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerSocialSecurityNumber\":\"19900102-0123\",\"bookedTime\":\"2026-01-01T10:00:00\",\"serviceName\":\"Coaching\",\"hours\":2,\"paid\":false}"))
                .andExpect(status().isConflict());

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(serviceRepository).findById("Coaching");
        verify(billRepository).existsById(any(BillId.class));
    }

    @Test
    void replaceReturnsOkWhenBillExists() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service existingService = new Service("Coaching", 500);
        Service replacementService = new Service("Advanced", 700);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill existingBill = new Bill(id, existingService, 2, false);

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.of(existingBill));
        when(serviceRepository.findById("Advanced")).thenReturn(Optional.of(replacementService));
        when(billRepository.save(any(Bill.class)))
                .thenReturn(new Bill(id, replacementService, 4, true));

        mockMvc.perform(
                        put("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"serviceName\":\"Advanced\",\"hours\":4,\"paid\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service.name").value("Advanced"))
                .andExpect(jsonPath("$.hours").value(4))
                .andExpect(jsonPath("$.paid").value(true));

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(billRepository).findById(any(BillId.class));
        verify(serviceRepository).findById("Advanced");
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void replaceReturnsNotFoundWhenBillDoesNotExist() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.empty());

        mockMvc.perform(
                        put("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"serviceName\":\"Advanced\",\"hours\":4,\"paid\":true}"))
                .andExpect(status().isNotFound());

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(billRepository).findById(any(BillId.class));
    }

    @Test
    void patchReturnsOkWhenBillExists() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service service = new Service("Coaching", 500);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill existingBill = new Bill(id, service, 2, false);

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.of(existingBill));
        when(billRepository.save(any(Bill.class))).thenReturn(new Bill(id, service, 3, true));

        mockMvc.perform(
                        patch("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"hours\":3,\"paid\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(3))
                .andExpect(jsonPath("$.paid").value(true));

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(billRepository).findById(any(BillId.class));
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void patchReturnsNotFoundWhenBillDoesNotExist() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.empty());

        mockMvc.perform(
                        patch("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"hours\":3,\"paid\":true}"))
                .andExpect(status().isNotFound());

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(billRepository).findById(any(BillId.class));
    }

    @Test
    void deleteReturnsNoContentWhenBillExists() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill existingBill = new Bill(id, new Service("Coaching", 500), 2, false);

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.of(existingBill));

        mockMvc.perform(delete("/bills/19900102-0123/2026-01-01T10:00:00"))
                .andExpect(status().isNoContent());

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(billRepository).findById(any(BillId.class));
        verify(billRepository).delete(existingBill);
    }

    @Test
    void deleteReturnsNotFoundWhenBillDoesNotExist() throws Exception {
        Customer customer =
                new Customer(
                        new SocialSecurityNumber(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");

        when(customerRepository.findById(any(SocialSecurityNumber.class)))
                .thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/bills/19900102-0123/2026-01-01T10:00:00"))
                .andExpect(status().isNotFound());

        verify(customerRepository).findById(any(SocialSecurityNumber.class));
        verify(billRepository).findById(any(BillId.class));
    }
}
