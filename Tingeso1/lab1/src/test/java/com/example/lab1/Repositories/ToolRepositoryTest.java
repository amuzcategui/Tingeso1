package com.example.lab1.Repositories;

import com.example.lab1.entities.ToolEntity;
import com.example.lab1.repositories.ToolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ToolRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ToolRepository toolRepository;

    private ToolEntity newTool(String name, String category, String initialState, double value, int stock) {
        ToolEntity t = new ToolEntity();
        t.setName(name);
        t.setCategory(category);
        t.setInitialState(initialState);
        t.setToolValue(value);
        t.setStock(stock);
        return t;
    }

    private ToolEntity persist(ToolEntity t) {
        em.persist(t);
        em.flush();
        return t;
    }

    @Test
    @DisplayName("findByid devuelve la herramienta correcta cuando existe")
    void findByid_returnsTool_whenExists() {
        persist(newTool("Taladro", "Electricas", "Disponible", 100.0, 5));
        ToolEntity saved = persist(newTool("Martillo", "Manuales", "Disponible", 150.0, 10));
        persist(newTool("Sierra", "Electricas", "En reparación", 100.0, 2));

        ToolEntity found = toolRepository.findByid(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("Martillo", found.getName());
        assertEquals("Manuales", found.getCategory());
        assertEquals(150.0, found.getToolValue());
    }

    @Test
    @DisplayName("findByid retorna null cuando no existe")
    void findByid_returnsNull_whenNotExists() {
        persist(newTool("Taladro", "Electricas", "Disponible", 100.0, 5));

        ToolEntity found = toolRepository.findByid(9999L);

        assertNull(found);
    }

    @Test
    @DisplayName("findBycategory retorna todas las herramientas de esa categoría")
    void findBycategory_returnsAllWithCategory() {
        persist(newTool("Taladro", "Electricas", "Disponible", 100.0, 5));
        persist(newTool("Sierra", "Electricas", "En reparación", 200.0, 2));
        persist(newTool("Martillo", "Manuales", "Disponible", 150.0, 10));

        List<ToolEntity> electricas = toolRepository.findBycategory("Electricas");

        assertEquals(2, electricas.size());
        assertTrue(electricas.stream().allMatch(t -> "Electricas".equals(t.getCategory())));
    }

    @Test
    @DisplayName("findByname retorna todas las herramientas con ese nombre")
    void findByname_returnsAllWithName() {
        persist(newTool("Martillo", "Manuales", "Disponible", 150.0, 10));
        persist(newTool("Martillo", "Demolición", "Prestada", 180.0, 3));
        persist(newTool("Taladro", "Electricas", "Disponible", 100.0, 5));

        List<ToolEntity> martillos = toolRepository.findByname("Martillo");

        assertEquals(2, martillos.size());
        assertTrue(martillos.stream().allMatch(t -> "Martillo".equals(t.getName())));
    }

    @Test
    @DisplayName("findByvalue retorna todas las herramientas con ese valor exacto")
    void findByvalue_returnsAllWithExactValue() {
        persist(newTool("Taladro", "Electricas", "Disponible", 100.0, 5));
        persist(newTool("Sierra", "Electricas", "Disponible", 100.0, 2));
        persist(newTool("Martillo", "Manuales", "Disponible", 150.0, 10));

        // El repo expone findByvalue(int), la entidad usa double: Spring Data convierte 100 -> 100.0
        List<ToolEntity> value100 = toolRepository.findBytoolValue(100);

        assertEquals(2, value100.size());
        assertTrue(value100.stream().allMatch(t -> t.getToolValue() == 100.0));
    }
}
