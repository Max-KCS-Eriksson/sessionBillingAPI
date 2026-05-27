package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;

import org.springframework.data.jpa.repository.JpaRepository;

/** CustomerRepository */
public interface CustomerRepository extends JpaRepository<Customer, PersonalId> {}
