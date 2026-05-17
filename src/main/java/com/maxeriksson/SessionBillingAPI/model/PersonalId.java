package com.maxeriksson.SessionBillingAPI.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;

/** PersonalId */
@Embeddable
public class PersonalId implements Serializable {

    @Column(name = "dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "idLastFour")
    private String idLastFour;

    public PersonalId() {} // Required by JPA

    public PersonalId(LocalDate dateOfBirth, Integer idLastFour) {
        this.dateOfBirth = dateOfBirth;
        setIdLastFour(idLastFour);
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdLastFour() {
        return idLastFour;
    }

    public void setIdLastFour(Integer idLastFour) {
        if (idLastFour < 0 || idLastFour > 9999) {
            throw new IllegalArgumentException();
        }
        this.idLastFour = String.format("%04d", idLastFour);
    }

    @Override
    public String toString() {
        return dateOfBirth.toString().replace("-", "") + "-" + idLastFour;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dateOfBirth == null) ? 0 : dateOfBirth.hashCode());
        result = prime * result + ((idLastFour == null) ? 0 : idLastFour.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PersonalId other = (PersonalId) obj;
        if (dateOfBirth == null) {
            if (other.dateOfBirth != null) return false;
        } else if (!dateOfBirth.equals(other.dateOfBirth)) return false;
        if (idLastFour == null) {
            if (other.idLastFour != null) return false;
        } else if (!idLastFour.equals(other.idLastFour)) return false;
        return true;
    }
}
