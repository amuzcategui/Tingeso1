package com.example.lab1.Controllers;

import com.example.lab1.entities.LoanEntity;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.services.LoanService;
import com.example.lab1.controllers.LoanController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private LoanService loanService;
    @Mock private LoanRepository loanRepository;

    @InjectMocks
    private LoanController loanController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(loanController).build();
    }

    private LoanEntity createLoanEntity(long id, String rut) {
        LoanEntity loan = new LoanEntity();
        loan.setId(id);
        loan.setRutCustomer(rut);
        return loan;
    }


    @Test
    void createLoan_Success() throws Exception {
        LoanEntity loan = createLoanEntity(1L, "rut");
        when(loanService.createLoan(any(), any(), any(), any(), any())).thenReturn(loan);
        mockMvc.perform(post("/api/v1/loan/create")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loan)))
                .andExpect(status().isOk());
    }

    @Test
    void createLoan_Error() throws Exception {
        when(loanService.createLoan(any(), any(), any(), any(), any())).thenThrow(new IllegalArgumentException("Error"));
        mockMvc.perform(post("/api/v1/loan/create")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new LoanEntity())))
                .andExpect(status().isBadRequest());
    }


    @Test
    void returnTools_Success() throws Exception {
        when(loanService.returnTools(anyLong(), anyDouble(), anyDouble(), any(), any())).thenReturn(new LoanEntity());
        mockMvc.perform(put("/api/v1/loan/return/1")
                        .param("damaged", "Taladro").param("discarded", "Martillo"))
                .andExpect(status().isOk());
    }

    @Test
    void returnTools_Success_NoOptionalParams() throws Exception {
        when(loanService.returnTools(1L, 0, 0, new ArrayList<>(), new ArrayList<>())).thenReturn(new LoanEntity());
        mockMvc.perform(put("/api/v1/loan/return/1"))
                .andExpect(status().isOk());
    }

    @Test
    void returnTools_Error() throws Exception {
        when(loanService.returnTools(anyLong(), anyDouble(), anyDouble(), any(), any())).thenThrow(new IllegalArgumentException("Error"));
        mockMvc.perform(put("/api/v1/loan/return/1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void payLoan_Success() throws Exception {
        when(loanService.markLoanAsPaid(anyLong())).thenReturn(new LoanEntity());
        mockMvc.perform(put("/api/v1/loan/1/pay"))
                .andExpect(status().isOk());
    }

    @Test
    void payLoan_Error() throws Exception {
        when(loanService.markLoanAsPaid(anyLong())).thenThrow(new IllegalArgumentException("Error"));
        mockMvc.perform(put("/api/v1/loan/1/pay"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getMyLoans_Success() throws Exception {
        when(loanService.findLoansByCustomerRut("rut")).thenReturn(List.of(new LoanEntity()));
        mockMvc.perform(get("/api/v1/loan/my-loans").param("rut", "rut"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getMyLoans_Error() throws Exception {
        when(loanService.findLoansByCustomerRut("rut")).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/v1/loan/my-loans").param("rut", "rut"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAllLoans_Success() throws Exception {
        when(loanRepository.findAll()).thenReturn(List.of(new LoanEntity(), new LoanEntity()));
        mockMvc.perform(get("/api/v1/loan/all-loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getAllLoans_Error() throws Exception {
        when(loanRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/v1/loan/all-loans"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getLoanById_Found() throws Exception {
        LoanEntity loan = createLoanEntity(1L, "rut");
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        mockMvc.perform(get("/api/v1/loan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getLoanById_NotFound_ThrowsException() throws Exception {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/loan/99"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Préstamo no encontrado con ID: 99")));
    }
}