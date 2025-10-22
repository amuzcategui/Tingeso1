package com.example.lab1.Controllers;

import com.example.lab1.controllers.CustomerController;
import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.services.CustomerService;
import com.example.lab1.services.KardexService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // ✅ CAMBIO: Import correcto
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CustomerService customerService;
    @MockitoBean private CustomerRepository customerRepository;
    @MockitoBean private KardexService kardexService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Test
    @DisplayName("POST /create - OK")
    void testCreateCustomer_Success() throws Exception {
        when(customerService.createCustomer(any(CustomerEntity.class))).thenReturn(new CustomerEntity());
        mockMvc.perform(post("/api/v1/customer/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new CustomerEntity())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /create - Error")
    void testCreateCustomer_Error() throws Exception {
        when(customerService.createCustomer(any(CustomerEntity.class))).thenThrow(new IllegalArgumentException("Error"));
        mockMvc.perform(post("/api/v1/customer/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new CustomerEntity())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /complete-profile - OK")
    void testCompleteProfile_Success() throws Exception {
        when(customerService.checkAndCreateCustomer(any(Jwt.class))).thenReturn(new CustomerEntity());
        mockMvc.perform(post("/api/v1/customer/complete-profile")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /complete-profile - Error")
    void testCompleteProfile_Error() throws Exception {
        when(customerService.checkAndCreateCustomer(any(Jwt.class))).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(post("/api/v1/customer/complete-profile").with(jwt()).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /update-restriction - OK")
    void testUpdateRestriction_Success() throws Exception {
        when(customerRepository.findByrut(anyString())).thenReturn(new CustomerEntity());
        mockMvc.perform(put("/api/v1/customer/update-restriction").param("rut", "rut")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /update-restriction - Customer Not Found")
    void testUpdateRestriction_CustomerNotFound() throws Exception {
        when(customerRepository.findByrut(anyString())).thenReturn(null);
        mockMvc.perform(put("/api/v1/customer/update-restriction").param("rut", "rut")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /update-restriction - Service Error")
    void testUpdateRestriction_ServiceError() throws Exception {
        when(customerRepository.findByrut(anyString())).thenReturn(new CustomerEntity());
        doThrow(new RuntimeException("Error")).when(customerService).updateRestriction(any());
        mockMvc.perform(put("/api/v1/customer/update-restriction").param("rut", "rut")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("GET /all - OK")
    void testGetAllCustomers_Success() throws Exception {
        when(customerRepository.findAll()).thenReturn(List.of(new CustomerEntity(), new CustomerEntity()));
        mockMvc.perform(get("/api/v1/customer/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /all - Error")
    void testGetAllCustomers_Error() throws Exception {
        when(customerRepository.findAll()).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/v1/customer/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /allGreatherThan - OK")
    void testCustomerGreaterThan_Success() throws Exception {
        when(customerRepository.findByquantityLoansGreaterThan(anyInt())).thenReturn(List.of(new CustomerEntity()));
        mockMvc.perform(get("/api/v1/customer/allGreatherThan").param("quantity", "3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /allGreatherThan - Error")
    void testCustomerGreaterThan_Error() throws Exception {
        when(customerRepository.findByquantityLoansGreaterThan(anyInt())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/v1/customer/allGreatherThan").param("quantity", "3")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("GET /findCustomer - OK")
    void testFindByRut_Success() throws Exception {
        CustomerEntity customer = new CustomerEntity();
        customer.setRut("rut");
        when(customerRepository.findByrut("rut")).thenReturn(customer);
        mockMvc.perform(get("/api/v1/customer/findCustomer").param("rut", "rut")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut", is("rut")));
    }

    @Test
    @DisplayName("GET /findCustomer - Error")
    void testFindByRut_Error() throws Exception {
        when(customerRepository.findByrut(anyString())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/v1/customer/findCustomer").param("rut", "rut")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("GET /overdue - OK")
    void testCustomersOverdue_Success() throws Exception {
        when(kardexService.listOverdueCustomers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/customer/overdue")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /overdue - Error")
    void testCustomersOverdue_Error() throws Exception {
        when(kardexService.listOverdueCustomers()).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/v1/customer/overdue")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest());
    }
}