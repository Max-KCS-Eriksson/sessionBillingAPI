package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.model.Service;

import org.springframework.data.jpa.repository.JpaRepository;

/** ServiceRepository */
public interface ServiceRepository extends JpaRepository<Service, String> {}
