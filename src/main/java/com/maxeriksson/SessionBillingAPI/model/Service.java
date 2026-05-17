package com.maxeriksson.SessionBillingAPI.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Service */
@Entity
@Table(name = "services")
public class Service {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "sekPerHour")
    private int sekPerHour;

    public Service() {} // Required by JPA

    public Service(String name, int sekPerHour) {
        setName(name);
        setSekPerHour(sekPerHour);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    public int getSekPerHour() {
        return sekPerHour;
    }

    public void setSekPerHour(int sekPerHour) {
        if (sekPerHour <= 0) {
            throw new IllegalArgumentException("Invalid input - must be above 0. Try again.");
        }
        this.sekPerHour = sekPerHour;
    }

    @Override
    public String toString() {
        return "Service [" + name + ", sekPerHour=" + sekPerHour + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Service other = (Service) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }
}
