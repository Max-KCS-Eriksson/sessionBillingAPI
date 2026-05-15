package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.repository.BillRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST controller exposing the current bill registry. */
@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillRepository billRepository;

    /**
     * Creates a bill registry controller backed by the existing repository.
     *
     * @param billRepository persistence boundary for bill records
     */
    public BillController(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * Lists all registered bills.
     *
     * @return all bill records currently persisted
     */
    @GetMapping
    public List<Bill> findAll() {
        return billRepository.findAll();
    }
}
