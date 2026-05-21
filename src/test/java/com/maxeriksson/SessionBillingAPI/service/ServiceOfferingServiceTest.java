package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.repository.ServiceOfferingRepository;
import com.maxeriksson.SessionBillingAPI.repository.ServiceOfferingVersionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Unit tests for service offering creation and versioning. */
@ExtendWith(MockitoExtension.class)
class ServiceOfferingServiceTest {

    @Mock private ServiceOfferingRepository serviceOfferingRepository;
    @Mock private ServiceOfferingVersionRepository serviceOfferingVersionRepository;

    @InjectMocks private ServiceOfferingService serviceOfferingService;

    @Test
    void createReturnsInitialVersionWhenNameIsAvailable() {
        when(serviceOfferingRepository.existsByName("Coaching")).thenReturn(false);
        when(serviceOfferingRepository.save(any(ServiceOffering.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(serviceOfferingVersionRepository.save(any(ServiceOfferingVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOfferingVersion created =
                serviceOfferingService.create(
                        new ServiceOfferingService.ServiceOfferingCreateRequest(
                                "Coaching", new BigDecimal("500.00"), "SEK"));

        assertEquals(1, created.getVersionNumber());
        assertEquals(new BigDecimal("500.00"), created.getHourlyChargeAmount());
        assertEquals("SEK", created.getCurrencyCode());
        assertEquals(true, created.isCurrentVersion());
        verify(serviceOfferingRepository).existsByName("Coaching");
        verify(serviceOfferingRepository).save(any(ServiceOffering.class));
        verify(serviceOfferingVersionRepository).save(any(ServiceOfferingVersion.class));
    }

    @Test
    void createThrowsConflictWhenServiceOfferingAlreadyExists() {
        when(serviceOfferingRepository.existsByName("Coaching")).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                serviceOfferingService.create(
                                        new ServiceOfferingService.ServiceOfferingCreateRequest(
                                                "Coaching", new BigDecimal("500.00"), "SEK")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(serviceOfferingRepository).existsByName("Coaching");
        verifyNoMoreInteractions(serviceOfferingRepository);
        verifyNoMoreInteractions(serviceOfferingVersionRepository);
    }

    @Test
    void createVersionReturnsNewCurrentVersion() {
        ServiceOffering serviceOffering = new ServiceOffering("Coaching");
        ServiceOfferingVersion currentVersion =
                new ServiceOfferingVersion(
                        serviceOffering,
                        1,
                        new BigDecimal("500.00"),
                        "SEK",
                        true);

        when(serviceOfferingRepository.findByName("Coaching")).thenReturn(Optional.of(serviceOffering));
        when(serviceOfferingVersionRepository.findFirstByServiceOfferingAndCurrentVersionTrue(serviceOffering))
                .thenReturn(Optional.of(currentVersion));
        when(serviceOfferingVersionRepository.findByServiceOfferingOrderByVersionNumberDesc(serviceOffering))
                .thenReturn(List.of(currentVersion));
        when(serviceOfferingVersionRepository.save(any(ServiceOfferingVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOfferingVersion created =
                serviceOfferingService.createVersion(
                        "Coaching",
                        new ServiceOfferingService.ServiceOfferingVersionRequest(
                                new BigDecimal("650.00"), "SEK"));

        assertEquals(2, created.getVersionNumber());
        assertEquals(new BigDecimal("650.00"), created.getHourlyChargeAmount());
        assertEquals("SEK", created.getCurrencyCode());
        assertEquals(true, created.isCurrentVersion());
        assertEquals(false, currentVersion.isCurrentVersion());
        verify(serviceOfferingRepository).findByName("Coaching");
        verify(serviceOfferingVersionRepository)
                .findFirstByServiceOfferingAndCurrentVersionTrue(serviceOffering);
        verify(serviceOfferingVersionRepository)
                .findByServiceOfferingOrderByVersionNumberDesc(serviceOffering);
        verify(serviceOfferingVersionRepository).save(currentVersion);
        verify(serviceOfferingVersionRepository, times(2))
                .save(any(ServiceOfferingVersion.class));
    }

    @Test
    void createVersionThrowsNotFoundWhenServiceOfferingDoesNotExist() {
        when(serviceOfferingRepository.findByName("Missing")).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                serviceOfferingService.createVersion(
                                        "Missing",
                                        new ServiceOfferingService.ServiceOfferingVersionRequest(
                                                new BigDecimal("650.00"), "SEK")));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(serviceOfferingRepository).findByName("Missing");
        verifyNoMoreInteractions(serviceOfferingRepository);
        verifyNoMoreInteractions(serviceOfferingVersionRepository);
    }
}
