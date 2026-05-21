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

import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.service.ServiceCatalogService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

/** Unit tests for the service registry REST controller. */
@WebMvcTest(ServiceController.class)
class ServiceControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ServiceCatalogService serviceCatalogService;

    @Test
    void findAllReturnsServices() throws Exception {
        when(serviceCatalogService.findAll()).thenReturn(List.of(new Service("Coaching", 500)));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Coaching"))
                .andExpect(jsonPath("$[0].sekPerHour").value(500));

        verify(serviceCatalogService).findAll();
    }

    @Test
    void findByNameReturnsService() throws Exception {
        when(serviceCatalogService.findByName("Coaching"))
                .thenReturn(Optional.of(new Service("Coaching", 500)));

        mockMvc.perform(get("/services/Coaching"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coaching"))
                .andExpect(jsonPath("$.sekPerHour").value(500));

        verify(serviceCatalogService).findByName("Coaching");
    }

    @Test
    void findByNameReturnsNotFound() throws Exception {
        when(serviceCatalogService.findByName("Missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/services/Missing")).andExpect(status().isNotFound());

        verify(serviceCatalogService).findByName("Missing");
    }

    @Test
    void createReturnsCreatedWhenServiceDoesNotExist() throws Exception {
        when(serviceCatalogService.create(any(ServiceCatalogService.ServiceRequest.class)))
                .thenReturn(new Service("Coaching", 500));

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coaching\",\"sekPerHour\":500}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Coaching"))
                .andExpect(jsonPath("$.sekPerHour").value(500));

        verify(serviceCatalogService).create(any(ServiceCatalogService.ServiceRequest.class));
    }

    @Test
    void createReturnsConflictWhenServiceAlreadyExists() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Service already exists"))
                .when(serviceCatalogService)
                .create(any(ServiceCatalogService.ServiceRequest.class));

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coaching\",\"sekPerHour\":500}"))
                .andExpect(status().isConflict());

        verify(serviceCatalogService).create(any(ServiceCatalogService.ServiceRequest.class));
    }

    @Test
    void replaceReturnsOkWhenServiceExists() throws Exception {
        when(serviceCatalogService.replace(
                        "Coaching", new ServiceCatalogService.ServiceRequest("Advanced", 700)))
                .thenReturn(new Service("Advanced", 700));

        mockMvc.perform(put("/services/Coaching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Advanced\",\"sekPerHour\":700}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Advanced"))
                .andExpect(jsonPath("$.sekPerHour").value(700));

        verify(serviceCatalogService)
                .replace("Coaching", new ServiceCatalogService.ServiceRequest("Advanced", 700));
    }

    @Test
    void replaceReturnsNotFoundWhenServiceDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(serviceCatalogService)
                .replace("Missing", new ServiceCatalogService.ServiceRequest("Advanced", 700));

        mockMvc.perform(put("/services/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Advanced\",\"sekPerHour\":700}"))
                .andExpect(status().isNotFound());

        verify(serviceCatalogService)
                .replace("Missing", new ServiceCatalogService.ServiceRequest("Advanced", 700));
    }

    @Test
    void patchReturnsOkWhenServiceExists() throws Exception {
        when(serviceCatalogService.patch(
                        "Coaching", new ServiceCatalogService.ServicePatchRequest(900)))
                .thenReturn(new Service("Coaching", 900));

        mockMvc.perform(patch("/services/Coaching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sekPerHour\":900}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coaching"))
                .andExpect(jsonPath("$.sekPerHour").value(900));

        verify(serviceCatalogService)
                .patch("Coaching", new ServiceCatalogService.ServicePatchRequest(900));
    }

    @Test
    void patchReturnsNotFoundWhenServiceDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(serviceCatalogService)
                .patch("Missing", new ServiceCatalogService.ServicePatchRequest(900));

        mockMvc.perform(patch("/services/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sekPerHour\":900}"))
                .andExpect(status().isNotFound());

        verify(serviceCatalogService)
                .patch("Missing", new ServiceCatalogService.ServicePatchRequest(900));
    }

    @Test
    void deleteReturnsNoContentWhenServiceExists() throws Exception {
        mockMvc.perform(delete("/services/Coaching")).andExpect(status().isNoContent());

        verify(serviceCatalogService).delete("Coaching");
    }

    @Test
    void deleteReturnsNotFoundWhenServiceDoesNotExist() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND))
                .when(serviceCatalogService)
                .delete("Missing");

        mockMvc.perform(delete("/services/Missing")).andExpect(status().isNotFound());

        verify(serviceCatalogService).delete("Missing");
    }
}
