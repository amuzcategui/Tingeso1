package com.example.lab1.Controllers;

import com.example.lab1.controllers.ToolController;
import com.example.lab1.entities.ToolEntity;
import com.example.lab1.repositories.ToolRepository;
import com.example.lab1.services.ToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ToolControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private ToolService toolService;
    @Mock private ToolRepository toolRepository;

    @InjectMocks
    private ToolController toolController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(toolController).build();
    }

    private ToolEntity createTool(Long id, String name, int stock) {
        ToolEntity tool = new ToolEntity();
        tool.setId(id);
        tool.setName(name);
        tool.setStock(stock);
        return tool;
    }


    @Test
    void saveTool_Success() throws Exception {
        ToolEntity tool = createTool(null, "Martillo", 10);
        when(toolService.saveTool(any(ToolEntity.class), anyString())).thenReturn(tool);
        mockMvc.perform(post("/api/v1/tool/save").param("rutAdmin", "rut")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tool)))
                .andExpect(status().isOk());
    }

    @Test
    void saveTool_Error() throws Exception {
        when(toolService.saveTool(any(ToolEntity.class), anyString())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(post("/api/v1/tool/save").param("rutAdmin", "rut")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new ToolEntity())))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deactivateTool_Success() throws Exception {
        when(toolService.deactivateTool(anyLong(), anyString(), anyInt())).thenReturn(new ToolEntity());
        mockMvc.perform(put("/api/v1/tool/deactivate").param("idTool", "1").param("rutCustomer", "rut").param("quantityToDeactivate", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateTool_Error() throws Exception {
        when(toolService.deactivateTool(anyLong(), anyString(), anyInt())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(put("/api/v1/tool/deactivate").param("idTool", "1").param("rutCustomer", "rut").param("quantityToDeactivate", "1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void availableTool_Success() throws Exception {
        when(toolService.availableTool(anyLong(), anyString(), anyInt())).thenReturn(new ToolEntity());
        mockMvc.perform(put("/api/v1/tool/available").param("idTool", "1").param("rutCustomer", "rut").param("quantityToActivate", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void availableTool_Error() throws Exception {
        when(toolService.availableTool(anyLong(), anyString(), anyInt())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(put("/api/v1/tool/available").param("idTool", "1").param("rutCustomer", "rut").param("quantityToActivate", "1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void loanTool_Success() throws Exception {
        when(toolService.loanTool(anyLong(), anyString(), anyInt())).thenReturn(new ToolEntity());
        mockMvc.perform(put("/api/v1/tool/loan").param("idTool", "1").param("rutCustomer", "rut").param("quantityToLoan", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void loanTool_Error() throws Exception {
        when(toolService.loanTool(anyLong(), anyString(), anyInt())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(put("/api/v1/tool/loan").param("idTool", "1").param("rutCustomer", "rut").param("quantityToLoan", "1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void repairTool_Success() throws Exception {
        when(toolService.repairTool(anyLong(), anyString(), anyInt())).thenReturn(new ToolEntity());
        mockMvc.perform(put("/api/v1/tool/repair").param("idTool", "1").param("rutCustomer", "rut").param("quantityToRepair", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void repairTool_Error() throws Exception {
        when(toolService.repairTool(anyLong(), anyString(), anyInt())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(put("/api/v1/tool/repair").param("idTool", "1").param("rutCustomer", "rut").param("quantityToRepair", "1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateReplacementValue_Success() throws Exception {
        when(toolService.updateReplacementValue(anyLong(), anyDouble())).thenReturn(new ToolEntity());
        mockMvc.perform(put("/api/v1/tool/update-value").param("idTool", "1").param("replacementValue", "100.0"))
                .andExpect(status().isOk());
    }

    @Test
    void updateReplacementValue_Error() throws Exception {
        when(toolService.updateReplacementValue(anyLong(), anyDouble())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(put("/api/v1/tool/update-value").param("idTool", "1").param("replacementValue", "100.0"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAllTools_Success() throws Exception {
        when(toolRepository.findAllByinitialState(anyString())).thenReturn(List.of(createTool(1L, "Martillo", 5)));
        mockMvc.perform(get("/api/v1/tool/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllTools_Error() throws Exception {
        when(toolRepository.findAllByinitialState(anyString())).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/v1/tool/all"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAllToolsForAdmin_Success() throws Exception {
        when(toolRepository.findAll()).thenReturn(List.of(createTool(1L, "Martillo", 5)));
        mockMvc.perform(get("/api/v1/tool/inventory/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllToolsForAdmin_Error() throws Exception {
        when(toolRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/v1/tool/inventory/all"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getToolById_Found() throws Exception {
        when(toolRepository.findById(1L)).thenReturn(Optional.of(createTool(1L, "Martillo", 5)));
        mockMvc.perform(get("/api/v1/tool/by-id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getToolById_NotFound() throws Exception {
        when(toolRepository.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/tool/by-id/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getToolById_Error() throws Exception {
        when(toolRepository.findById(anyLong())).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/v1/tool/by-id/1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getToolByName_Success() throws Exception {
        when(toolRepository.findByname("Martillo")).thenReturn(List.of(createTool(1L, "Martillo", 5)));
        mockMvc.perform(get("/api/v1/tool/by-name/Martillo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Martillo")));
    }

    @Test
    void getToolByName_Error() throws Exception {
        when(toolRepository.findByname(anyString())).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/v1/tool/by-name/Martillo"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateToolFee_Success() throws Exception {
        when(toolService.updateFee(anyLong(), anyDouble())).thenReturn(new ToolEntity());
        mockMvc.perform(put("/api/v1/tool/update-fee").param("id", "1").param("fee", "120.0"))
                .andExpect(status().isOk());
    }

    @Test
    void updateToolFee_Error() throws Exception {
        when(toolService.updateFee(anyLong(), anyDouble())).thenThrow(new IllegalArgumentException("Error"));
        mockMvc.perform(put("/api/v1/tool/update-fee").param("id", "1").param("fee", "-10.0"))
                .andExpect(status().isBadRequest());
    }
}