package com.maxeriksson.SessionBillingAPI.controller;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.SocialSecurityNumber;
import com.maxeriksson.SessionBillingAPI.repository.BillRepository;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * Deletes an existing bill record.
     *
     * @param customerSocialSecurityNumber customer identifier from the request path
     * @param bookedTime bill timestamp from the request path
     * @return no content when the bill is removed
     */
    @DeleteMapping("/{customerSocialSecurityNumber}/{bookedTime}")
    public ResponseEntity<Void> delete(
            @PathVariable String customerSocialSecurityNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bookedTime) {
        BillId id = new BillId(toCustomer(customerSocialSecurityNumber), bookedTime);

        Bill existingBill =
                billRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        billRepository.delete(existingBill);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converts the legacy social security number path segment into a customer reference.
     *
     * @param socialSecurityNumber customer identifier from the request
     * @return customer value object used in the bill id
     */
    private Customer toCustomer(String socialSecurityNumber) {
        return new Customer(toSocialSecurityNumber(socialSecurityNumber), null, null, null);
    }

    /**
     * Parses the legacy social security number format used by the prototype.
     *
     * @param socialSecurityNumber customer identifier from the request
     * @return parsed identifier value object
     */
    private SocialSecurityNumber toSocialSecurityNumber(String socialSecurityNumber) {
        String[] parts = socialSecurityNumber.split("-");
        if (parts.length != 2 || parts[0].length() != 8 || parts[1].length() != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid social security number");
        }

        try {
            LocalDate dateOfBirth =
                    LocalDate.of(
                            Integer.parseInt(parts[0].substring(0, 4)),
                            Integer.parseInt(parts[0].substring(4, 6)),
                            Integer.parseInt(parts[0].substring(6, 8)));
            Integer idLastFour = Integer.parseInt(parts[1]);
            return new SocialSecurityNumber(dateOfBirth, idLastFour);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid social security number");
        }
    }
}
