package com.example.lab1.Services;

import com.example.lab1.entities.KardexEntity;
import com.example.lab1.entities.ToolEntity;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.ToolRepository;
import com.example.lab1.services.ToolService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolServiceTest {

    @Mock ToolRepository toolRepository;
    @Mock KardexRepository kardexRepository;

    @InjectMocks
    ToolService toolService;


    private ToolEntity tool(Long id, String name, String cat, double value, double rentalFee, String state, int stock) {
        ToolEntity t = new ToolEntity();
        t.setId(id);
        t.setName(name);
        t.setCategory(cat);
        t.setToolValue(value);
        t.setRentalFee(rentalFee);
        t.setInitialState(state);
        t.setStock(stock);
        return t;
    }

    private static ArgumentMatcher<KardexEntity> kardexOf(String type, String name, int qty) {
        return k -> k != null && type.equals(k.getMovementType()) && name.equals(k.getToolName()) && qty == k.getToolQuantity();
    }


    @Nested
    class SaveTool {
        @Test
        void save_new_tool_ok() {
            ToolEntity incoming = tool(null, "Taladro", "E", 10000.0, 500.0, "Disponible", 5);
            when(toolRepository.findByname("Taladro")).thenReturn(Collections.emptyList());
            when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            ToolEntity saved = toolService.saveTool(incoming, "rut");
            assertEquals("Disponible", saved.getInitialState());
            verify(toolRepository).save(incoming);
            verify(kardexRepository).save(argThat(kardexOf("Ingreso", "Taladro", 5)));
        }

        @Test
        void save_merges_with_existing_ok() {
            ToolEntity existing = tool(1L, "T", "E", 100.0, 50.0, "Disponible", 3);
            ToolEntity incoming = tool(null, "T", "E", 100.0, 50.0, "Disponible", 2);
            when(toolRepository.findByname("T")).thenReturn(List.of(existing));
            when(toolRepository.save(any(ToolEntity.class))).thenReturn(existing);

            ToolEntity result = toolService.saveTool(incoming, "rut");
            assertEquals(5, result.getStock());
            verify(toolRepository).save(existing);
            verify(kardexRepository).save(argThat(kardexOf("Ingreso", "T", 2)));
        }

        @Test
        void save_no_merge_creates_new() {
            ToolEntity existing = tool(1L, "T", "E", 100.0, 50.0, "Prestada", 3); // Estado diferente
            ToolEntity incoming = tool(null, "T", "E", 100.0, 50.0, "Disponible", 2);
            when(toolRepository.findByname("T")).thenReturn(List.of(existing));
            when(toolRepository.save(incoming)).thenReturn(incoming);

            toolService.saveTool(incoming, "rut");
            verify(toolRepository).save(incoming);
            verify(kardexRepository).save(argThat(kardexOf("Ingreso", "T", 2)));
        }

        @Test
        void save_invalid_inputs_throw_IAE() {
            assertThrows(IllegalArgumentException.class, () -> toolService.saveTool(tool(1L, null, "E", 100, 50, "D", 1), "r"));
            assertThrows(IllegalArgumentException.class, () -> toolService.saveTool(tool(1L, "T", "E", 0, 50, "D", 1), "r"));
            assertThrows(IllegalArgumentException.class, () -> toolService.saveTool(tool(1L, "T", "E", 100, 50, "D", 0), "r"));
            verifyNoInteractions(toolRepository, kardexRepository);
        }
    }

    // =====================================================================
    // deactivateTool
    // =====================================================================
    @Nested
    class DeactivateTool {
        @Test
        void deactivate_partial_stock_ok() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 5);
            when(toolRepository.findByid(1L)).thenReturn(t);
            toolService.deactivateTool(1L, "rut", 2);
            assertEquals(3, t.getStock());
            verify(toolRepository).save(t);
            verify(toolRepository).save(argThat(x -> "Dada de baja".equals(x.getInitialState()) && x.getStock() == 2));
            verify(kardexRepository).save(argThat(kardexOf("Baja", "T", 2)));
        }

        @Test
        void deactivate_total_stock_ok() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 2);
            when(toolRepository.findByid(1L)).thenReturn(t);
            toolService.deactivateTool(1L, "rut", 2);
            assertEquals(0, t.getStock());
            assertEquals("Dada de baja", t.getInitialState());
            verify(toolRepository).save(t);
        }

        @Test
        void deactivate_invalid_throws_RuntimeException() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 5);
            when(toolRepository.findByid(1L)).thenReturn(t);
            assertThrows(RuntimeException.class, () -> toolService.deactivateTool(1L, "r", 0));
            assertThrows(RuntimeException.class, () -> toolService.deactivateTool(1L, "r", 6));
        }

        @Test
        void deactivate_repo_fails_throws_RuntimeException() {
            when(toolRepository.findByid(1L)).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> toolService.deactivateTool(1L, "r", 1));
        }
    }


    @Nested
    class AvailableTool {
        @Test
        void available_partial_stock_and_merge_ok() {
            ToolEntity prestada = tool(1L, "T", "E", 100, 50, "Prestada", 5);
            ToolEntity disponible = tool(2L, "T", "E", 100, 50, "Disponible", 8);
            when(toolRepository.findByid(1L)).thenReturn(prestada);
            when(toolRepository.findByname("T")).thenReturn(List.of(disponible));
            toolService.availableTool(1L, "rut", 2);
            assertEquals(10, disponible.getStock());
            assertEquals(3, prestada.getStock());
            verify(toolRepository).save(prestada);
            verify(toolRepository).save(disponible);
            verify(kardexRepository).save(argThat(kardexOf("Devolución", "T", 2)));
        }

        @Test
        void available_total_stock_deletes_prestada() {
            ToolEntity prestada = tool(1L, "T", "E", 100, 50, "Prestada", 2);
            ToolEntity disponible = tool(2L, "T", "E", 100, 50, "Disponible", 8);
            when(toolRepository.findByid(1L)).thenReturn(prestada);
            when(toolRepository.findByname("T")).thenReturn(List.of(disponible));
            toolService.availableTool(1L, "rut", 2);
            assertEquals(10, disponible.getStock());
            verify(toolRepository).delete(prestada);
        }

        @Test
        void available_no_merge_ok() {
            ToolEntity prestada = tool(1L, "T", "E", 100, 50, "Prestada", 5);
            when(toolRepository.findByid(1L)).thenReturn(prestada);
            when(toolRepository.findByname("T")).thenReturn(Collections.emptyList());
            toolService.availableTool(1L, "rut", 2);
            verify(toolRepository, never()).delete(any());
        }

        @Test
        void available_invalid_throws_RuntimeException() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Prestada", 5);
            when(toolRepository.findByid(1L)).thenReturn(t);
            assertThrows(RuntimeException.class, () -> toolService.availableTool(1L, "r", 0));
        }

        @Test
        void available_repo_fails_throws_RuntimeException() {
            when(toolRepository.findByid(1L)).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> toolService.availableTool(1L, "r", 1));
        }
    }


    @Nested
    class LoanTool {
        @Test
        void loan_ok() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 5);
            when(toolRepository.findByid(1L)).thenReturn(t);
            toolService.loanTool(1L, "rut", 2);
            assertEquals(3, t.getStock());
            verify(toolRepository).save(t);
            verify(toolRepository).save(argThat(x -> "Prestada".equals(x.getInitialState()) && x.getStock() == 2));
            verify(kardexRepository).save(argThat(kardexOf("Préstamo", "T", 2)));
        }

        @Test
        void loan_invalid_throws_RuntimeException() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 5);
            when(toolRepository.findByid(1L)).thenReturn(t);
            assertThrows(RuntimeException.class, () -> toolService.loanTool(1L, "r", 0));
            assertThrows(RuntimeException.class, () -> toolService.loanTool(1L, "r", 6));
        }

        @Test
        void loan_repo_fails_throws_RuntimeException() {
            when(toolRepository.findByid(1L)).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> toolService.loanTool(1L, "r", 1));
        }
    }


    @Nested
    class RepairTool {
        @Test
        void repair_partial_stock_ok() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 5);
            when(toolRepository.findByid(1L)).thenReturn(t);
            toolService.repairTool(1L, "rut", 2);
            assertEquals(3, t.getStock());
            verify(toolRepository).save(t);
            verify(toolRepository).save(argThat(x -> "En reparación".equals(x.getInitialState()) && x.getStock() == 2));
            verify(kardexRepository).save(argThat(kardexOf("reparación", "T", 2)));
        }

        @Test
        void repair_total_stock_changes_state() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 2);
            when(toolRepository.findByid(1L)).thenReturn(t);
            toolService.repairTool(1L, "rut", 2);
            assertEquals(0, t.getStock());
            assertEquals("Dada de baja", t.getInitialState());
            verify(toolRepository).save(t);
        }

        @Test
        void repair_invalid_throws_RuntimeException() {
            ToolEntity t = tool(1L, "T", "E", 100, 50, "Disponible", 5);
            when(toolRepository.findByid(1L)).thenReturn(t);
            assertThrows(RuntimeException.class, () -> toolService.repairTool(1L, "r", 0));
            assertThrows(RuntimeException.class, () -> toolService.repairTool(1L, "r", 6));
        }

        @Test
        void repair_repo_fails_throws_RuntimeException() {
            when(toolRepository.findByid(1L)).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> toolService.repairTool(1L, "r", 1));
        }
    }

    @Nested
    class UpdateReplacementValue {
        @Test
        void update_value_ok() {
            ToolEntity e = tool(1L, "T", "E", 100, 50, "D", 5);
            when(toolRepository.findByid(1L)).thenReturn(e);
            when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            ToolEntity res = toolService.updateReplacementValue(1L, 200.0);

            assertEquals(200.0, res.getToolValue());
            verify(toolRepository).save(e);
        }

        @Test
        void update_value_invalid_throws_IAE() {
            when(toolRepository.findByid(99L)).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () -> toolService.updateReplacementValue(99L, 100));
            assertThrows(IllegalArgumentException.class, () -> toolService.updateReplacementValue(1L, 0));
        }
    }


    @Nested
    class UpdateFee {
        @Test
        void update_fee_ok() {
            ToolEntity e = tool(1L, "T", "E", 100, 50, "D", 5);
            when(toolRepository.findByid(1L)).thenReturn(e);
            when(toolRepository.save(any(ToolEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            ToolEntity res = toolService.updateFee(1L, 120.0);

            assertEquals(120.0, res.getRentalFee());
            verify(toolRepository).save(e);
        }

        @Test
        void update_fee_invalid_throws_IAE() {
            when(toolRepository.findByid(99L)).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () -> toolService.updateFee(99L, 100));
            assertThrows(IllegalArgumentException.class, () -> toolService.updateFee(1L, 0)); // <= 0 es inválido
        }
    }
}