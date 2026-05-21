package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;
import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.repository.BillRepository;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Unit tests for the bill service layer. */
@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock private BillRepository billRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ServiceRepository serviceRepository;

    @InjectMocks private BillService billService;

    @Test
    void findAllReturnsBills() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Bill bill =
                new Bill(
                        new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0)),
                        new Service("Coaching", 500),
                        2,
                        false);
        when(billRepository.findAll()).thenReturn(List.of(bill));

        List<Bill> bills = billService.findAll();

        assertEquals(1, bills.size());
        assertEquals(2, bills.get(0).getHours());
        verify(billRepository).findAll();
    }

    @Test
    void createReturnsCreatedBillWhenIdentifiersExist() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service service = new Service("Coaching", 500);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));

        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(customer));
        when(serviceRepository.findById("Coaching")).thenReturn(Optional.of(service));
        when(billRepository.existsById(any(BillId.class))).thenReturn(false);
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bill created =
                billService.create(
                        new BillService.BillCreateRequest(
                                "19900102-0123",
                                LocalDateTime.of(2026, 1, 1, 10, 0),
                                "Coaching",
                                2,
                                false));

        assertEquals(id, created.getId());
        assertEquals(service, created.getService());
        assertEquals(2, created.getHours());
        assertEquals(false, created.isPaid());
        verify(customerRepository).findById(any(PersonalId.class));
        verify(serviceRepository).findById("Coaching");
        verify(billRepository).existsById(any(BillId.class));
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void createThrowsConflictWhenBillAlreadyExists() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(customer));
        when(serviceRepository.findById("Coaching"))
                .thenReturn(Optional.of(new Service("Coaching", 500)));
        when(billRepository.existsById(any(BillId.class))).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                billService.create(
                                        new BillService.BillCreateRequest(
                                                "19900102-0123",
                                                LocalDateTime.of(2026, 1, 1, 10, 0),
                                                "Coaching",
                                                2,
                                                false)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(customerRepository).findById(any(PersonalId.class));
        verify(serviceRepository).findById("Coaching");
        verify(billRepository).existsById(any(BillId.class));
        verifyNoMoreInteractions(billRepository);
    }

    @Test
    void replaceUpdatesExistingBill() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service existingService = new Service("Coaching", 500);
        Service replacementService = new Service("Advanced", 700);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill existingBill = new Bill(id, existingService, 2, false);

        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.of(existingBill));
        when(serviceRepository.findById("Advanced")).thenReturn(Optional.of(replacementService));
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bill updated =
                billService.replace(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillReplaceRequest("Advanced", 4, true));

        assertEquals(replacementService, updated.getService());
        assertEquals(4, updated.getHours());
        assertEquals(true, updated.isPaid());
        verify(customerRepository).findById(any(PersonalId.class));
        verify(billRepository).findById(any(BillId.class));
        verify(serviceRepository).findById("Advanced");
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void patchUpdatesExistingBillPartially() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        Service existingService = new Service("Coaching", 500);
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill existingBill = new Bill(id, existingService, 2, false);

        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.of(existingBill));
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bill updated =
                billService.patch(
                        "19900102-0123",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        new BillService.BillPatchRequest(null, 3, true));

        assertEquals(existingService, updated.getService());
        assertEquals(3, updated.getHours());
        assertEquals(true, updated.isPaid());
        verify(customerRepository).findById(any(PersonalId.class));
        verify(billRepository).findById(any(BillId.class));
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void deleteRemovesExistingBill() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        BillId id = new BillId(customer, LocalDateTime.of(2026, 1, 1, 10, 0));
        Bill existingBill = new Bill(id, new Service("Coaching", 500), 2, false);

        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(customer));
        when(billRepository.findById(any(BillId.class))).thenReturn(Optional.of(existingBill));

        billService.delete("19900102-0123", LocalDateTime.of(2026, 1, 1, 10, 0));

        verify(customerRepository).findById(any(PersonalId.class));
        verify(billRepository).findById(any(BillId.class));
        verify(billRepository).delete(existingBill);
    }

    @Test
    void createThrowsBadRequestWhenPersonalIdIsMalformed() {
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                billService.create(
                                        new BillService.BillCreateRequest(
                                                "invalid",
                                                LocalDateTime.of(2026, 1, 1, 10, 0),
                                                "Coaching",
                                                2,
                                                false)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoMoreInteractions(customerRepository, serviceRepository, billRepository);
    }
}
