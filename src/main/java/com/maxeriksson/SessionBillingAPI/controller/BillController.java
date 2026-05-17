package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;
import com.maxeriksson.SessionBillingAPI.repository.BillRepository;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** REST controller exposing the current bill registry. */
@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Creates a bill registry controller backed by the existing repository.
     *
     * @param billRepository persistence boundary for bill records
     * @param customerRepository persistence boundary for customer records
     * @param serviceRepository persistence boundary for service records
     */
    public BillController(
            BillRepository billRepository,
            CustomerRepository customerRepository,
            ServiceRepository serviceRepository) {
        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
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

    /**
     * Creates a new bill when the customer, service, and bill id are all valid.
     *
     * @param request bill payload to persist
     * @return the created bill
     */
    @PostMapping
    public ResponseEntity<Bill> create(@RequestBody BillCreateRequest request) {
        Customer customer = findCustomer(request.customerPersonalId());
        Service service = findService(request.serviceName());
        BillId id = new BillId(customer, request.bookedTime());

        if (billRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bill already exists");
        }

        Bill createdBill = new Bill(id, service, request.hours(), request.paid());
        return ResponseEntity.status(HttpStatus.CREATED).body(billRepository.save(createdBill));
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
        BillId id = toBillId(customerPersonalId, bookedTime);

        Bill existingBill =
                billRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        existingBill.setService(findService(request.serviceName()));
        existingBill.setHours(request.hours());
        existingBill.setPaid(request.paid());

        return ResponseEntity.ok(billRepository.save(existingBill));
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
        BillId id = toBillId(customerPersonalId, bookedTime);

        Bill existingBill =
                billRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (request.serviceName() != null) {
            existingBill.setService(findService(request.serviceName()));
        }
        if (request.hours() != null) {
            existingBill.setHours(request.hours());
        }
        if (request.paid() != null) {
            existingBill.setPaid(request.paid());
        }

        return ResponseEntity.ok(billRepository.save(existingBill));
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
        BillId id = toBillId(customerPersonalId, bookedTime);

        Bill existingBill =
                billRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        billRepository.delete(existingBill);
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

    /**
     * Resolves a customer by personal id.
     *
     * @param personalId customer identifier from the request
     * @return matching customer record
     */
    private Customer findCustomer(String personalId) {
        PersonalId id = toPersonalId(personalId);
        return customerRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    /**
     * Resolves a service by its unique name.
     *
     * @param name service name from the request
     * @return matching service record
     */
    private Service findService(String name) {
        return serviceRepository
                .findById(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }

    /**
     * Builds a bill id from the path parameters.
     *
     * @param customerPersonalId customer identifier from the request path
     * @param bookedTime bill timestamp from the request path
     * @return resolved bill identifier
     */
    private BillId toBillId(String customerPersonalId, LocalDateTime bookedTime) {
        return new BillId(findCustomer(customerPersonalId), bookedTime);
    }

    /**
     * Parses the legacy personal id format used by the prototype.
     *
     * @param personalId customer identifier from the request
     * @return parsed identifier value object
     */
    private PersonalId toPersonalId(String personalId) {
        String[] parts = personalId.split("-");
        if (parts.length != 2 || parts[0].length() != 8 || parts[1].length() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid personal id");
        }

        try {
            LocalDate dateOfBirth =
                    LocalDate.of(
                            Integer.parseInt(parts[0].substring(0, 4)),
                            Integer.parseInt(parts[0].substring(4, 6)),
                            Integer.parseInt(parts[0].substring(6, 8)));
            Integer idLastFour = Integer.parseInt(parts[1]);
            return new PersonalId(dateOfBirth, idLastFour);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid personal id");
        }
    }
}
