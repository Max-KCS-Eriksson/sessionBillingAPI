package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.domain.ServiceOffering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository for service offering aggregates. */
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    /**
     * Finds one service offering by its unique name.
     *
     * @param name unique service offering name
     * @return matching service offering if it exists
     */
    Optional<ServiceOffering> findByName(String name);

    /**
     * Checks whether a service offering already exists for the given name.
     *
     * @param name unique service offering name
     * @return true when the name is already registered
     */
    boolean existsByName(String name);
}
