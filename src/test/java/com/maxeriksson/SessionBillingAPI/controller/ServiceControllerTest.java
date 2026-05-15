package com.maxeriksson.SessionBillingAPI.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
}
