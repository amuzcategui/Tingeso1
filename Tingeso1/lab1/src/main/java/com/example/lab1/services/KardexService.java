package com.example.lab1.services;


import com.example.lab1.entities.KardexEntity;
import com.example.lab1.repositories.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class KardexService {
    @Autowired
    private KardexRepository kardexRepository;

    public KardexEntity kardex(String rut, String type, String toolName, int quantity) {
        KardexEntity k = new KardexEntity();
        k.setRutCustomer(rut);
        k.setMovementType(type);
        k.setMovementDate(LocalDate.now());
        k.setToolName(toolName);
        k.setToolQuantity(quantity);
        return k;
    }
}
