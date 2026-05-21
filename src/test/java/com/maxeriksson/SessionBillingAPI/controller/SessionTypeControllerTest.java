package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.service.SessionTypeService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

/** Unit tests for session type creation and versioning REST endpoints. */
@WebMvcTest(SessionTypeController.class)
class SessionTypeControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private SessionTypeService sessionTypeService;

    @Test
    void createReturnsCreatedVersionWhenSessionTypeDoesNotExist() throws Exception {
        ServiceOfferingVersion serviceOfferingVersion =
                new ServiceOfferingVersion(
                        new ServiceOffering("Coaching"),
                        1,
                        new BigDecimal("500.00"),
                        "SEK",
                        true);
        SessionTypeVersion createdVersion =
                new SessionTypeVersion(
                        new SessionType("GroupSession"),
                        serviceOfferingVersion,
                        1,
                        90,
                        true);
        when(sessionTypeService.create(any(SessionTypeService.SessionTypeCreateRequest.class)))
                .thenReturn(createdVersion);

        mockMvc.perform(post("/session-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"GroupSession\",\"durationMinutes\":90,\"serviceOfferingName\":\"Coaching\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.durationMinutes").value(90))
                .andExpect(jsonPath("$.currentVersion").value(true));

        verify(sessionTypeService).create(any(SessionTypeService.SessionTypeCreateRequest.class));
    }

    @Test
    void createReturnsConflictWhenSessionTypeAlreadyExists() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Session type already exists"))
                .when(sessionTypeService)
                .create(any(SessionTypeService.SessionTypeCreateRequest.class));

        mockMvc.perform(post("/session-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Group Session\",\"durationMinutes\":90,\"serviceOfferingName\":\"Coaching\"}"))
                .andExpect(status().isConflict());

        verify(sessionTypeService).create(any(SessionTypeService.SessionTypeCreateRequest.class));
    }

    @Test
    void createVersionReturnsCreatedVersionWhenSessionTypeExists() throws Exception {
        ServiceOfferingVersion serviceOfferingVersion =
                new ServiceOfferingVersion(
                        new ServiceOffering("Coaching"),
                        2,
                        new BigDecimal("650.00"),
                        "SEK",
                        true);
        SessionTypeVersion createdVersion =
                new SessionTypeVersion(
                        new SessionType("GroupSession"),
                        serviceOfferingVersion,
                        2,
                        60,
                        true);
        when(sessionTypeService.createVersion(
                        "GroupSession",
                        new SessionTypeService.SessionTypeVersionRequest(60, "Coaching")))
                .thenReturn(createdVersion);

        mockMvc.perform(post("/session-types/GroupSession/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMinutes\":60,\"serviceOfferingName\":\"Coaching\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.currentVersion").value(true));

        verify(sessionTypeService)
                .createVersion(
                        "GroupSession",
                        new SessionTypeService.SessionTypeVersionRequest(60, "Coaching"));
    }

    @Test
    void createVersionReturnsNotFoundWhenSessionTypeDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(sessionTypeService)
                .createVersion(
                        "Missing",
                        new SessionTypeService.SessionTypeVersionRequest(60, "Coaching"));

        mockMvc.perform(post("/session-types/Missing/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMinutes\":60,\"serviceOfferingName\":\"Coaching\"}"))
                .andExpect(status().isNotFound());

        verify(sessionTypeService)
                .createVersion(
                        "Missing",
                        new SessionTypeService.SessionTypeVersionRequest(60, "Coaching"));
    }
}
