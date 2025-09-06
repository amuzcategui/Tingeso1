package com.example.lab1.repositories;

import com.example.lab1.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    public CustomerEntity findByrut(String rut);

    List<CustomerEntity> findBystatus(String status);
    List<CustomerEntity> findByname(String name);
    List<CustomerEntity> findByquantityLoansGreaterThan(int loans);


}
