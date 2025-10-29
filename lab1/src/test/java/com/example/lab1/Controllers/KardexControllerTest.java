package com.example.lab1.Controllers;

import com.example.lab1.controllers.KardexController;
import com.example.lab1.entities.KardexEntity;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.services.KardexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = KardexController.class)
class KardexControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private KardexRepository kardexRepository;
    @MockitoBean private CustomerRepository customerRepository;
    @MockitoBean private KardexService kardexService;

    private KardexEntity kx(String name, int qty) {
        KardexEntity k = new KardexEntity();
        k.setToolName(name);
        k.setToolQuantity(qty);
        return k;
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void toolHistory_ok_returns_list() throws Exception {
        when(kardexRepository.findBytoolName("Taladro")).thenReturn(List.of(kx("Taladro", 2)));
        mockMvc.perform(get("/api/v1/kardex/tool-history").param("toolName", "Taladro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toolName").value("Taladro"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toolHistory_bad_request_when_blank() throws Exception {
        mockMvc.perform(get("/api/v1/kardex/tool-history").param("toolName", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("toolName es requerido")));
        verifyNoInteractions(kardexRepository, kardexService, customerRepository);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toolHistory_catches_exception() throws Exception {
        when(kardexRepository.findBytoolName(anyString())).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get("/api/v1/kardex/tool-history").param("toolName", "Taladro"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("boom")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void range_bad_request_when_to_before_from() throws Exception {
        mockMvc.perform(get("/api/v1/kardex/range").param("from", "2025-02-01").param("to", "2025-01-01").param("movementType", "Préstamo"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(kardexRepository);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void range_ok_returns_list_between() throws Exception {
        when(kardexRepository.findByMovementTypeAndMovementDateBetween(any(), any(), any())).thenReturn(List.of(kx("Taladro", 1)));
        mockMvc.perform(get("/api/v1/kardex/range").param("from", "2025-01-01").param("to", "2025-01-31").param("movementType", "Préstamo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toolName").value("Taladro"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void range_catches_exception() throws Exception {
        when(kardexRepository.findByMovementTypeAndMovementDateBetween(any(), any(), any())).thenThrow(new RuntimeException("DB down"));
        mockMvc.perform(get("/api/v1/kardex/range").param("from", "2025-01-01").param("to", "2025-01-31").param("movementType", "Préstamo"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void activeLoansGrouped_ok() throws Exception {
        when(kardexService.listActiveLoansGrouped()).thenReturn(Collections.emptyMap());
        mockMvc.perform(get("/api/v1/kardex/loans/active/grouped"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activeLoansGrouped_catches_exception() throws Exception {
        when(kardexService.listActiveLoansGrouped()).thenThrow(new RuntimeException("X"));
        mockMvc.perform(get("/api/v1/kardex/loans/active/grouped"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void topTools_bad_request_when_to_before_from() throws Exception {
        mockMvc.perform(get("/api/v1/kardex/tools/top").param("from", "2025-02-01").param("to", "2025-01-01"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(kardexService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void topTools_ok_with_all_params() throws Exception {
        when(kardexService.topLoanedTools(any(), any(), any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/kardex/tools/top").param("from", "2025-01-01").param("to", "2025-01-31").param("limit", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void topTools_ok_with_no_optional_params() throws Exception {

        when(kardexService.topLoanedTools(isNull(), isNull(), isNull())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/kardex/tools/top"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void topTools_catches_exception() throws Exception {
        when(kardexService.topLoanedTools(any(), any(), any())).thenThrow(new RuntimeException("Z"));
        mockMvc.perform(get("/api/v1/kardex/tools/top"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void allKardex_ok_returns_list() throws Exception {

        when(kardexRepository.findAll()).thenReturn(List.of(kx("Sierra", 1), kx("Martillo", 3)));
        mockMvc.perform(get("/api/v1/kardex/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allKardex_catches_exception() throws Exception {

        when(kardexRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/v1/kardex/all"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("DB Error")));
    }
}