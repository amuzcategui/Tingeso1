package com.example.lab1.services;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.entities.LoanEntity;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private LoanRepository loanRepository;
    //Requisitos Funcionales
    //• RF3.1 Registrar información de clientes (nombre, contacto, rut, estado).
    //• RF3.2 Cambiar estado de cliente a “restringido” en caso de atrasos.
    //Reglas de Negocio
    //• Cada cliente debe registrarse con: nombre, RUT, teléfono y correo.
    //• Estados de cliente: Activo(puede solicitar préstamos), Restringido (no puede hasta
    //regularizar atrasos).

    //private String name;
    //    private String email;
    //    private String rut;
    //    private String password;
    //    private String phone;
    //    private LocalDateTime birthDate;
    //    private boolean admin;
    //    private String status;
    //    private int quantityLoans;

    List<String> states= Arrays.asList("Activo", "Restringido");
    public CustomerEntity createCustomer(CustomerEntity customer) {
        if (customer.getName()!=null && customer.getRut()!=null && customer.getPhone()!=null && customer.getEmail()!=null) {
            return customerRepository.save(customer);
        }else {
            throw new IllegalArgumentException("Faltan datos para poder registrar al cliente");
        }
    }
//Este no está funcionando correctamente
    public CustomerEntity updateStatus(CustomerEntity customer) {
        List<LoanEntity> existingLoans = loanRepository.findByrutCustomer(customer.getRut());
        for (LoanEntity loan : existingLoans) {
            if (loan.getDueDate()!= loan.getEndDate()){
                customer.setStatus("Restringido");
                //customerRepository.save(customer);
            }else{
                customer.setStatus("Activo");
            }

        }

        return customerRepository.save(customer);
    }
}
