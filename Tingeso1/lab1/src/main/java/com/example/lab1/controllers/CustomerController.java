package com.example.lab1.controllers;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.services.CustomerService;
import com.example.lab1.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin("*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // RF3.1 Registrar cliente
    @PostMapping("/create")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerEntity customer) {
        try {
            CustomerEntity saved = customerService.createCustomer(customer);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // RF3.2 Cambiar estado de cliente
    @PutMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestBody CustomerEntity customer) {
        try {
            CustomerEntity updated = customerService.updateStatus(customer);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

