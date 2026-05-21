package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.domain.Invoice;
import com.maxeriksson.SessionBillingAPI.service.InvoiceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST controller exposing invoice records. */
@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Creates an invoice controller backed by the service layer.
     *
     * @param invoiceService service layer used for invoice workflows
     */
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Lists all stored invoices.
     *
     * @return all invoice records currently persisted
     */
    @GetMapping
    public List<Invoice> findAll() {
        return invoiceService.findAll();
    }

    /**
     * Deletes an invoice when business rules allow it.
     *
     * @param id invoice identifier from the request path
     * @return no content when the invoice is removed
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
