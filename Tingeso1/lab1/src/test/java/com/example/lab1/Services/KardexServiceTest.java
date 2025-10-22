package com.example.lab1.Services;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.entities.KardexEntity;
import com.example.lab1.entities.LoanEntity;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.services.KardexService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KardexServiceTest {

    @Mock KardexRepository kardexRepository;
    @Mock LoanRepository loanRepository;
    @Mock CustomerRepository customerRepository;

    @InjectMocks
    KardexService kardexService;

    private LoanEntity loan(String rut, LocalDate due, LocalDate end) {
        LoanEntity l = new LoanEntity();
        l.setRutCustomer(rut);
        l.setDueDate(due);
        l.setEndDate(end);
        return l;
    }

    private CustomerEntity customer(String rut, String name) {
        CustomerEntity c = new CustomerEntity();
        c.setRut(rut);
        c.setName(name);
        return c;
    }


    @Test
    void kardex_builds_entity_with_now_date() {
        KardexEntity k = kardexService.kardex("11-1", "Préstamo", "Taladro", 2);
        assertEquals("11-1", k.getRutCustomer());
        assertEquals("Préstamo", k.getMovementType());
        assertEquals("Taladro", k.getToolName());
        assertEquals(2, k.getToolQuantity());
        assertNotNull(k.getMovementDate());
        assertTrue(!k.getMovementDate().isAfter(LocalDate.now()));
    }


    @Test
    void listActiveLoansGrouped_splits_into_late_and_notLate_plus_nullDue_as_notLate() {
        LocalDate today = LocalDate.now();


        List<LoanEntity> lates = List.of(
                loan("1", today.minusDays(1), null),
                loan("2", today.minusDays(10), null)
        );

        List<LoanEntity> notLates = new ArrayList<>(List.of(
                loan("3", today, null),
                loan("4", today.plusDays(5), null)
        ));

        List<LoanEntity> activeNoDue = List.of(
                loan("5", null, null),
                loan("6", null, null)
        );

        when(loanRepository.findByEndDateIsNullAndDueDateBefore(any(LocalDate.class)))
                .thenReturn(lates);
        when(loanRepository.findByEndDateIsNullAndDueDateGreaterThanEqual(any(LocalDate.class)))
                .thenReturn(notLates);
        when(loanRepository.findByEndDateIsNull())
                .thenReturn(activeNoDue);

        Map<String, List<LoanEntity>> grouped = kardexService.listActiveLoansGrouped();

        assertEquals(2, grouped.get("Atrasos").size());
        assertEquals(4, grouped.get("Vigentes").size());

        assertEquals(List.of("Atrasos", "Vigentes"), new ArrayList<>(grouped.keySet()));
    }

    @Test
    void listOverdueCustomers_returns_unique_customers_and_skips_null_rut() {
        LocalDate today = LocalDate.now();
        List<LoanEntity> lateLoans = List.of(
                loan("1", today.minusDays(1), null),
                loan("1", today.minusDays(2), null),
                loan(null, today.minusDays(3), null)
        );

        when(loanRepository.findByEndDateIsNullAndDueDateBefore(any(LocalDate.class)))
                .thenReturn(lateLoans);
        when(customerRepository.findByrut("1")).thenReturn(customer("1", "Alice"));

        List<CustomerEntity> out = kardexService.listOverdueCustomers();

        assertEquals(1, out.size());
        assertEquals("1", out.get(0).getRut());
        assertEquals("Alice", out.get(0).getName());
    }


    @Nested
    class TopLoanedTools {

        private KardexEntity kx(String name, Integer qty) {
            KardexEntity k = new KardexEntity();
            k.setToolName(name);
            k.setToolQuantity(qty == null ? 0 : qty);
            return k;
        }

        @Test
        void without_range_uses_findBymovementType_and_aggregates_sorts_and_limits() {

            when(kardexRepository.findBymovementType("Préstamo"))
                    .thenReturn(List.of(
                            kx("Taladro", 3),
                            kx("Taladro", 2),
                            kx("Sierra", 5),
                            kx("Sierra", 1),
                            kx("Lijadora", 0),
                            kx(null, 10)
                    ));

            List<Object[]> out = kardexService.topLoanedTools(null, null, 2);


            assertEquals(2, out.size());
            assertEquals("Sierra", out.get(0)[0]);
            assertEquals(6, out.get(0)[1]);
            assertEquals("Taladro", out.get(1)[0]);
            assertEquals(5, out.get(1)[1]);
            verify(kardexRepository).findBymovementType("Préstamo");
            verify(kardexRepository, never()).findByMovementTypeAndMovementDateBetween(anyString(), any(), any());
        }

        @Test
        void with_range_uses_between_and_applies_no_limit_when_null_or_too_large() {
            LocalDate from = LocalDate.of(2025, 1, 1);
            LocalDate to   = LocalDate.of(2025, 1, 31);

            when(kardexRepository.findByMovementTypeAndMovementDateBetween("Préstamo", from, to))
                    .thenReturn(List.of(
                            kx("Sierra", 2),
                            kx("Taladro", 4)
                    ));


            List<Object[]> out = kardexService.topLoanedTools(from, to, null);
            assertEquals(2, out.size());
            assertEquals("Taladro", out.get(0)[0]);
            assertEquals(4, out.get(0)[1]);
            assertEquals("Sierra", out.get(1)[0]);
            assertEquals(2, out.get(1)[1]);


            List<Object[]> out2 = kardexService.topLoanedTools(from, to, 999);
            assertEquals(2, out2.size());

            verify(kardexRepository, times(2))
                    .findByMovementTypeAndMovementDateBetween("Préstamo", from, to);
        }
    }
}
