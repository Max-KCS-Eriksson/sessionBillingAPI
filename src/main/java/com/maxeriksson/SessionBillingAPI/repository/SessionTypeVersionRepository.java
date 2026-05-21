package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.domain.SessionType;
import com.maxeriksson.SessionBillingAPI.domain.SessionTypeVersion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repository for historical session type versions. */
public interface SessionTypeVersionRepository extends JpaRepository<SessionTypeVersion, Long> {

    /**
     * Finds the current version for a session type.
     *
     * @param sessionType owning session type
     * @return current version if one exists
     */
    Optional<SessionTypeVersion> findFirstBySessionTypeAndCurrentVersionTrue(SessionType sessionType);

    /**
     * Lists versions for a session type with newest entries first.
     *
     * @param sessionType owning session type
     * @return versions ordered by version number descending
     */
    List<SessionTypeVersion> findBySessionTypeOrderByVersionNumberDesc(SessionType sessionType);
}
