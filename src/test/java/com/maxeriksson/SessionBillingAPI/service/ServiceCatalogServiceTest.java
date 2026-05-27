package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/** Unit tests for the service catalog service layer. */
@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {

    @Mock private ServiceRepository serviceRepository;

    @InjectMocks private ServiceCatalogService serviceCatalogService;

    @Test
    void findAllReturnsServices() {
        when(serviceRepository.findAll()).thenReturn(List.of(new Service("Coaching", 500)));

        List<Service> services = serviceCatalogService.findAll();

        assertEquals(1, services.size());
        assertEquals("Coaching", services.get(0).getName());
        assertEquals(500, services.get(0).getSekPerHour());
        verify(serviceRepository).findAll();
    }

    @Test
    void findByNameReturnsService() {
        Service service = new Service("Coaching", 500);
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(service));

        Optional<Service> result = serviceCatalogService.findByName("Coaching");

        assertEquals(Optional.of(service), result);
        verify(serviceRepository).findById("Coaching");
    }

    @Test
    void createReturnsCreatedServiceWhenNameIsAvailable() {
        when(serviceRepository.existsById("Coaching")).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Service created = serviceCatalogService.create(
                new ServiceCatalogService.ServiceRequest("Coaching", 500));

        assertEquals("Coaching", created.getName());
        assertEquals(500, created.getSekPerHour());
        verify(serviceRepository).existsById("Coaching");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void createThrowsConflictWhenServiceAlreadyExists() {
        when(serviceRepository.existsById("Coaching")).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                serviceCatalogService.create(
                                        new ServiceCatalogService.ServiceRequest("Coaching", 500)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(serviceRepository).existsById("Coaching");
        verifyNoMoreInteractions(serviceRepository);
    }

    @Test
    void replaceUpdatesExistingService() {
        Service existingService = new Service("Coaching", 500);
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(existingService));
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Service updated =
                serviceCatalogService.replace(
                        "Coaching", new ServiceCatalogService.ServiceRequest("Coaching", 700));

        assertEquals("Coaching", updated.getName());
        assertEquals(700, updated.getSekPerHour());
        verify(serviceRepository).findById("Coaching");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void patchUpdatesHourlyRateForExistingService() {
        Service existingService = new Service("Coaching", 500);
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(existingService));
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Service updated =
                serviceCatalogService.patch(
                        "Coaching", new ServiceCatalogService.ServicePatchRequest(900));

        assertEquals("Coaching", updated.getName());
        assertEquals(900, updated.getSekPerHour());
        verify(serviceRepository).findById("Coaching");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void deleteRemovesExistingService() {
        Service existingService = new Service("Coaching", 500);
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(existingService));

        serviceCatalogService.delete("Coaching");

        verify(serviceRepository).findById("Coaching");
        verify(serviceRepository).delete(existingService);
    }

    @Test
    void deleteThrowsNotFoundWhenServiceIsMissing() {
        when(serviceRepository.findById("Missing")).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> serviceCatalogService.delete("Missing"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(serviceRepository).findById("Missing");
        verifyNoMoreInteractions(serviceRepository);
    }
}
