package com.maxeriksson.SessionBillingAPI.repository;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository for legacy bill records. */
public interface BillRepository extends JpaRepository<Bill, Long> {

    /**
     * Finds a bill by its natural key.
     *
     * @param billId legacy bill identifier
     * @return matching bill if it exists
     */
    Optional<Bill> findByBillId(BillId billId);

    /**
     * Checks whether a bill already exists for the provided natural key.
     *
     * @param billId legacy bill identifier
     * @return true when the bill exists
     */
    boolean existsByBillId(BillId billId);
}
