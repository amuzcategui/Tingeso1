package com.example.lab1.services;

import com.example.lab1.entities.ToolEntity;
import com.example.lab1.entities.KardexEntity;
import com.example.lab1.repositories.ToolRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        if (tool.getName() == null || tool.getToolValue() <= 0 || tool.getStock() <= 0) {
            throw new IllegalArgumentException("Datos inválidos para registrar la herramienta");
        }
        List<ToolEntity> existingTools = toolRepository.findByname(tool.getName());
        ToolEntity toolToSave = tool;
        boolean found = false;

        for (ToolEntity existing : existingTools) {
            if (existing.getInitialState().equals(tool.getInitialState())
                    && existing.getToolValue() == tool.getToolValue()
                    && existing.getCategory().equals(tool.getCategory())) {
                existing.setStock(existing.getStock() + tool.getStock());
                toolToSave = existing;
                found = true;
            }
        }

        if (!found) {
            toolToSave.setInitialState("Disponible");
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


    public ToolEntity deactivateTool(Long idTool, String rutCustomer, int quantityToDeactivate) {
        try {
            ToolEntity tool = toolRepository.findByid(idTool);

            if (quantityToDeactivate <= 0) {
                throw new IllegalArgumentException("La cantidad a dar de baja debe ser mayor que 0");
            }

            if (quantityToDeactivate > tool.getStock()) {
                throw new IllegalArgumentException("No se puede dar de baja más unidades de las existentes");
            }

            // Reduce stock
            tool.setStock(tool.getStock() - quantityToDeactivate);

            // change status
            if (tool.getStock() == 0) {
                tool.setInitialState("Dada de baja");
            }

            toolRepository.save(tool);

            ToolEntity dtool = new ToolEntity();
            dtool.setName(tool.getName());
            dtool.setCategory(tool.getCategory());
            dtool.setToolValue((tool.getToolValue()));
            dtool.setInitialState("Dada de baja");
            dtool.setStock(quantityToDeactivate);
            toolRepository.save(dtool);

            // Kardex movement
            KardexEntity movement = new KardexEntity();
            movement.setRutCustomer(rutCustomer);
            movement.setMovementType("Baja");
            movement.setMovementDate(LocalDate.now());
            movement.setToolName(tool.getName());
            movement.setToolQuantity(quantityToDeactivate);
            kardexRepository.save(movement);

            return tool;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ToolEntity availableTool(Long idTool, String rutCustomer, int quantityToActivate) {
        try {
            ToolEntity tool = toolRepository.findByid(idTool);

            if (quantityToActivate <= 0) {
                throw new IllegalArgumentException("La cantidad de activar debe ser mayor que 0");
            }

            List<ToolEntity> existingTools = toolRepository.findByname(tool.getName());
            ToolEntity toolToSave = tool;
            boolean found = false;

            for (ToolEntity existing : existingTools) {
                if (existing.getInitialState().equals("Disponible")
                        && existing.getToolValue() == tool.getToolValue()
                        && existing.getCategory().equals(tool.getCategory())) {
                    existing.setStock(existing.getStock() + quantityToActivate);
                    toolToSave = existing;

                    found = true;
                }
            }

            toolRepository.save(toolToSave);


            tool.setStock(tool.getStock() - quantityToActivate);
            if (tool.getStock() <= 0) {
                toolRepository.delete(tool);
            } else {
                toolRepository.save(tool);
            }

            // Movement
            KardexEntity movement = new KardexEntity();
            movement.setRutCustomer(rutCustomer);
            movement.setMovementType("Devolución");
            movement.setMovementDate(LocalDate.now());
            movement.setToolName(tool.getName());
            movement.setToolQuantity(quantityToActivate);
            kardexRepository.save(movement);

            return tool;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ToolEntity loanTool(Long idTool, String rutCustomer, int quantityToLoan) {
        try {
            ToolEntity tool = toolRepository.findByid(idTool);

            if (quantityToLoan <= 0) {
                throw new IllegalArgumentException("La cantidad a dar de baja debe ser mayor que 0");
            }

            if (quantityToLoan > tool.getStock()) {
                throw new IllegalArgumentException("No se puede dar de préstamo más unidades de las existentes");
            }

            // Reduce stock
            tool.setStock(tool.getStock() - quantityToLoan);

            toolRepository.save(tool);



            ToolEntity dtool = new ToolEntity();
            dtool.setName(tool.getName());
            dtool.setCategory(tool.getCategory());
            dtool.setToolValue(tool.getToolValue());
            dtool.setInitialState("Prestada");
            dtool.setStock(quantityToLoan);
            toolRepository.save(dtool);

            // Kardex movement
            KardexEntity movement = new KardexEntity();
            movement.setRutCustomer(rutCustomer);
            movement.setMovementType("Préstamo");
            movement.setMovementDate(LocalDate.now());
            movement.setToolName(tool.getName());
            movement.setToolQuantity(quantityToLoan);
            kardexRepository.save(movement);

            return tool;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ToolEntity repairTool(Long idTool, String rutCustomer, int quantityToRepair) {
        try {
            ToolEntity tool = toolRepository.findByid(idTool);

            if (quantityToRepair <= 0) {
                throw new IllegalArgumentException("La cantidad a dar de baja debe ser mayor que 0");
            }

            if (quantityToRepair > tool.getStock()) {
                throw new IllegalArgumentException("No se puede reparar más unidades de las existentes");
            }


            tool.setStock(tool.getStock() - quantityToRepair);


            if (tool.getStock() == 0) {
                tool.setInitialState("Dada de baja");
            }

            toolRepository.save(tool);

            ToolEntity rtool = new ToolEntity();
            rtool.setName(tool.getName());
            rtool.setCategory(tool.getCategory());
            rtool.setToolValue(tool.getToolValue());
            rtool.setInitialState("En reparación");
            rtool.setStock(quantityToRepair);
            toolRepository.save(rtool);

            KardexEntity movement = new KardexEntity();
            movement.setRutCustomer(rutCustomer);
            movement.setMovementType("reparación");
            movement.setMovementDate(LocalDate.now());
            movement.setToolName(tool.getName());
            movement.setToolQuantity(quantityToRepair);
            kardexRepository.save(movement);

            return tool;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ToolEntity updateReplacementValue(Long idTool, double newValue) {
        ToolEntity tool = toolRepository.findByid(idTool);
        if (tool == null) throw new IllegalArgumentException("Herramienta no encontrada");
        if (newValue <= 0) throw new IllegalArgumentException("Valor de reposición inválido");
        tool.setToolValue(newValue);
        return toolRepository.save(tool);
    }

    public ToolEntity updateFee(Long idTool, double newValue) {
        ToolEntity tool = toolRepository.findByid(idTool);
        if (tool == null) throw new IllegalArgumentException("Herramienta no encontrada");
        if (newValue <= 0) throw new IllegalArgumentException("Valor de Fee inválido");
        tool.setRentalFee(newValue);
        return toolRepository.save(tool);
    }




}
