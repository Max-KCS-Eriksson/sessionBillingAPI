package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;

import org.springframework.data.jpa.repository.JpaRepository;

/** BillRepository */
public interface BillRepository extends JpaRepository<Bill, BillId> {}
