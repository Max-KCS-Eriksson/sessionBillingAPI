package com.maxeriksson.SessionBillingAPI.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Service offering aggregate root.
 *
 * <p>The entity represents the named billable offering that version records attach to.
 */
@Entity
@Table(name = "service_offerings")
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public ServiceOffering() {}

    /**
     * Creates a service offering with a unique name.
     *
     * @param name unique service offering name
     */
    public ServiceOffering(String name) {
        setName(name);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /**
     * Updates the service offering name.
     *
     * @param name unique service offering name
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Service offering name is required");
        }
        this.name = name;
    }
}
