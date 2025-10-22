package com.example.lab1.Services;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.entities.LoanEntity;
import com.example.lab1.entities.ToolEntity;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.repositories.ToolRepository;
import com.example.lab1.services.CustomerService;
import com.example.lab1.services.LoanService;
import com.example.lab1.services.ToolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock ToolRepository toolRepository;
    @Mock CustomerRepository customerRepository;
    @Mock KardexRepository kardexRepository;
    @Mock
    ToolService toolService;
    @Mock
    CustomerService customerService;

    @InjectMocks
    LoanService loanService;

    private CustomerEntity activeCustomer;
    private ToolEntity availableTool;

    @BeforeEach
    void setUp() {
        activeCustomer = new CustomerEntity();
        activeCustomer.setRut("12345678-9");
        activeCustomer.setStatus("Activo");
        activeCustomer.setQuantityLoans(0);

        availableTool = new ToolEntity();
        availableTool.setId(10L);
        availableTool.setName("Taladro");
        availableTool.setInitialState("Disponible");
        availableTool.setStock(2);
        availableTool.setRentalFee(1000.0);
    }

    @Test
    void createLoan_ok_calculatesFeeAndSaves() {
        when(customerRepository.findByrut(anyString())).thenReturn(activeCustomer);
        when(toolRepository.findByname(anyString())).thenReturn(List.of(availableTool));
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanEntity created = loanService.createLoan(
                new LoanEntity(), "rut", List.of("Taladro"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 5) // 4 días
        );

        assertEquals(4000.0, created.getRentalFee()); // 4 * 1000.0
        assertEquals(1, activeCustomer.getQuantityLoans());
        verify(toolService).loanTool(10L, "rut", 1);
        verify(loanRepository).save(any(LoanEntity.class));
    }


    @Nested
    class CreateLoanValidation {
        @Test
        void createLoan_customerNotFound_throwsIAE() {
            when(customerRepository.findByrut(anyString())).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("T"), LocalDate.now(), LocalDate.now().plusDays(1)));
        }

        @Test
        void createLoan_customerHasOverdue_throwsIAE() {
            when(customerRepository.findByrut(anyString())).thenReturn(activeCustomer);
            when(loanRepository.existsByRutCustomerAndEndDateIsNullAndDueDateBefore(anyString(), any(LocalDate.class))).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("T"), LocalDate.now(), LocalDate.now().plusDays(1)));
        }

        @Test
        void createLoan_customerHasUnpaid_throwsIAE() {
            when(customerRepository.findByrut(anyString())).thenReturn(activeCustomer);
            when(loanRepository.existsByRutCustomerAndPaidIsFalseAndEndDateNotNull(anyString())).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("T"), LocalDate.now(), LocalDate.now().plusDays(1)));
        }

        @Test
        void createLoan_customerInactive_throwsIAE() {
            activeCustomer.setStatus("Inactivo");
            when(customerRepository.findByrut(anyString())).thenReturn(activeCustomer);
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("T"), LocalDate.now(), LocalDate.now().plusDays(1)));
        }

        @Test
        void createLoan_dueBeforeStart_throwsIAE() {
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("T"), LocalDate.now(), LocalDate.now().minusDays(1)));
        }

        @Test
        void createLoan_sameDayRental_throwsIAE() {
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("T"), LocalDate.now(), LocalDate.now()));
        }

        @Test
        void createLoan_toolNotAvailable_throwsIAE() {
            when(customerRepository.findByrut(anyString())).thenReturn(activeCustomer);
            when(toolRepository.findByname(anyString())).thenReturn(Collections.emptyList());
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("T"), LocalDate.now(), LocalDate.now().plusDays(1)));
        }

        @Test
        void createLoan_toolAlreadyInAnotherLoan_throwsIAE() {
            when(customerRepository.findByrut(anyString())).thenReturn(activeCustomer);
            LoanEntity activeLoan = new LoanEntity();
            activeLoan.setToolNames(List.of("Taladro"));
            when(loanRepository.findByRutCustomerAndEndDateIsNull(anyString())).thenReturn(List.of(activeLoan));

            assertThrows(IllegalArgumentException.class, () ->
                    loanService.createLoan(new LoanEntity(), "rut", List.of("Taladro"), LocalDate.now(), LocalDate.now().plusDays(1)));
        }
    }


    @Nested
    class ReturnTools {
        private LoanEntity baseLoan;

        @BeforeEach
        void setUp() {
            baseLoan = new LoanEntity();
            baseLoan.setId(1L);
            baseLoan.setRutCustomer("rut");
            baseLoan.setToolNames(List.of("Martillo"));
            baseLoan.setDueDate(LocalDate.now()); // Due today
        }

        @Test
        void returnTools_ok_noFine() {
            when(loanRepository.findByid(1L)).thenReturn(baseLoan);
            when(customerRepository.findByrut("rut")).thenReturn(activeCustomer);

            ToolEntity prestada = new ToolEntity();
            prestada.setId(1L);
            prestada.setName("Martillo");
            prestada.setInitialState("Prestada");
            when(toolRepository.findByname("Martillo")).thenReturn(List.of(prestada));

            loanService.returnTools(1L, 1000, 0, new ArrayList<>(), new ArrayList<>());
            assertEquals(0.0, baseLoan.getFine());
            verify(toolService).availableTool(1L, "rut", 1);
        }

        @Test
        void returnTools_lateReturn_calculatesFine() {
            baseLoan.setDueDate(LocalDate.now().minusDays(5)); // 5 días de atraso
            when(loanRepository.findByid(1L)).thenReturn(baseLoan);
            when(customerRepository.findByrut("rut")).thenReturn(activeCustomer);
            ToolEntity prestada = new ToolEntity();
            prestada.setId(1L);
            prestada.setName("Martillo");
            prestada.setInitialState("Prestada");
            when(toolRepository.findByname("Martillo")).thenReturn(List.of(prestada));

            loanService.returnTools(1L, 1000, 0, new ArrayList<>(), new ArrayList<>());

            assertEquals(5000.0, baseLoan.getFine()); // 5 días * 1000
            verify(toolService).availableTool(1L, "rut", 1);
        }

        @Test
        void returnTools_withDamage_addsRepairCost() {
            when(loanRepository.findByid(1L)).thenReturn(baseLoan);
            when(customerRepository.findByrut("rut")).thenReturn(activeCustomer);

            ToolEntity prestada = new ToolEntity();
            prestada.setId(1L);
            prestada.setName("Martillo");
            prestada.setInitialState("Prestada");
            when(toolRepository.findByname("Martillo")).thenReturn(List.of(prestada));

            loanService.returnTools(1L, 1000, 5000, List.of("Martillo"), new ArrayList<>());
            assertEquals(5000.0, baseLoan.getFine());
            verify(toolService).repairTool(1L, "rut", 1);
        }

        @Test
        void returnTools_withDiscard_addsReplacementCost() {
            when(loanRepository.findByid(1L)).thenReturn(baseLoan);
            when(customerRepository.findByrut("rut")).thenReturn(activeCustomer);

            ToolEntity prestada = new ToolEntity();
            prestada.setId(1L);
            prestada.setName("Martillo");
            prestada.setInitialState("Prestada");
            prestada.setToolValue(20000);
            when(toolRepository.findByname("Martillo")).thenReturn(List.of(prestada));

            loanService.returnTools(1L, 1000, 0, new ArrayList<>(), List.of("Martillo"));
            assertEquals(20000.0, baseLoan.getFine());
            verify(toolService).deactivateTool(1L, "rut", 1);
        }

        @Test
        void returnTools_loanNotFound_throwsIAE() {
            when(loanRepository.findByid(99L)).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.returnTools(99L, 1000, 0, new ArrayList<>(), new ArrayList<>()));
        }

        @Test
        void returnTools_alreadyPaid_throwsIAE() {
            when(loanRepository.findByid(1L)).thenReturn(baseLoan);
            baseLoan.setPaid(true);
            assertThrows(IllegalArgumentException.class, () ->
                    loanService.returnTools(1L, 1000, 0, new ArrayList<>(), new ArrayList<>()));
        }

        @Test
        void returnTools_prestadaToolNotFound_throwsIAE() {
            when(loanRepository.findByid(1L)).thenReturn(baseLoan);
            when(customerRepository.findByrut("rut")).thenReturn(activeCustomer);
            when(toolRepository.findByname(anyString())).thenReturn(Collections.emptyList()); // No tool found

            assertThrows(IllegalArgumentException.class, () ->
                    loanService.returnTools(1L, 1000, 0, new ArrayList<>(), new ArrayList<>()));
        }
    }


    @Nested
    class MarkLoanAsPaid {
        @Test
        void markAsPaid_ok() {
            LoanEntity loan = new LoanEntity();
            loan.setRutCustomer("rut");
            when(loanRepository.findByid(1L)).thenReturn(loan);
            when(customerRepository.findByrut("rut")).thenReturn(activeCustomer);

            loanService.markLoanAsPaid(1L);

            assertTrue(loan.isPaid());
            verify(customerService).updateRestriction(activeCustomer);
            verify(loanRepository).save(loan);
        }

        @Test
        void markAsPaid_loanNotFound_throwsIAE() {
            when(loanRepository.findByid(99L)).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () -> loanService.markLoanAsPaid(99L));
        }

        @Test
        void markAsPaid_customerNotFound_doesNotThrow() {
            LoanEntity loan = new LoanEntity();
            loan.setRutCustomer("rut");
            when(loanRepository.findByid(1L)).thenReturn(loan);
            when(customerRepository.findByrut("rut")).thenReturn(null); // Customer not found

            loanService.markLoanAsPaid(1L);

            assertTrue(loan.isPaid());
            verify(customerService, never()).updateRestriction(any()); // Should not be called
            verify(loanRepository).save(loan);
        }
    }


    @Nested
    class HelperMethods {
        @Test
        void validateLoan_isValid() {
            ToolEntity tool = new ToolEntity();
            tool.setInitialState("Disponible");
            tool.setStock(1);
            assertTrue(loanService.validateLoan(tool));
        }

        @Test
        void validateLoan_isNotValid() {
            ToolEntity tool1 = new ToolEntity();
            tool1.setInitialState("Prestada");
            tool1.setStock(1);
            assertFalse(loanService.validateLoan(tool1));

            ToolEntity tool2 = new ToolEntity();
            tool2.setInitialState("Disponible");
            tool2.setStock(0);
            assertFalse(loanService.validateLoan(tool2));
        }

        @Test
        void calculateLateFee_isLate() {
            double fee = loanService.calculateLateFee(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 4), // 3 days late
                    1000.0);
            assertEquals(3000.0, fee);
        }

        @Test
        void calculateLateFee_notLate() {
            double fee = loanService.calculateLateFee(
                    LocalDate.of(2025, 1, 4),
                    LocalDate.of(2025, 1, 4), // Same day
                    1000.0);
            assertEquals(0.0, fee);
        }
    }

    @Test
    void findLoansByCustomerRut_returnsList() {
        List<LoanEntity> loans = List.of(new LoanEntity(), new LoanEntity());
        when(loanRepository.findByrutCustomer("rut")).thenReturn(loans);

        List<LoanEntity> result = loanService.findLoansByCustomerRut("rut");

        assertEquals(2, result.size());
        verify(loanRepository).findByrutCustomer("rut");
    }
}