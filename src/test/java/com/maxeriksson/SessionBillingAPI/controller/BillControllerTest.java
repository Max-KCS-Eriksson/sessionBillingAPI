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

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;
import com.maxeriksson.SessionBillingAPI.service.BillService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;

/** Unit tests for the bill registry REST controller. */
@WebMvcTest(BillController.class)
class BillControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BillService billService;

    @Test
    void findAllReturnsBills() throws Exception {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Bill bill =
                new Bill(
                        new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0)),
                        new Service("Coaching", 500),
                        2,
                        false);
        when(billService.findAll()).thenReturn(List.of(bill));

        mockMvc.perform(get("/bills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.customer.firstName").value("Ada"))
                .andExpect(jsonPath("$[0].id.bookedTime").value("2026-01-01T10:00:00"))
                .andExpect(jsonPath("$[0].service.name").value("Coaching"))
                .andExpect(jsonPath("$[0].hours").value(2))
                .andExpect(jsonPath("$[0].paid").value(false));

        verify(billService).findAll();
    }

    @Test
    void createReturnsCreatedWhenBillDoesNotExist() throws Exception {
        Bill bill =
                new Bill(
                        new BillId(
                                new Customer(
                                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                                        "Ada",
                                        "Lovelace",
                                        "Example Street"),
                                LocalDateTime.of(2026, 1, 1, 10, 0)),
                        new Service("Coaching", 500),
                        2,
                        false);

        when(billService.create(any(BillService.BillCreateRequest.class))).thenReturn(bill);

        mockMvc.perform(
                        post("/bills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerPersonalId\":\"19900102-0123\",\"bookedTime\":\"2026-01-01T10:00:00\",\"serviceName\":\"Coaching\",\"hours\":2,\"paid\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.customer.firstName").value("Ada"))
                .andExpect(jsonPath("$.id.bookedTime").value("2026-01-01T10:00:00"))
                .andExpect(jsonPath("$.service.name").value("Coaching"))
                .andExpect(jsonPath("$.hours").value(2))
                .andExpect(jsonPath("$.paid").value(false));

        verify(billService).create(any(BillService.BillCreateRequest.class));
    }

    @Test
    void createReturnsConflictWhenBillAlreadyExists() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Bill already exists"))
                .when(billService)
                .create(any(BillService.BillCreateRequest.class));

        mockMvc.perform(
                        post("/bills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"customerPersonalId\":\"19900102-0123\",\"bookedTime\":\"2026-01-01T10:00:00\",\"serviceName\":\"Coaching\",\"hours\":2,\"paid\":false}"))
                .andExpect(status().isConflict());

        verify(billService).create(any(BillService.BillCreateRequest.class));
    }

    @Test
    void replaceReturnsOkWhenBillExists() throws Exception {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service replacementService = new Service("Advanced", 700);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill updatedBill = new Bill(id, replacementService, 4, true);

        when(billService.replace(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillReplaceRequest("Advanced", 4, true)))
                .thenReturn(updatedBill);

        mockMvc.perform(
                        put("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"serviceName\":\"Advanced\",\"hours\":4,\"paid\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service.name").value("Advanced"))
                .andExpect(jsonPath("$.hours").value(4))
                .andExpect(jsonPath("$.paid").value(true));

        verify(billService)
                .replace(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillReplaceRequest("Advanced", 4, true));
    }

    @Test
    void replaceReturnsNotFoundWhenBillDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(billService)
                .replace(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillReplaceRequest("Advanced", 4, true));

        mockMvc.perform(
                        put("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"serviceName\":\"Advanced\",\"hours\":4,\"paid\":true}"))
                .andExpect(status().isNotFound());

        verify(billService)
                .replace(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillReplaceRequest("Advanced", 4, true));
    }

    @Test
    void patchReturnsOkWhenBillExists() throws Exception {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service service = new Service("Coaching", 500);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill updatedBill = new Bill(id, service, 3, true);

        when(billService.patch(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillPatchRequest(null, 3, true)))
                .thenReturn(updatedBill);

        mockMvc.perform(
                        patch("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"hours\":3,\"paid\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(3))
                .andExpect(jsonPath("$.paid").value(true));

        verify(billService)
                .patch(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillPatchRequest(null, 3, true));
    }

    @Test
    void patchReturnsNotFoundWhenBillDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(billService)
                .patch(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillPatchRequest(null, 3, true));

        mockMvc.perform(
                        patch("/bills/19900102-0123/2026-01-01T10:00:00")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"hours\":3,\"paid\":true}"))
                .andExpect(status().isNotFound());

        verify(billService)
                .patch(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillPatchRequest(null, 3, true));
    }

    @Test
    void deleteReturnsNoContentWhenBillExists() throws Exception {
        mockMvc.perform(delete("/bills/19900102-0123/2026-01-01T10:00:00"))
                .andExpect(status().isNoContent());

        verify(billService).delete("19900102-0123", LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    @Test
    void deleteReturnsNotFoundWhenBillDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(billService)
                .delete("19900102-0123", LocalDateTime.of(2026, 1, 1, 10, 0));

        mockMvc.perform(delete("/bills/19900102-0123/2026-01-01T10:00:00"))
                .andExpect(status().isNotFound());

        verify(billService).delete("19900102-0123", LocalDateTime.of(2026, 1, 1, 10, 0));
    }
}
