package com.example.lab1.Repositories;

import com.example.lab1.entities.KardexEntity;
import com.example.lab1.repositories.KardexRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class KardexRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KardexRepository kardexRepository;

    // Helper para persistir más fácil
    private KardexEntity persist(String rut, String movementType, LocalDate date, String toolName, int qty) {
        KardexEntity e = new KardexEntity(null, rut, movementType, date, toolName, qty);
        entityManager.persist(e);
        entityManager.flush();
        return e;
    }

    @Test
    @DisplayName("findByid retorna el movimiento cuando existe")
    void findByid_returnsEntity_whenExists() {
        KardexEntity saved = persist("11-1", "ingreso", LocalDate.of(2025, 1, 10), "Taladro", 5);

        KardexEntity found = kardexRepository.findByid(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getMovementType()).isEqualTo("ingreso");
        assertThat(found.getToolName()).isEqualTo("Taladro");
        assertThat(found.getToolQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("findByid retorna null cuando no existe")
    void findByid_returnsNull_whenNotExists() {
        KardexEntity found = kardexRepository.findByid(999L);
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("findBymovementType retorna todos con el tipo indicado")
    void findBymovementType_returnsAllWithType() {
        persist("11-1", "ingreso", LocalDate.of(2025, 1, 10), "Taladro", 5);
        persist("22-2", "prestamo", LocalDate.of(2025, 1, 11), "Taladro", 1);
        persist("33-3", "ingreso", LocalDate.of(2025, 1, 12), "Serrucho", 3);

        List<KardexEntity> ingresos = kardexRepository.findBymovementType("ingreso");

        assertThat(ingresos).hasSize(2);
        assertThat(ingresos).extracting(KardexEntity::getMovementType).containsOnly("ingreso");
    }

    @Test
    @DisplayName("findByrutCustomer retorna todos del cliente (RUT) indicado")
    void findByrutCustomer_returnsAllForRut() {
        persist("11-1", "ingreso", LocalDate.of(2025, 1, 10), "Taladro", 5);
        persist("11-1", "prestamo", LocalDate.of(2025, 1, 11), "Taladro", 1);
        persist("22-2", "devolucion", LocalDate.of(2025, 1, 12), "Serrucho", 3);

        List<KardexEntity> movimientos = kardexRepository.findByrutCustomer("11-1");

        assertThat(movimientos).hasSize(2);
        assertThat(movimientos).allMatch(k -> "11-1".equals(k.getRutCustomer()));
    }

    @Test
    @DisplayName("findBytoolName retorna todos para la herramienta indicada")
    void findBytoolName_returnsAllForTool() {
        persist("11-1", "ingreso", LocalDate.of(2025, 1, 10), "Taladro", 5);
        persist("22-2", "prestamo", LocalDate.of(2025, 1, 11), "Taladro", 1);
        persist("33-3", "ingreso", LocalDate.of(2025, 1, 12), "Serrucho", 3);

        List<KardexEntity> taladros = kardexRepository.findBytoolName("Taladro");

        assertThat(taladros).hasSize(2);
        assertThat(taladros).allMatch(k -> "Taladro".equals(k.getToolName()));
    }

    @Test
    @DisplayName("findByMovementTypeAndMovementDateBetween filtra por tipo y rango de fechas")
    void findByMovementTypeAndMovementDateBetween_filtersByTypeAndDateRange() {
        // Dentro del rango
        persist("11-1", "prestamo", LocalDate.of(2025, 1, 10), "Taladro", 1);
        persist("22-2", "prestamo", LocalDate.of(2025, 1, 12), "Serrucho", 2);
        // Fuera del rango o de otro tipo
        persist("33-3", "prestamo", LocalDate.of(2024, 12, 31), "Serrucho", 2);
        persist("44-4", "ingreso",  LocalDate.of(2025, 1, 11), "Taladro", 5);

        List<KardexEntity> encontrados = kardexRepository
                .findByMovementTypeAndMovementDateBetween(
                        "prestamo",
                        LocalDate.of(2025, 1, 9),
                        LocalDate.of(2025, 1, 12)
                );

        assertThat(encontrados).hasSize(2);
        assertThat(encontrados).allSatisfy(k -> {
            assertThat(k.getMovementType()).isEqualTo("prestamo");
            assertThat(k.getMovementDate()).isBetween(LocalDate.of(2025, 1, 9), LocalDate.of(2025, 1, 12));
        });
    }
}
