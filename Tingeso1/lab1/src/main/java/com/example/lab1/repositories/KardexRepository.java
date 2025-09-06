package com.example.lab1.repositories;

import com.example.lab1.entities.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {
    KardexEntity findByid(long id);

    List<KardexEntity> findBymovementType(String movement);
    List<KardexEntity> findByrutCustomer(String rutCustomer);
    List<KardexEntity> findBytoolName(String toolName);

}
