package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.repository.SessionTypeRepository;
import com.maxeriksson.SessionBillingAPI.repository.SessionTypeVersionRepository;

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

/** Unit tests for session type creation and versioning. */
@ExtendWith(MockitoExtension.class)
class SessionTypeServiceTest {

    @Mock private SessionTypeRepository sessionTypeRepository;
    @Mock private SessionTypeVersionRepository sessionTypeVersionRepository;
    @Mock private ServiceOfferingService serviceOfferingService;

    @InjectMocks private SessionTypeService sessionTypeService;

    @Test
    void findCurrentVersionReturnsVersion() {
        SessionType sessionType = new SessionType("GroupSession");
        SessionTypeVersion currentVersion =
                new SessionTypeVersion(
                        sessionType,
                        new ServiceOfferingVersion(
                                new ServiceOffering("Coaching"),
                                1,
                                new BigDecimal("500.00"),
                                "SEK",
                                true),
                        1,
                        90,
                        true);

        when(sessionTypeRepository.findByName("GroupSession")).thenReturn(Optional.of(sessionType));
        when(sessionTypeVersionRepository.findFirstBySessionTypeAndCurrentVersionTrue(sessionType))
                .thenReturn(Optional.of(currentVersion));

        Optional<SessionTypeVersion> result = sessionTypeService.findCurrentVersion("GroupSession");

        assertEquals(Optional.of(currentVersion), result);
        verify(sessionTypeRepository).findByName("GroupSession");
        verify(sessionTypeVersionRepository)
                .findFirstBySessionTypeAndCurrentVersionTrue(sessionType);
    }

    @Test
    void createReturnsInitialVersionWhenSessionTypeNameIsAvailable() {
        ServiceOfferingVersion serviceOfferingVersion =
                new ServiceOfferingVersion(
                        new ServiceOffering("Coaching"),
                        1,
                        new BigDecimal("500.00"),
                        "SEK",
                        true);
        when(sessionTypeRepository.existsByName("GroupSession")).thenReturn(false);
        when(serviceOfferingService.findCurrentVersion("Coaching"))
                .thenReturn(Optional.of(serviceOfferingVersion));
        when(sessionTypeRepository.save(any(SessionType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionTypeVersionRepository.save(any(SessionTypeVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionTypeVersion created =
                sessionTypeService.create(
                        new SessionTypeService.SessionTypeCreateRequest(
                                "GroupSession", 90, "Coaching"));

        assertEquals(1, created.getVersionNumber());
        assertEquals(90, created.getDurationMinutes());
        assertEquals(serviceOfferingVersion, created.getServiceOfferingVersion());
        assertEquals(true, created.isCurrentVersion());
        verify(sessionTypeRepository).existsByName("GroupSession");
        verify(serviceOfferingService).findCurrentVersion("Coaching");
        verify(sessionTypeRepository).save(any(SessionType.class));
        verify(sessionTypeVersionRepository).save(any(SessionTypeVersion.class));
    }

    @Test
    void createThrowsConflictWhenSessionTypeAlreadyExists() {
        when(sessionTypeRepository.existsByName("GroupSession")).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                sessionTypeService.create(
                                        new SessionTypeService.SessionTypeCreateRequest(
                                                "GroupSession", 90, "Coaching")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(sessionTypeRepository).existsByName("GroupSession");
        verifyNoMoreInteractions(sessionTypeRepository);
        verifyNoMoreInteractions(sessionTypeVersionRepository);
        verifyNoMoreInteractions(serviceOfferingService);
    }

    @Test
    void createVersionReturnsNewCurrentVersion() {
        SessionType sessionType = new SessionType("GroupSession");
        SessionTypeVersion currentVersion =
                new SessionTypeVersion(
                        sessionType,
                        new ServiceOfferingVersion(
                                new ServiceOffering("Coaching"),
                                1,
                                new BigDecimal("500.00"),
                                "SEK",
                                true),
                        1,
                        90,
                        true);
        ServiceOfferingVersion updatedServiceOfferingVersion =
                new ServiceOfferingVersion(
                        new ServiceOffering("Coaching"),
                        2,
                        new BigDecimal("650.00"),
                        "SEK",
                        true);

        when(sessionTypeRepository.findByName("GroupSession")).thenReturn(Optional.of(sessionType));
        when(serviceOfferingService.findCurrentVersion("Coaching"))
                .thenReturn(Optional.of(updatedServiceOfferingVersion));
        when(sessionTypeVersionRepository.findFirstBySessionTypeAndCurrentVersionTrue(sessionType))
                .thenReturn(Optional.of(currentVersion));
        when(sessionTypeVersionRepository.findBySessionTypeOrderByVersionNumberDesc(sessionType))
                .thenReturn(List.of(currentVersion));
        when(sessionTypeVersionRepository.save(any(SessionTypeVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionTypeVersion created =
                sessionTypeService.createVersion(
                        "GroupSession",
                        new SessionTypeService.SessionTypeVersionRequest(60, "Coaching"));

        assertEquals(2, created.getVersionNumber());
        assertEquals(60, created.getDurationMinutes());
        assertEquals(updatedServiceOfferingVersion, created.getServiceOfferingVersion());
        assertEquals(true, created.isCurrentVersion());
        assertEquals(false, currentVersion.isCurrentVersion());
        verify(sessionTypeRepository).findByName("GroupSession");
        verify(serviceOfferingService).findCurrentVersion("Coaching");
        verify(sessionTypeVersionRepository)
                .findFirstBySessionTypeAndCurrentVersionTrue(sessionType);
        verify(sessionTypeVersionRepository)
                .findBySessionTypeOrderByVersionNumberDesc(sessionType);
        verify(sessionTypeVersionRepository).save(currentVersion);
        verify(sessionTypeVersionRepository, times(2)).save(any(SessionTypeVersion.class));
    }

    @Test
    void createVersionThrowsNotFoundWhenSessionTypeDoesNotExist() {
        when(sessionTypeRepository.findByName("Missing")).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                sessionTypeService.createVersion(
                                        "Missing",
                                        new SessionTypeService.SessionTypeVersionRequest(
                                                60, "Coaching")));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(sessionTypeRepository).findByName("Missing");
        verifyNoMoreInteractions(sessionTypeRepository);
        verifyNoMoreInteractions(sessionTypeVersionRepository);
        verifyNoMoreInteractions(serviceOfferingService);
    }
}
