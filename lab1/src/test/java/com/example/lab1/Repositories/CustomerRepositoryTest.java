package com.example.lab1.Repositories;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.repositories.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    // ✅ CAMBIO: Ya no se necesita el TestEntityManager
    // @Autowired
    // private TestEntityManager em;

    @Autowired
    private CustomerRepository customerRepository;

    private CustomerEntity newCustomer(String rut, String name, String status, int loans) {
        CustomerEntity c = new CustomerEntity();
        c.setRut(rut);
        c.setName(name);
        c.setStatus(status);
        c.setQuantityLoans(loans);
        // Asignamos valores a campos que podrían ser no nulos para evitar errores
        c.setEmail("test@example.com");
        c.setPhone("123456789");
        return c;
    }

    // ✅ CAMBIO: Se elimina el método persist
    // private CustomerEntity persist(CustomerEntity c) { ... }

    @Test
    @DisplayName("findByrut devuelve el cliente correcto cuando existe")
    void findByrut_returnsCustomer_whenExists() {
        // ✅ CAMBIO: Usamos .save() en lugar de persist()
        customerRepository.save(newCustomer("1-9", "Ana", "Activo", 2));
        CustomerEntity saved = customerRepository.save(newCustomer("2-7", "Ana", "Restringido", 5));
        customerRepository.save(newCustomer("3-5", "Luis", "Suspendido", 0));

        CustomerEntity found = customerRepository.findByrut("2-7");

        assertNotNull(found);
        assertEquals(saved.getRut(), found.getRut());
        assertEquals("Restringido", found.getStatus());
        assertEquals("Ana", found.getName());
        assertEquals(5, found.getQuantityLoans());
    }

    @Test
    @DisplayName("findByrut retorna null cuando no existe")
    void findByrut_returnsNull_whenNotExists() {
        // ✅ CAMBIO: Usamos .save()
        customerRepository.save(newCustomer("1-9", "Ana", "Activo", 2));

        CustomerEntity found = customerRepository.findByrut("99-9");

        assertNull(found);
    }

    @Test
    @DisplayName("findBystatus retorna todos los clientes con ese status")
    void findBystatus_returnsAllWithStatus() {
        // ✅ CAMBIO: Usamos .save()
        customerRepository.save(newCustomer("1-9", "Ana", "Activo", 2));
        customerRepository.save(newCustomer("2-7", "Ana", "Restringido", 5));
        customerRepository.save(newCustomer("3-5", "Luis", "Activo", 0));

        List<CustomerEntity> activos = customerRepository.findBystatus("Activo");

        assertEquals(2, activos.size());
        assertTrue(activos.stream().allMatch(c -> "Activo".equals(c.getStatus())));
    }

    @Test
    @DisplayName("findByname retorna todos los clientes con ese nombre")
    void findByname_returnsAllWithName() {
        // ✅ CAMBIO: Usamos .save()
        customerRepository.save(newCustomer("1-9", "Ana", "Activo", 2));
        customerRepository.save(newCustomer("2-7", "Ana", "Restringido", 5));
        customerRepository.save(newCustomer("3-5", "Luis", "Suspendido", 0));

        List<CustomerEntity> anas = customerRepository.findByname("Ana");

        assertEquals(2, anas.size());
        assertTrue(anas.stream().allMatch(c -> "Ana".equals(c.getName())));
    }

    @Test
    @DisplayName("findByquantityLoansGreaterThan retorna los clientes con loans estrictamente mayores")
    void findByquantityLoansGreaterThan_returnsAboveThreshold() {
        // ✅ CAMBIO: Usamos .save()
        customerRepository.save(newCustomer("1-9", "Ana", "Activo", 2));
        customerRepository.save(newCustomer("2-7", "Ana", "Restringido", 5));
        customerRepository.save(newCustomer("3-5", "Luis", "Suspendido", 0));

        List<CustomerEntity> gt1 = customerRepository.findByquantityLoansGreaterThan(1);
        List<CustomerEntity> gt2 = customerRepository.findByquantityLoansGreaterThan(2);
        List<CustomerEntity> gt5 = customerRepository.findByquantityLoansGreaterThan(5);

        assertEquals(2, gt1.size(), "Mayor a 1 debería traer 2 (2 y 5)");
        assertEquals(1, gt2.size(), "Mayor a 2 debería traer 1 (5)");
        assertEquals(0, gt5.size(), "Mayor a 5 debería traer 0");
    }
}