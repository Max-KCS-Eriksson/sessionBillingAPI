package com.maxeriksson.SessionBillingAPI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.PersonalId;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Unit tests for the customer service layer. */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private CustomerService customerService;

    @Test
    void findAllReturnsCustomers() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> customers = customerService.findAll();

        assertEquals(1, customers.size());
        assertEquals("Ada", customers.get(0).getFirstName());
        verify(customerRepository).findAll();
    }

    @Test
    void findByPersonalIdReturnsCustomer() {
        Customer customer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findByPersonalId("19900102-0123");

        assertEquals(Optional.of(customer), result);
        verify(customerRepository).findById(any(PersonalId.class));
    }

    @Test
    void findByPersonalIdThrowsBadRequestWhenPersonalIdIsMalformed() {
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerService.findByPersonalId("invalid"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void createReturnsCreatedCustomerWhenPersonalIdIsAvailable() {
        when(customerRepository.existsById(any(PersonalId.class))).thenReturn(false);
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer created =
                customerService.create(
                        new CustomerService.CustomerCreateRequest(
                                LocalDate.of(1990, 1, 2),
                                123,
                                "Ada",
                                "Lovelace",
                                "Example Street"));

        assertEquals("Ada", created.getFirstName());
        verify(customerRepository).existsById(any(PersonalId.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createThrowsConflictWhenCustomerAlreadyExists() {
        when(customerRepository.existsById(any(PersonalId.class))).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                customerService.create(
                                        new CustomerService.CustomerCreateRequest(
                                                LocalDate.of(1990, 1, 2),
                                                123,
                                                "Ada",
                                                "Lovelace",
                                                "Example Street")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(customerRepository).existsById(any(PersonalId.class));
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void replaceUpdatesExistingCustomer() {
        Customer existingCustomer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer updated =
                customerService.replace(
                        "19900102-0123",
                        new CustomerService.CustomerReplaceRequest(
                                "Grace", "Hopper", "Navy Street"));

        assertEquals("Grace", updated.getFirstName());
        assertEquals("Hopper", updated.getLastName());
        assertEquals("Navy Street", updated.getAddress());
        verify(customerRepository).findById(any(PersonalId.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void patchUpdatesExistingCustomerPartially() {
        Customer existingCustomer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer updated =
                customerService.patch(
                        "19900102-0123",
                        new CustomerService.CustomerPatchRequest(null, null, "Updated Street"));

        assertEquals("Updated Street", updated.getAddress());
        verify(customerRepository).findById(any(PersonalId.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void deleteRemovesExistingCustomer() {
        Customer existingCustomer =
                new Customer(
                        new PersonalId(LocalDate.of(1990, 1, 2), 123),
                        "Ada",
                        "Lovelace",
                        "Example Street");
        when(customerRepository.findById(any(PersonalId.class))).thenReturn(Optional.of(existingCustomer));

        customerService.delete("19900102-0123");

        verify(customerRepository).findById(any(PersonalId.class));
        verify(customerRepository).delete(existingCustomer);
    }
}
