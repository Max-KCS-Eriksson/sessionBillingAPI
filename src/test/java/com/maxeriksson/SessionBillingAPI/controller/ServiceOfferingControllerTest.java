package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.service.ServiceOfferingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

/** Unit tests for service offering creation and versioning REST endpoints. */
@WebMvcTest(ServiceOfferingController.class)
class ServiceOfferingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ServiceOfferingService serviceOfferingService;

    @Test
    void createReturnsCreatedVersionWhenServiceOfferingDoesNotExist() throws Exception {
        ServiceOfferingVersion createdVersion =
                new ServiceOfferingVersion(
                        new ServiceOffering("Coaching"),
                        1,
                        new BigDecimal("500.00"),
                        "SEK",
                        true);
        when(serviceOfferingService.create(any(ServiceOfferingService.ServiceOfferingCreateRequest.class)))
                .thenReturn(createdVersion);

        mockMvc.perform(post("/service-offerings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coaching\",\"hourlyChargeAmount\":500.00,\"currencyCode\":\"SEK\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.hourlyChargeAmount").value(500.00))
                .andExpect(jsonPath("$.currencyCode").value("SEK"))
                .andExpect(jsonPath("$.currentVersion").value(true));

        verify(serviceOfferingService)
                .create(any(ServiceOfferingService.ServiceOfferingCreateRequest.class));
    }

    @Test
    void createReturnsConflictWhenServiceOfferingAlreadyExists() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Service offering already exists"))
                .when(serviceOfferingService)
                .create(any(ServiceOfferingService.ServiceOfferingCreateRequest.class));

        mockMvc.perform(post("/service-offerings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coaching\",\"hourlyChargeAmount\":500.00,\"currencyCode\":\"SEK\"}"))
                .andExpect(status().isConflict());

        verify(serviceOfferingService)
                .create(any(ServiceOfferingService.ServiceOfferingCreateRequest.class));
    }

    @Test
    void createVersionReturnsCreatedVersionWhenServiceOfferingExists() throws Exception {
        ServiceOfferingVersion createdVersion =
                new ServiceOfferingVersion(
                        new ServiceOffering("Coaching"),
                        2,
                        new BigDecimal("650.00"),
                        "SEK",
                        true);
        when(serviceOfferingService.createVersion(
                        "Coaching",
                        new ServiceOfferingService.ServiceOfferingVersionRequest(
                                new BigDecimal("650.00"), "SEK")))
                .thenReturn(createdVersion);

        mockMvc.perform(post("/service-offerings/Coaching/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hourlyChargeAmount\":650.00,\"currencyCode\":\"SEK\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.hourlyChargeAmount").value(650.00))
                .andExpect(jsonPath("$.currentVersion").value(true));

        verify(serviceOfferingService)
                .createVersion(
                        "Coaching",
                        new ServiceOfferingService.ServiceOfferingVersionRequest(
                                new BigDecimal("650.00"), "SEK"));
    }

    @Test
    void createVersionReturnsNotFoundWhenServiceOfferingDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(serviceOfferingService)
                .createVersion(
                        "Missing",
                        new ServiceOfferingService.ServiceOfferingVersionRequest(
                                new BigDecimal("650.00"), "SEK"));

        mockMvc.perform(post("/service-offerings/Missing/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hourlyChargeAmount\":650.00,\"currencyCode\":\"SEK\"}"))
                .andExpect(status().isNotFound());

        verify(serviceOfferingService)
                .createVersion(
                        "Missing",
                        new ServiceOfferingService.ServiceOfferingVersionRequest(
                                new BigDecimal("650.00"), "SEK"));
    }
}
