package com.maxeriksson.SessionBillingAPI.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Session type aggregate root.
 *
 * <p>The entity represents the named session category that version records attach to.
 */
@Entity
@Table(name = "session_types")
public class SessionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public SessionType() {}

    /**
     * Creates a session type with a unique name.
     *
     * @param name unique session type name
     */
    public SessionType(String name) {
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
     * Updates the session type name.
     *
     * @param name unique session type name
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Session type name is required");
        }
        this.name = name;
    }
}
