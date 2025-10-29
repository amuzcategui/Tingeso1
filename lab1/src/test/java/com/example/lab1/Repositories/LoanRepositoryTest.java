package com.example.lab1.Repositories;

import com.example.lab1.entities.LoanEntity;
import com.example.lab1.repositories.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Útil para evitar choques con palabras reservadas en H2 en TODOS los repos de prueba
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
class LoanRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private LoanRepository loanRepository;

    // ---------- helpers ----------
    private LoanEntity persistLoan(
            String rut,
            LocalDate start,
            LocalDate due,
            LocalDate end,
            boolean paid
    ) {
        LoanEntity l = new LoanEntity();
        l.setRutCustomer(rut);
        l.setStartDate(start);
        l.setDueDate(due);
        l.setEndDate(end);
        l.setRentalFee(1000.0);
        l.setFine(0.0);
        l.setPaid(paid);
        // Si en tu entidad estos son @ElementCollection o similares, perfecto;
        // si no, puedes dejarlos vacíos o con 1 valor simple.
        l.setToolNames(List.of("Taladro"));
        l.setDamagedTools(List.of());
        l.setDiscardedTools(List.of());
        em.persist(l);
        em.flush();
        return l;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("existsByRutCustomerAndEndDateIsNullAndDueDateBefore: true sólo para préstamos activos y vencidos")
    void existsByRutCustomerAndEndDateIsNullAndDueDateBefore_works() {
        LocalDate today = LocalDate.now();

        // Activo y vencido (del rut A) -> debe contar
        LoanEntity a1 = persistLoan("11-1", today.minusDays(10), today.minusDays(1), null, false);

        // Activo y NO vencido (del rut A)
        LoanEntity a2 = persistLoan("11-1", today.minusDays(2), today.plusDays(3), null, false);

        // Devuelto y con due anterior (del rut A) -> no cuenta por tener endDate
        LoanEntity a3 = persistLoan("11-1", today.minusDays(8), today.minusDays(3), today.minusDays(1), false);

        // Activo y vencido pero de otro rut -> para control
        LoanEntity b1 = persistLoan("22-2", today.minusDays(7), today.minusDays(2), null, false);

        boolean rutAOverdue = loanRepository
                .existsByRutCustomerAndEndDateIsNullAndDueDateBefore("11-1", today);
        boolean rutBOverdue = loanRepository
                .existsByRutCustomerAndEndDateIsNullAndDueDateBefore("22-2", today);
        boolean rutCOverdue = loanRepository
                .existsByRutCustomerAndEndDateIsNullAndDueDateBefore("33-3", today);

        assertThat(rutAOverdue).isTrue();   // a1 califica
        assertThat(rutBOverdue).isTrue();   // b1 califica
        assertThat(rutCOverdue).isFalse();  // no hay préstamos para ese rut
    }

    @Test
    @DisplayName("findByRutCustomerAndEndDateIsNull: devuelve sólo préstamos activos del rut")
    void findByRutCustomerAndEndDateIsNull_works() {
        LocalDate today = LocalDate.now();

        LoanEntity a1 = persistLoan("11-1", today.minusDays(10), today.minusDays(1), null, false);  // activo
        LoanEntity a2 = persistLoan("11-1", today.minusDays(2), today.plusDays(3), null, false);    // activo
        LoanEntity a3 = persistLoan("11-1", today.minusDays(8), today.minusDays(3), today, false);  // devuelto
        LoanEntity b1 = persistLoan("22-2", today.minusDays(7), today.minusDays(2), null, false);   // activo, otro rut

        List<LoanEntity> activeA = loanRepository.findByRutCustomerAndEndDateIsNull("11-1");

        assertThat(activeA)
                .extracting(LoanEntity::getId)
                .containsExactlyInAnyOrder(a1.getId(), a2.getId())
                .doesNotContain(a3.getId(), b1.getId());
    }

    @Test
    @DisplayName("existsByRutCustomerAndPaidIsFalse: detecta deudas impagas por rut")
    void existsByRutCustomerAndPaidIsFalse_works() {
        LocalDate today = LocalDate.now();

        persistLoan("11-1", today.minusDays(3), today.plusDays(3), null, false); // impago
        persistLoan("22-2", today.minusDays(5), today.minusDays(1), today, true); // pagado

        assertThat(loanRepository.existsByRutCustomerAndPaidIsFalse("11-1")).isTrue();
        assertThat(loanRepository.existsByRutCustomerAndPaidIsFalse("22-2")).isFalse();
        assertThat(loanRepository.existsByRutCustomerAndPaidIsFalse("33-3")).isFalse();
    }

    @Test
    @DisplayName("findByEndDateIsNull: devuelve todos los préstamos activos")
    void findByEndDateIsNull_works() {
        LocalDate today = LocalDate.now();

        LoanEntity a1 = persistLoan("11-1", today.minusDays(3), today.plusDays(1), null, false); // activo
        LoanEntity a2 = persistLoan("22-2", today.minusDays(10), today.minusDays(1), null, false); // activo
        LoanEntity a3 = persistLoan("33-3", today.minusDays(8), today.minusDays(2), today, false); // devuelto

        List<LoanEntity> actives = loanRepository.findByEndDateIsNull();

        assertThat(actives)
                .extracting(LoanEntity::getId)
                .contains(a1.getId(), a2.getId())
                .doesNotContain(a3.getId());
    }

    @Test
    @DisplayName("findByEndDateIsNullAndDueDateBefore: devuelve sólo activos vencidos")
    void findByEndDateIsNullAndDueDateBefore_works() {
        LocalDate today = LocalDate.now();

        LoanEntity overdue = persistLoan("11-1", today.minusDays(5), today.minusDays(1), null, false);
        LoanEntity notOverdue = persistLoan("11-1", today.minusDays(1), today.plusDays(2), null, false);
        LoanEntity returnedButOverdue = persistLoan("11-1", today.minusDays(7), today.minusDays(2), today, false);

        List<LoanEntity> result = loanRepository.findByEndDateIsNullAndDueDateBefore(today);

        assertThat(result)
                .extracting(LoanEntity::getId)
                .contains(overdue.getId())
                .doesNotContain(notOverdue.getId(), returnedButOverdue.getId());
    }

    @Test
    @DisplayName("findByEndDateIsNullAndDueDateGreaterThanEqual: activos no vencidos (incluye due hoy)")
    void findByEndDateIsNullAndDueDateGreaterThanEqual_works() {
        LocalDate today = LocalDate.now();

        LoanEntity dueToday = persistLoan("11-1", today.minusDays(2), today, null, false);
        LoanEntity futureDue = persistLoan("11-1", today.minusDays(1), today.plusDays(3), null, false);
        LoanEntity overdue = persistLoan("11-1", today.minusDays(5), today.minusDays(1), null, false);

        List<LoanEntity> result = loanRepository.findByEndDateIsNullAndDueDateGreaterThanEqual(today);

        assertThat(result)
                .extracting(LoanEntity::getId)
                .contains(dueToday.getId(), futureDue.getId())
                .doesNotContain(overdue.getId());
    }
}
