package com.maxeriksson.SessionBillingAPI.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Customer */
@Entity
@Table(name = "customers")
public class Customer {

    @EmbeddedId private PersonalId personalId;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "address")
    private String address;

    public Customer() {} // Required by JPA

    public Customer(
            PersonalId personalId,
            String firstName,
            String lastName,
            String address) {
        this.personalId = personalId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public PersonalId getPersonalId() {
        return personalId;
    }

    public void setPersonalId(PersonalId personalId) {
        this.personalId = personalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer [id="
                + personalId
                + ", "
                + firstName
                + " "
                + lastName
                + ", "
                + address
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((personalId == null) ? 0 : personalId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Customer other = (Customer) obj;
        if (personalId == null) {
            if (other.personalId != null) return false;
        } else if (!personalId.equals(other.personalId)) return false;
        return true;
    }
}
