package com.example.lab1.controllers;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.services.CustomerService;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.services.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin("*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private KardexService kardexService;

    // RF3.1 client registration
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerEntity customer) {
        try {
            CustomerEntity saved = customerService.createCustomer(customer);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //register the client in the database
    @PostMapping("/complete-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeProfile(@AuthenticationPrincipal Jwt jwt) {
        try {
            CustomerEntity newCustomer = customerService.checkAndCreateCustomer(jwt);
            return ResponseEntity.ok(newCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/update-restriction")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateRestriction(@RequestParam String rut) {
        try {
            CustomerEntity customer = customerRepository.findByrut(rut);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Cliente no encontrado");
            }
            customerService.updateRestriction(customer);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<CustomerEntity> customers = customerRepository.findAll();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/allGreatherThan")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> customerGreatherThan(@RequestParam int quantity) {
        try {
            List<CustomerEntity> customers = customerRepository.findByquantityLoansGreaterThan(quantity);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/findCustomer")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> findByRut(@RequestParam String rut) {
        try {
            CustomerEntity customer = customerRepository.findByrut(rut);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // RF6.2 clients with overdue
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> customersOverdue() {
        try {
            return ResponseEntity.ok(kardexService.listOverdueCustomers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





}

