package com.example.lab1.services;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.entities.LoanEntity;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private LoanRepository loanRepository;


    List<String> states= Arrays.asList("Activo", "Restringido");
    public CustomerEntity createCustomer(CustomerEntity customer) {
        if (customer.getName()!=null && customer.getRut()!=null && customer.getPhone()!=null && customer.getEmail()!=null) {
            return customerRepository.save(customer);
        }else {
            throw new IllegalArgumentException("Faltan datos para poder registrar al cliente");
        }
    }

    public void updateRestriction(CustomerEntity customer) {
        String rut = customer.getRut();

        boolean hasOverdueActive = loanRepository
                .existsByRutCustomerAndEndDateIsNullAndDueDateBefore(rut, LocalDate.now());

        boolean hasUnpaidDebts = loanRepository
                .existsByRutCustomerAndPaidIsFalseAndEndDateNotNull(rut);

        if (hasOverdueActive || hasUnpaidDebts || customer.getQuantityLoans() >= 5) {
            customer.setStatus("Restringido");
        } else {
            customer.setStatus("Activo");
        }
        customerRepository.save(customer);
    }

    public CustomerEntity checkAndCreateCustomer(Jwt jwt) {
        String rut = jwt.getClaimAsString("rut");


        CustomerEntity customer = customerRepository.findByrut(rut);

        if (customer == null) {
            System.out.println("Cliente no encontrado, creando nuevo registro completo...");
            CustomerEntity newCustomer = new CustomerEntity();

            newCustomer.setRut(rut);
            newCustomer.setEmail(jwt.getClaimAsString("email"));
            newCustomer.setName(jwt.getClaimAsString("given_name") + " " + jwt.getClaimAsString("family_name"));
            newCustomer.setPhone(jwt.getClaimAsString("phone"));

            String birthDateString = jwt.getClaimAsString("birthdate");
            if (birthDateString != null) {
                newCustomer.setBirthDate(LocalDate.parse(birthDateString));
            }

            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            List<String> roles = (List<String>) realmAccess.get("roles");
            newCustomer.setAdmin(roles != null && roles.contains("ADMIN"));

            newCustomer.setPassword(null);
            newCustomer.setStatus("Activo");
            newCustomer.setQuantityLoans(0);

            // --- CAMBIO SOLICITADO ---
            // En lugar de guardar directamente, llamamos a tu método createCustomer.
            return this.createCustomer(newCustomer);
        }

        return customer;
    }
}





