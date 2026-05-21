package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.domain.SessionType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository for session type aggregates. */
public interface SessionTypeRepository extends JpaRepository<SessionType, Long> {

    /**
     * Finds one session type by its unique name.
     *
     * @param name unique session type name
     * @return matching session type if it exists
     */
    Optional<SessionType> findByName(String name);

    /**
     * Checks whether a session type already exists for the given name.
     *
     * @param name unique session type name
     * @return true when the name is already registered
     */
    boolean existsByName(String name);
}
