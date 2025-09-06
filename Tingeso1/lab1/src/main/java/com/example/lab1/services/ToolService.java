package com.example.lab1.services;

import com.example.lab1.entities.ToolEntity;
import com.example.lab1.entities.KardexEntity;
import com.example.lab1.repositories.ToolRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ToolService {
    @Autowired
    ToolRepository toolRepository;
    @Autowired
    KardexRepository kardexRepository;
    @Autowired
    CustomerRepository customerRepository;


    List<String> states= Arrays.asList("Disponible", "Prestada", "En reparación", "Dada de baja");
    public ToolEntity saveTool(ToolEntity tool, String rutAdmin) {

        if (tool.getName() == null || !states.contains(tool.getInitialState()) || tool.getValue() <= 0 || tool.getStock() <= 0) {
            throw new IllegalArgumentException("Datos inválidos para registrar la herramienta");
        }
        List<ToolEntity> existingTools = toolRepository.findByname(tool.getName());
        ToolEntity toolToSave = tool;
        boolean found = false;

        for (ToolEntity existing : existingTools) {
            if (existing.getInitialState().equals(tool.getInitialState())
                    && existing.getValue() == tool.getValue()
                    && existing.getCategory().equals(tool.getCategory())) {
                existing.setStock(existing.getStock() + tool.getStock());
                toolToSave = existing;
                found = true;
            }
        }

        ToolEntity savedTool = toolRepository.save(toolToSave);


        KardexEntity movement = new KardexEntity();
        movement.setRutCustomer(rutAdmin);
        movement.setMovementType("Ingreso");
        movement.setMovementDate(LocalDate.now());
        movement.setToolName(savedTool.getName());
        movement.setToolQuantity(tool.getStock());
        kardexRepository.save(movement);

        return savedTool;
    }

    //Dañadas (reparación) o en desuso (darle de baja)
//Estados válidos: Disponible, Prestada, En reparación, Dada de baja
    //Kardex-> INGRESO, PRESTAMO, DEVOLUCION, BAJA, REPARACION
    //BUSCAR CAMBIAR EL STATE (INITIAL STATE) Y EL CATEGORY
    //EVENTUALMENTE SE VA A QUITAR LO DEL IsAdmin, PORQUE SE PUEDE HACER POR FRONT
    public boolean deactivateTool(Long idTool, String rutCustomer, int quantityToDeactivate) {
        try {
            ToolEntity tool = toolRepository.findByid(idTool);

            if (quantityToDeactivate <= 0) {
                throw new IllegalArgumentException("La cantidad a dar de baja debe ser mayor que 0");
            }

            if (quantityToDeactivate > tool.getStock()) {
                throw new IllegalArgumentException("No se puede dar de baja más unidades de las existentes");
            }

            // Reducir stock
            tool.setStock(tool.getStock() - quantityToDeactivate);

            // Cambiar estado si se da de baja todo el stock
            if (tool.getStock() == 0) {
                tool.setInitialState("Dada de baja");
            }
        //PROBAR BIEN ESTO, NO SE SI SE ESTÁ CREANDO LA HERRAMIENTA CON EL NUEVO ESTADO O SI SOLO SE ESTÁ BAJANDO EL STOCK
            toolRepository.save(tool);

            ToolEntity dtool = new ToolEntity();
            dtool.setName(tool.getName());
            dtool.setCategory(tool.getCategory());
            dtool.setValue(tool.getValue());
            dtool.setInitialState("Dada de baja");
            dtool.setStock(quantityToDeactivate);
            toolRepository.save(dtool);

            // Registrar movimiento en Kardex
            KardexEntity movement = new KardexEntity();
            movement.setRutCustomer(rutCustomer);
            movement.setMovementType("Baja");
            movement.setMovementDate(LocalDate.now());
            movement.setToolName(tool.getName());
            movement.setToolQuantity(quantityToDeactivate); // registramos solo la cantidad dada de baja
            kardexRepository.save(movement);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
