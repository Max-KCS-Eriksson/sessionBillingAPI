package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.service.BillService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** REST controller exposing the current bill registry. */
@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;

    /**
     * Creates a bill registry controller backed by the service layer.
     *
     * @param billService service-layer boundary for bill records
     */
    public BillController(BillService billService) {
        this.billService = billService;
    }

    /**
     * Lists all registered bills.
     *
     * @return all bill records currently persisted
     */
    @GetMapping
    public List<Bill> findAll() {
        return billService.findAll();
    }

    /**
     * Creates a new bill when the customer, service, and bill id are all valid.
     *
     * @param request bill payload to persist
     * @return the created bill
     */
    @PostMapping
    public ResponseEntity<Bill> create(@RequestBody BillCreateRequest request) {
        Bill createdBill =
                billService.create(
                        new BillService.BillCreateRequest(
                                request.customerPersonalId(),
                                request.bookedTime(),
                                request.serviceName(),
                                request.hours(),
                                request.paid()));
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(createdBill);
    }

    /**
     * Fully replaces an existing bill using strict PUT semantics.
     *
     * @param customerPersonalId customer identifier from the request path
     * @param bookedTime bill timestamp from the request path
     * @param request replacement payload
     * @return the replaced bill
     */
    @PutMapping("/{customerPersonalId}/{bookedTime}")
    public ResponseEntity<Bill> replace(
            @PathVariable String customerPersonalId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bookedTime,
            @RequestBody BillReplaceRequest request) {
        Bill updatedBill =
                billService.replace(
                        customerPersonalId,
                        bookedTime,
                        new BillService.BillReplaceRequest(
                                request.serviceName(), request.hours(), request.paid()));
        return ResponseEntity.ok(updatedBill);
    }

    /**
     * Partially updates an existing bill.
     *
     * @param customerPersonalId customer identifier from the request path
     * @param bookedTime bill timestamp from the request path
     * @param request patch payload
     * @return the updated bill
     */
    @PatchMapping("/{customerPersonalId}/{bookedTime}")
    public ResponseEntity<Bill> patch(
            @PathVariable String customerPersonalId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bookedTime,
            @RequestBody BillPatchRequest request) {
        Bill updatedBill =
                billService.patch(
                        customerPersonalId,
                        bookedTime,
                        new BillService.BillPatchRequest(
                                request.serviceName(), request.hours(), request.paid()));
        return ResponseEntity.ok(updatedBill);
    }

    /**
     * Deletes an existing bill record.
     *
     * @param customerPersonalId customer identifier from the request path
     * @param bookedTime bill timestamp from the request path
     * @return no content when the bill is removed
     */
    @DeleteMapping("/{customerPersonalId}/{bookedTime}")
    public ResponseEntity<Void> delete(
            @PathVariable String customerPersonalId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bookedTime) {
        billService.delete(customerPersonalId, bookedTime);
        return ResponseEntity.noContent().build();
    }

    /**
     * Request payload for creating a bill.
     *
     * @param customerPersonalId customer identifier for the bill
     * @param bookedTime bill timestamp
     * @param serviceName service assigned to the bill
     * @param hours billed number of hours
     * @param paid payment status
     */
    public record BillCreateRequest(
            String customerPersonalId,
            LocalDateTime bookedTime,
            String serviceName,
            int hours,
            boolean paid) {}

    /**
     * Request payload for fully replacing a bill.
     *
     * @param serviceName service assigned to the bill
     * @param hours billed number of hours
     * @param paid payment status
     */
    public record BillReplaceRequest(String serviceName, int hours, boolean paid) {}

    /**
     * Request payload for partially updating a bill.
     *
     * @param serviceName service assigned to the bill
     * @param hours billed number of hours
     * @param paid payment status
     */
    public record BillPatchRequest(String serviceName, Integer hours, Boolean paid) {}

}
