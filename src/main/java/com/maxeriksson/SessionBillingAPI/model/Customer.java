package com.maxeriksson.SessionBillingAPI.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Customer */
@Entity
@Table(name = "customers")
public class Customer {

    @EmbeddedId private SocialSecurityNumber socialSecurityNumber;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "address")
    private String address;

    public Customer() {} // Required by JPA

    public Customer(
            SocialSecurityNumber socialSecurityNumber,
            String firstName,
            String lastName,
            String address) {
        this.socialSecurityNumber = socialSecurityNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public SocialSecurityNumber getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(SocialSecurityNumber socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
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
                + socialSecurityNumber
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
                        + ((socialSecurityNumber == null) ? 0 : socialSecurityNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Customer other = (Customer) obj;
        if (socialSecurityNumber == null) {
            if (other.socialSecurityNumber != null) return false;
        } else if (!socialSecurityNumber.equals(other.socialSecurityNumber)) return false;
        return true;
    }
}
