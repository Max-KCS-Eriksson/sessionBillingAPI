package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.model.SocialSecurityNumber;
import com.maxeriksson.SessionBillingAPI.repository.BillRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Unit tests for the bill registry REST controller. */
@WebMvcTest(BillController.class)
class BillControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BillRepository billRepository;

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
}
