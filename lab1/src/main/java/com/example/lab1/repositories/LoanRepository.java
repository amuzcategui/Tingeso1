package com.example.lab1.repositories;

import com.example.lab1.entities.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    LoanEntity findByid(long id);

    List<LoanEntity> findByrutCustomer(String rutCustomer);
    @Query("SELECT l FROM LoanEntity l WHERE l.toolNames IN :tools")
    List<LoanEntity> findByToolNamesIn(@Param("tools") List<String> tools);

    List<LoanEntity> findBystartDateBetween(LocalDateTime start, LocalDateTime end);

    List<LoanEntity> findBydueDateBetween(LocalDateTime start, LocalDateTime end);

    List<LoanEntity> findBystartDate(LocalDateTime date);

    List<LoanEntity> findBydueDate(LocalDateTime date);
    List<LoanEntity> findAll();

    boolean existsByRutCustomerAndEndDateIsNullAndDueDateBefore(String rutCustomer, LocalDate date);
    List<LoanEntity> findByRutCustomerAndEndDateIsNull(String rutCustomer);

    boolean existsByRutCustomerAndPaidIsFalse(String rutCustomer);
    boolean existsByRutCustomerAndPaidIsFalseAndEndDateNotNull(String rutCustomer);

    List<LoanEntity> findByEndDateIsNull();

    List<LoanEntity> findByEndDateIsNullAndDueDateBefore(LocalDate today);

    List<LoanEntity> findByEndDateIsNullAndDueDateGreaterThanEqual(LocalDate today);


}