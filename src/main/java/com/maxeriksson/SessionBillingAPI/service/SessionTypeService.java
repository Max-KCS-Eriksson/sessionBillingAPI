package com.maxeriksson.SessionBillingAPI.service;

import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.domain.ServiceOfferingVersion;
import com.maxeriksson.SessionBillingAPI.repository.SessionTypeRepository;
import com.maxeriksson.SessionBillingAPI.repository.SessionTypeVersionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Service-layer entry point for session type workflows.
 *
 * <p>The class owns session type creation and versioning, including resolution of the exact
 * service offering version used by each session type version.
 */
@org.springframework.stereotype.Service
@Transactional
public class SessionTypeService {

    private final SessionTypeRepository sessionTypeRepository;
    private final SessionTypeVersionRepository sessionTypeVersionRepository;
    private final ServiceOfferingService serviceOfferingService;

    /**
     * Creates a session type service backed by the existing repositories.
     *
     * @param sessionTypeRepository persistence boundary for session types
     * @param sessionTypeVersionRepository persistence boundary for session type versions
     * @param serviceOfferingService service layer used to resolve service offering versions
     */
    public SessionTypeService(
            SessionTypeRepository sessionTypeRepository,
            SessionTypeVersionRepository sessionTypeVersionRepository,
            ServiceOfferingService serviceOfferingService) {
        this.sessionTypeRepository = sessionTypeRepository;
        this.sessionTypeVersionRepository = sessionTypeVersionRepository;
        this.serviceOfferingService = serviceOfferingService;
    }

    /**
     * Creates a new session type together with its first version.
     *
     * @param request session type creation payload
     * @return the created current version
     */
    public SessionTypeVersion create(SessionTypeCreateRequest request) {
        if (sessionTypeRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session type already exists");
        }

        ServiceOfferingVersion serviceOfferingVersion =
                serviceOfferingService
                        .findCurrentVersion(request.serviceOfferingName())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Service offering version not found"));

        SessionType sessionType = sessionTypeRepository.save(new SessionType(request.name()));
        SessionTypeVersion version =
                new SessionTypeVersion(
                        sessionType,
                        serviceOfferingVersion,
                        1,
                        request.durationMinutes(),
                        true);
        return sessionTypeVersionRepository.save(version);
    }

    /**
     * Creates a new version for an existing session type.
     *
     * @param name unique session type name
     * @param request version payload
     * @return the newly created current version
     */
    public SessionTypeVersion createVersion(String name, SessionTypeVersionRequest request) {
        SessionType sessionType =
                sessionTypeRepository
                        .findByName(name)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ServiceOfferingVersion serviceOfferingVersion =
                serviceOfferingService
                        .findCurrentVersion(request.serviceOfferingName())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Service offering version not found"));

        Optional<SessionTypeVersion> currentVersion =
                sessionTypeVersionRepository.findFirstBySessionTypeAndCurrentVersionTrue(sessionType);
        currentVersion.ifPresent(
                version -> {
                    version.setCurrentVersion(false);
                    sessionTypeVersionRepository.save(version);
                });

        int nextVersionNumber =
                sessionTypeVersionRepository
                        .findBySessionTypeOrderByVersionNumberDesc(sessionType)
                        .stream()
                        .findFirst()
                        .map(SessionTypeVersion::getVersionNumber)
                        .orElse(0)
                        + 1;

        SessionTypeVersion version =
                new SessionTypeVersion(
                        sessionType,
                        serviceOfferingVersion,
                        nextVersionNumber,
                        request.durationMinutes(),
                        true);
        return sessionTypeVersionRepository.save(version);
    }

    /**
     * Request payload for creating a new session type.
     *
     * @param name unique session type name
     * @param durationMinutes session duration in minutes
     * @param serviceOfferingName service offering name to resolve the current offering version
     */
    public record SessionTypeCreateRequest(
            String name, int durationMinutes, String serviceOfferingName) {}

    /**
     * Request payload for creating a new session type version.
     *
     * @param durationMinutes session duration in minutes
     * @param serviceOfferingName service offering name to resolve the current offering version
     */
    public record SessionTypeVersionRequest(int durationMinutes, String serviceOfferingName) {}
}
