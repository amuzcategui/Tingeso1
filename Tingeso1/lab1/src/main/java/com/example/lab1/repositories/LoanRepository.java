package com.example.lab1.repositories;

import com.example.lab1.entities.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    LoanEntity findByid(long id);


    List<LoanEntity> findByrutCustomer(String rutCustomer);
    @Query("SELECT l FROM LoanEntity l WHERE l.toolNames IN :tools")
    List<LoanEntity> findByToolNamesIn(@Param("tools") List<String> tools);

    // Buscar préstamos entre dos fechas de inicio
    List<LoanEntity> findBystartDateBetween(LocalDateTime start, LocalDateTime end);

    // Buscar préstamos entre dos fechas de vencimiento
    List<LoanEntity> findBydueDateBetween(LocalDateTime start, LocalDateTime end);

    // Buscar préstamos para una fecha exacta de inicio
    List<LoanEntity> findBystartDate(LocalDateTime date);

    // Buscar préstamos para una fecha exacta de vencimiento
    List<LoanEntity> findBydueDate(LocalDateTime date);


}