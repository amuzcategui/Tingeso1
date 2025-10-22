package com.example.lab1.Services;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.services.CustomerService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private LoanRepository loanRepository;

    @InjectMocks private CustomerService customerService;


    private CustomerEntity customer(String rut, String name, String email, String phone, int qtyLoans, String status) {
        CustomerEntity c = new CustomerEntity();
        c.setRut(rut);
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setQuantityLoans(qtyLoans);
        c.setStatus(status);
        return c;
    }


    @Nested
    class CreateCustomer {
        @Test
        void creates_when_all_required_fields_present() {
            CustomerEntity input = customer("1-9","Alice","a@x.com","123",0,null);
            when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(i -> i.getArgument(0));
            CustomerEntity saved = customerService.createCustomer(input);
            assertSame(input, saved);
            verify(customerRepository).save(input);
        }

        @Test
        void throws_when_missing_any_required_field() {
            assertThrows(IllegalArgumentException.class,
                    () -> customerService.createCustomer(customer("1", null, "a@x.com","123",0,null)));
            assertThrows(IllegalArgumentException.class,
                    () -> customerService.createCustomer(customer(null,"A","a@x.com","123",0,null)));
            assertThrows(IllegalArgumentException.class,
                    () -> customerService.createCustomer(customer("1","A","a@x.com",null,0,null)));
            assertThrows(IllegalArgumentException.class,
                    () -> customerService.createCustomer(customer("1","A",null,"123",0,null)));
            verifyNoInteractions(customerRepository);
        }
    }


    @Nested
    class UpdateRestriction {
        @Test
        void sets_restringido_when_has_overdue_active() {
            CustomerEntity c = customer("11-1","Bob","b@x.com","999",0,"Activo");
            when(loanRepository.existsByRutCustomerAndEndDateIsNullAndDueDateBefore(eq("11-1"), any())).thenReturn(true);
            customerService.updateRestriction(c);
            assertEquals("Restringido", c.getStatus());
            verify(customerRepository).save(c);
        }

        @Test
        void sets_restringido_when_has_unpaid_debts() {
            CustomerEntity c = customer("22-2","Caro","c@x.com","999",0,"Activo");
            when(loanRepository.existsByRutCustomerAndPaidIsFalseAndEndDateNotNull("22-2")).thenReturn(true);
            customerService.updateRestriction(c);
            assertEquals("Restringido", c.getStatus());
            verify(customerRepository).save(c);
        }

        @Test
        void sets_restringido_when_quantity_loans_ge_5() {
            CustomerEntity c = customer("33-3","Dan","d@x.com","999",5,"Activo");
            customerService.updateRestriction(c);
            assertEquals("Restringido", c.getStatus());
            verify(customerRepository).save(c);
        }

        @Test
        void sets_activo_when_no_issues() {
            CustomerEntity c = customer("44-4","Eve","e@x.com","999",2,"Restringido");
            when(loanRepository.existsByRutCustomerAndEndDateIsNullAndDueDateBefore(eq("44-4"), any())).thenReturn(false);

            customerService.updateRestriction(c);
            assertEquals("Activo", c.getStatus());
            verify(customerRepository).save(c);
        }
    }

    @Nested
    class CheckAndCreateCustomer {

        @Test
        void returnsExistingCustomer_whenFound() {

            Jwt mockJwt = mock(Jwt.class);
            when(mockJwt.getClaimAsString("rut")).thenReturn("1-9");

            CustomerEntity existingCustomer = customer("1-9", "Alice", "a@x.com", "123", 0, "Activo");
            when(customerRepository.findByrut("1-9")).thenReturn(existingCustomer);


            CustomerEntity result = customerService.checkAndCreateCustomer(mockJwt);


            assertSame(existingCustomer, result);
            verify(customerRepository, never()).save(any(CustomerEntity.class));
        }

        @Test
        void createsNewAdminCustomer_whenNotFound_withAllClaims() {

            Jwt mockJwt = mock(Jwt.class);
            when(mockJwt.getClaimAsString("rut")).thenReturn("2-7");
            when(mockJwt.getClaimAsString("email")).thenReturn("admin@test.com");
            when(mockJwt.getClaimAsString("given_name")).thenReturn("Admin");
            when(mockJwt.getClaimAsString("family_name")).thenReturn("User");
            when(mockJwt.getClaimAsString("phone")).thenReturn("123456789");
            when(mockJwt.getClaimAsString("birthdate")).thenReturn("1990-01-15");


            Map<String, Object> realmAccess = Map.of("roles", List.of("USER", "ADMIN"));
            when(mockJwt.getClaimAsMap("realm_access")).thenReturn(realmAccess);


            when(customerRepository.findByrut("2-7")).thenReturn(null);
            when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(i -> i.getArgument(0));


            CustomerEntity newCustomer = customerService.checkAndCreateCustomer(mockJwt);


            assertEquals("2-7", newCustomer.getRut());
            assertEquals("Admin User", newCustomer.getName());
            assertEquals(LocalDate.of(1990, 1, 15), newCustomer.getBirthDate());
            assertTrue(newCustomer.isAdmin());
            assertEquals("Activo", newCustomer.getStatus());
            verify(customerRepository).save(any(CustomerEntity.class));
        }

        @Test
        void createsNewNonAdminCustomer_whenNotFound_withMinimumClaims() {

            Jwt mockJwt = mock(Jwt.class);
            when(mockJwt.getClaimAsString("rut")).thenReturn("3-5");
            when(mockJwt.getClaimAsString("email")).thenReturn("user@test.com");
            when(mockJwt.getClaimAsString("given_name")).thenReturn("Regular");
            when(mockJwt.getClaimAsString("family_name")).thenReturn("User");
            when(mockJwt.getClaimAsString("phone")).thenReturn("987654321");
            when(mockJwt.getClaimAsString("birthdate")).thenReturn(null);


            Map<String, Object> realmAccess = Map.of("roles", List.of("USER"));
            when(mockJwt.getClaimAsMap("realm_access")).thenReturn(realmAccess);


            when(customerRepository.findByrut("3-5")).thenReturn(null);
            when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(i -> i.getArgument(0));


            CustomerEntity newCustomer = customerService.checkAndCreateCustomer(mockJwt);


            assertEquals("3-5", newCustomer.getRut());
            assertNull(newCustomer.getBirthDate());
            assertFalse(newCustomer.isAdmin());
            verify(customerRepository).save(any(CustomerEntity.class));
        }
    }
}