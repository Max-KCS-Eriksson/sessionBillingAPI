package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;
import com.maxeriksson.SessionBillingAPI.service.SessionTypeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller exposing session type creation and versioning. */
@RestController
@RequestMapping("/session-types")
public class SessionTypeController {

    private final SessionTypeService sessionTypeService;

    /**
     * Creates a session type controller backed by the service layer.
     *
     * @param sessionTypeService service-layer boundary for session types
     */
    public SessionTypeController(SessionTypeService sessionTypeService) {
        this.sessionTypeService = sessionTypeService;
    }

    /**
     * Creates a new session type together with its first version.
     *
     * @param request creation payload
     * @return the created current version
     */
    @PostMapping
    public ResponseEntity<SessionTypeVersion> create(@RequestBody SessionTypeCreateRequest request) {
        SessionTypeVersion createdVersion =
                sessionTypeService.create(
                        new SessionTypeService.SessionTypeCreateRequest(
                                request.name(), request.durationMinutes(), request.serviceOfferingName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
    }

    /**
     * Creates a new version for an existing session type.
     *
     * @param name session type name from the request path
     * @param request version payload
     * @return the newly created current version
     */
    @PostMapping("/{name}/versions")
    public ResponseEntity<SessionTypeVersion> createVersion(
            @PathVariable String name, @RequestBody SessionTypeVersionRequest request) {
        SessionTypeVersion createdVersion =
                sessionTypeService.createVersion(
                        name,
                        new SessionTypeService.SessionTypeVersionRequest(
                                request.durationMinutes(), request.serviceOfferingName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
    }

    /**
     * Request payload for creating a session type.
     *
     * @param name unique session type name
     * @param durationMinutes session duration in minutes
     * @param serviceOfferingName service offering name to resolve the current offering version
     */
    public record SessionTypeCreateRequest(
            String name, int durationMinutes, String serviceOfferingName) {}

    /**
     * Request payload for creating a session type version.
     *
     * @param durationMinutes session duration in minutes
     * @param serviceOfferingName service offering name to resolve the current offering version
     */
    public record SessionTypeVersionRequest(int durationMinutes, String serviceOfferingName) {}
}
