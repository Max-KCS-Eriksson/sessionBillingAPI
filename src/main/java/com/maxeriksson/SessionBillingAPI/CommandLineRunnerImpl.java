package com.maxeriksson.SessionBillingAPI;

import com.maxeriksson.SessionBillingAPI.model.Bill;
import com.maxeriksson.SessionBillingAPI.model.BillId;
import com.maxeriksson.SessionBillingAPI.model.Customer;
import com.maxeriksson.SessionBillingAPI.model.Service;
import com.maxeriksson.SessionBillingAPI.model.SocialSecurityNumber;
import com.maxeriksson.SessionBillingAPI.repository.BillRepository;
import com.maxeriksson.SessionBillingAPI.repository.CustomerRepository;
import com.maxeriksson.SessionBillingAPI.repository.ServiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.*;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/** CommandLineRunnerImpl */
public class CommandLineRunnerImpl implements CommandLineRunner {

    private CommandLineInput in = new CommandLineInput();

    @Autowired BillRepository billRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired ServiceRepository serviceRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Welcome to the Session Billing API\n");

        boolean isRunning = true;
        String[] menuChoices = {
            "Handle Billing",
            "Handle Customer Registry",
            "Handle Service Registry",
            "Exit the Session Billing API"
        };
        while (isRunning) {
            printHumanReadableMenuChoiceIndexes(menuChoices);
            int choice = pickListIndex(menuChoices);
            System.out.println();
            switch (choice) {
                case 1 -> {
                    handleBilling();
                }
                case 2 -> {
                    handleCustomers();
                }
                case 3 -> {
                    handleServices();
                }
                case 4 -> isRunning = false;
            }
        }

        System.out.println("Thank you for using the Session Billing API\nGoodbye");
        in.close();
    }

    private void handleBilling() {
        boolean isHandlingBills = true;
        String[] menuChoices = {
            "Show all Bills in Registry",
            "Add Bill to Registry, or Update existing Bill",
            "Delete Bill from Registry",
            "Main Menu"
        };
        while (isHandlingBills) {
            printHumanReadableMenuChoiceIndexes(menuChoices);
            int choice = pickListIndex(menuChoices);
            System.out.println();
            switch (choice) {
                case 1 -> {
                    printAllEntitiesFrom(billRepository, "All Bills in Registry:");
                }
                case 2 -> {
                    Optional<Bill> bill = createBill();
                    if (bill.isPresent()) {
                        billRepository.save(bill.get());
                    }
                }
                case 3 -> deleteBill();

                case 4 -> isHandlingBills = false;
            }

            if (choice == 2 || choice == 3) {
                System.out.println();
            }
        }
    }

    private void handleCustomers() {
        boolean isHandlingCustomers = true;
        String[] menuChoices = {
            "Show all Customers in Registry",
            "Add Customer to Registry, or Update existing Customer",
            "Delete Customer from Registry",
            "Main Menu"
        };
        while (isHandlingCustomers) {
            printHumanReadableMenuChoiceIndexes(menuChoices);
            int choice = pickListIndex(menuChoices);
            System.out.println();
            switch (choice) {
                case 1 -> {
                    printAllEntitiesFrom(customerRepository, "All Customers in Registry:");
                }
                case 2 -> {
                    Optional<Customer> customer = createCustomer();
                    if (customer.isPresent()) {
                        customerRepository.save(customer.get());
                    }
                }
                case 3 -> deleteCustomer();

                case 4 -> isHandlingCustomers = false;
            }

            if (choice == 2 || choice == 3) {
                System.out.println();
            }
        }
    }

    private void handleServices() {
        boolean isHandlingServices = true;
        String[] menuChoices = {
            "Show all Services in Registry",
            "Add Service to Registry, or Update existing Service",
            "Delete Service from Registry",
            "Main Menu"
        };
        while (isHandlingServices) {
            printHumanReadableMenuChoiceIndexes(menuChoices);
            int choice = pickListIndex(menuChoices);
            System.out.println();
            switch (choice) {
                case 1 -> {
                    printAllEntitiesFrom(serviceRepository, "All Services in Registry:");
                }
                case 2 -> {
                    Optional<Service> service = createService();
                    if (service.isPresent()) {
                        serviceRepository.save(service.get());
                    }
                }
                case 3 -> deleteService();

                case 4 -> isHandlingServices = false;
            }

            if (choice == 2 || choice == 3) {
                System.out.println();
            }
        }
    }

    private <E, T> void printAllEntitiesFrom(JpaRepository<E, T> repository, String header) {
        System.out.println(header);
        List<E> entities = repository.findAll();
        if (entities.isEmpty()) {
            System.out.println("  None in Registry");
        }
        for (E entity : entities) {
            System.out.println("  " + entity);
        }
        System.out.println();
    }

    // Handle Bills

    private Optional<Bill> createBill() {
        Bill bill = new Bill();

        Optional<BillId> id = createUniqueBillId();
        try {
            bill.setId(id.get());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }

        boolean isExistingService = false;
        while (!isExistingService) {
            String serviceName = toInitialUpperCase(in.inputString("Service"));
            try {
                bill.setService(serviceRepository.findById(serviceName).get());
                isExistingService = true;
            } catch (NoSuchElementException e) {
                System.out.println("Service not found in the Registry.\n  " + serviceName);
                if (in.inputConfirmation("Register Service?\n")) {
                    Optional<Service> service = createService(serviceName);
                    if (service.isPresent()) {
                        serviceRepository.save(service.get());
                        bill.setService(service.get());
                        isExistingService = true;
                    }
                } else if (!in.inputConfirmation("Try again?")) {
                    return Optional.empty();
                }
            }
        }

        boolean isHoursValid = false;
        while (!isHoursValid) {
            try {
                bill.setHours(in.inputInt("Amount hours"));
                isHoursValid = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return Optional.of(bill);
    }

    private void deleteBill() {
        Optional<BillId> id = createExistingBillId();
        if (id.isEmpty()) {
            return;
        }
        Optional<Bill> bill = billRepository.findById(id.get());
        if (bill.isPresent()) {
            String message =
                    bill.get().isPaid()
                            ? "Bill found in Registry"
                            : "Unpaid Bill found in Registry";
            System.out.println(message + ":\n  " + bill.get());
            if (in.inputConfirmation("Delete")) {
                billRepository.delete(bill.get());
            }
        }
    }

    private Optional<BillId> createUniqueBillId() {
        BillId id = null;

        boolean isUniqueId = false;
        while (!isUniqueId) {
            SocialSecurityNumber customerId;
            try {
                System.out.println("Enter Customers details:");
                boolean isNonExistingAllowed = true;
                customerId = createExistingSocialSecurityNumber(isNonExistingAllowed).get();
            } catch (NoSuchElementException e) {
                return Optional.empty();
            }

            LocalDateTime bookedTime;
            System.out.println("Enter booked details:");
            bookedTime = LocalDateTime.of(createLocalDate(), createLocalTime());

            id = new BillId(customerRepository.findById(customerId).get(), bookedTime);
            isUniqueId = !billRepository.existsById(id);
            if (!isUniqueId) {
                Bill bill = billRepository.findById(id).get();
                System.out.println("Bill already exists in the registry:\n  " + bill);
                if (in.inputConfirmation("Mark Bill as Paid?\n")) {
                    bill.setPaid(true);
                    billRepository.save(bill);
                }
                return Optional.empty();
            }
        }
        return Optional.of(id);
    }

    private Optional<BillId> createExistingBillId() {
        BillId id = null;

        boolean isUniqueId = false;
        while (!isUniqueId) {
            SocialSecurityNumber customerId;
            try {
                System.out.println("Enter Customers details:");
                customerId = createExistingSocialSecurityNumber().get();
            } catch (NoSuchElementException e) {
                return Optional.empty();
            }

            LocalDateTime bookedTime;
            System.out.println("Enter booked details:");
            bookedTime = LocalDateTime.of(createLocalDate(), createLocalTime());

            id = new BillId(customerRepository.findById(customerId).get(), bookedTime);
            isUniqueId = billRepository.existsById(id);
            if (!isUniqueId) {
                System.out.println("Bill doesn't exists in the registry:\n  " + id);
                if (!in.inputConfirmation("Try again?\n")) {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(id);
    }

    // Handle Customers

    private Optional<Customer> createCustomer() {
        SocialSecurityNumber socialSecurityNumber;
        try {
            socialSecurityNumber = createUniqueSocialSecurityNumber().get();
            return createCustomer(socialSecurityNumber);
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    private Optional<Customer> createCustomer(SocialSecurityNumber socialSecurityNumber) {
        String firstName = toInitialUpperCase(in.inputString("First name"));
        String lastName = toInitialUpperCase(in.inputString("Last name"));
        String address = "";
        for (String word : in.inputString("Address").split(" ")) {
            address += toInitialUpperCase(word) + " ";
        }

        return Optional.of(new Customer(socialSecurityNumber, firstName, lastName, address));
    }

    private void deleteCustomer() {
        Optional<SocialSecurityNumber> socialSecurityNumber = createExistingSocialSecurityNumber();
        if (socialSecurityNumber.isEmpty()) {
            return;
        }
        Optional<Customer> customer = customerRepository.findById(socialSecurityNumber.get());
        if (customer.isPresent()) {
            System.out.println("Customer found in Registry:\n  " + customer.get());
            if (in.inputConfirmation("Delete"))
                customerRepository.deleteById(socialSecurityNumber.get());
        }
    }

    private Optional<SocialSecurityNumber> createUniqueSocialSecurityNumber() {
        SocialSecurityNumber socialSecurityNumber = null;

        boolean isUniqueId = false;
        while (!isUniqueId) {
            LocalDate dateOfBirth = createLocalDate();
            int idLastFour = createSocialSecurityNumberLastFourDigit();
            socialSecurityNumber = new SocialSecurityNumber(dateOfBirth, idLastFour);

            isUniqueId = !customerRepository.existsById(socialSecurityNumber);
            if (!isUniqueId) {
                System.out.println(
                        "ID number already exists in the registry:\n  " + socialSecurityNumber);
                if (in.inputConfirmation("Update existing customer details?\n")) {
                    break;
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(socialSecurityNumber);
    }

    private Optional<SocialSecurityNumber> createExistingSocialSecurityNumber() {
        return createExistingSocialSecurityNumber(false);
    }

    private Optional<SocialSecurityNumber> createExistingSocialSecurityNumber(boolean orCreateNew) {
        SocialSecurityNumber socialSecurityNumber = null;

        boolean isUniqueId = false;
        while (!isUniqueId) {
            LocalDate dateOfBirth = createLocalDate();
            int idLastFour = createSocialSecurityNumberLastFourDigit();
            socialSecurityNumber = new SocialSecurityNumber(dateOfBirth, idLastFour);

            isUniqueId = customerRepository.existsById(socialSecurityNumber);
            if (!isUniqueId) {
                System.out.println(
                        "ID number doesn't exists in the registry:\n  " + socialSecurityNumber);
                if (orCreateNew && in.inputConfirmation("Register Customer?\n")) {
                    Optional<Customer> customer = createCustomer(socialSecurityNumber);
                    if (customer.isPresent()) {
                        customerRepository.save(customer.get());
                        return Optional.of(customer.get().getSocialSecurityNumber());
                    }
                } else if (!in.inputConfirmation("Try again?\n")) {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(socialSecurityNumber);
    }

    private LocalDate createLocalDate() {
        LocalDate date = LocalDate.MAX;
        boolean isDateValid = false;
        while (!isDateValid) {
            String dateOfBirthInput =
                    in.inputString("Date (yyyyMMdd)")
                            .replace("-", "")
                            .replace("/", "")
                            .replace(" ", "");
            try {
                date = LocalDate.parse(dateOfBirthInput, DateTimeFormatter.ofPattern("yyyyMMdd"));
                isDateValid = true;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid input. Try again.");
            }
        }
        return date;
    }

    private LocalTime createLocalTime() {
        LocalTime time = LocalTime.MAX;
        boolean isDateValid = false;
        while (!isDateValid) {
            String dateOfBirthInput =
                    in.inputString("Time - 24h format (hhmm)")
                            .replace(":", "")
                            .replace("-", "")
                            .replace("/", "")
                            .replace(" ", "");
            try {
                time = LocalTime.parse(dateOfBirthInput, DateTimeFormatter.ofPattern("HHmm"));
                isDateValid = true;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid input. Try again.");
            }
        }
        return time;
    }

    private int createSocialSecurityNumberLastFourDigit() {
        int idLastFour = -1;
        while (idLastFour < 0 || idLastFour > 9999) {
            idLastFour = in.inputInt("ID last four");
        }
        return idLastFour;
    }

    // Handle Services

    private Optional<Service> createService() {
        String serviceName = null;
        boolean isUniqueId = false;
        while (!isUniqueId) {
            serviceName = toInitialUpperCase(in.inputString("Service name"));

            isUniqueId = !serviceRepository.existsById(serviceName);
            if (!isUniqueId) {
                System.out.println("Service already exists in the registry:\n  " + serviceName);
                if (in.inputConfirmation("Update existing service details?\n")) {
                    break;
                } else {
                    return Optional.empty();
                }
            }
        }

        return createService(serviceName);
    }

    private Optional<Service> createService(String serviceName) {
        Service service = new Service();
        service.setName(serviceName);

        boolean isSekPerHourValid = false;
        while (!isSekPerHourValid) {
            try {
                service.setSekPerHour(in.inputInt("Price per hour SEK"));
                isSekPerHourValid = true;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        return Optional.of(service);
    }

    private void deleteService() {
        String serviceId = null;
        boolean isUniqueId = false;
        while (!isUniqueId) {
            serviceId = toInitialUpperCase(in.inputString("Service name"));

            isUniqueId = serviceRepository.existsById(serviceId);
            if (!isUniqueId) {
                System.out.println("Service doesn't exists in the registry:\n  " + serviceId);
                if (!in.inputConfirmation("Try again?\n")) {
                    return;
                }
            }
        }

        Optional<Service> service = serviceRepository.findById(serviceId);
        System.out.println("Service found in Registry:\n  " + service.get());
        if (in.inputConfirmation("Delete")) {
            serviceRepository.deleteById(serviceId);
        }
    }

    // Helpers

    private void printHumanReadableMenuChoiceIndexes(String[] menu) {
        for (int i = 0; i < menu.length; i++) {
            System.out.println(String.format("%2d", (i + 1)) + ".  " + menu[i]);
        }
    }

    private <T> int pickListIndex(T[] arr) {
        return pickListIndex(Arrays.asList(arr));
    }

    private <T> int pickListIndex(List<T> list) {
        while (true) {
            int index = in.inputInt("Choice") - 1;
            if (index >= 0 && index < list.size()) {
                return index + 1;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private <T> T selectFromList(T[] arr) {
        return selectFromList(Arrays.asList(arr));
    }

    private <T> T selectFromList(List<T> list) {
        while (true) {
            try {
                return list.get(in.inputInt("Choice") - 1);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private String toInitialUpperCase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
