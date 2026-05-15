package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

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

    @MockBean private ServiceRepository serviceRepository;

    @Test
    void findAllReturnsServices() throws Exception {
        when(serviceRepository.findAll()).thenReturn(List.of(new Service("Coaching", 500)));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Coaching"))
                .andExpect(jsonPath("$[0].sekPerHour").value(500));

        verify(serviceRepository).findAll();
    }

    @Test
    void findByNameReturnsService() throws Exception {
        when(serviceRepository.findById("Coaching"))
                .thenReturn(Optional.of(new Service("Coaching", 500)));

        mockMvc.perform(get("/services/Coaching"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coaching"))
                .andExpect(jsonPath("$.sekPerHour").value(500));

        verify(serviceRepository).findById("Coaching");
    }

    @Test
    void findByNameReturnsNotFound() throws Exception {
        when(serviceRepository.findById("Missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/services/Missing")).andExpect(status().isNotFound());

        verify(serviceRepository).findById("Missing");
    }

    @Test
    void createReturnsCreatedWhenServiceDoesNotExist() throws Exception {
        when(serviceRepository.existsById("Coaching")).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(new Service("Coaching", 500));

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coaching\",\"sekPerHour\":500}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Coaching"))
                .andExpect(jsonPath("$.sekPerHour").value(500));

        verify(serviceRepository).existsById("Coaching");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void createReturnsConflictWhenServiceAlreadyExists() throws Exception {
        when(serviceRepository.existsById("Coaching")).thenReturn(true);

        mockMvc.perform(post("/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Coaching\",\"sekPerHour\":500}"))
                .andExpect(status().isConflict());

        verify(serviceRepository).existsById("Coaching");
    }

    @Test
    void replaceReturnsOkWhenServiceExists() throws Exception {
        Service existingService = new Service("Coaching", 500);
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(existingService));
        when(serviceRepository.save(any(Service.class))).thenReturn(new Service("Advanced", 700));

        mockMvc.perform(put("/services/Coaching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Advanced\",\"sekPerHour\":700}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Advanced"))
                .andExpect(jsonPath("$.sekPerHour").value(700));

        verify(serviceRepository).findById("Coaching");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void replaceReturnsNotFoundWhenServiceDoesNotExist() throws Exception {
        when(serviceRepository.findById("Missing")).thenReturn(Optional.empty());

        mockMvc.perform(put("/services/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Advanced\",\"sekPerHour\":700}"))
                .andExpect(status().isNotFound());

        verify(serviceRepository).findById("Missing");
    }

    @Test
    void patchReturnsOkWhenServiceExists() throws Exception {
        Service existingService = new Service("Coaching", 500);
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(existingService));
        when(serviceRepository.save(any(Service.class))).thenReturn(new Service("Coaching", 900));

        mockMvc.perform(patch("/services/Coaching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sekPerHour\":900}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coaching"))
                .andExpect(jsonPath("$.sekPerHour").value(900));

        verify(serviceRepository).findById("Coaching");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void patchReturnsNotFoundWhenServiceDoesNotExist() throws Exception {
        when(serviceRepository.findById("Missing")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/services/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sekPerHour\":900}"))
                .andExpect(status().isNotFound());

        verify(serviceRepository).findById("Missing");
    }
}
