package com.example.lab1.services;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.entities.KardexEntity;
import com.example.lab1.entities.LoanEntity;
import com.example.lab1.entities.ToolEntity;

import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.repositories.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private ToolRepository toolRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private KardexRepository kardexRepository;
//    private LocalDateTime startDate;
//    private LocalDateTime dueDate;
//    private LocalDateTime endDate;

// SI MI PRIMER COINCIDENCIA DE TOOL NO TIENE EL STOCK NECESARIO, QUE PASA??
    //VER BIEN LAS CONDICIONALES
    //VER SI SE DEJA LA BÚSQUEDA DE LAS TOOL CON EL NOMBRE O CON LA CATEGORIA
public LoanEntity createLoan(
        LoanEntity loan,
        String rutCustomer,
        List<String> toolNames,
        LocalDate startDate,
        LocalDate dueDate) {

    // Buscar cliente
    CustomerEntity customer = customerRepository.findByrut(rutCustomer);
    if (customer == null) {
        throw new IllegalArgumentException("Cliente no encontrado");
    }
    if (!"Activo".equalsIgnoreCase(customer.getStatus())) {
        throw new IllegalArgumentException("El cliente no está activo");
    }

    // Validar fechas
    if (dueDate.isBefore(startDate)) {
        throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la de inicio");
    }

    List<String> namesForLoan = new ArrayList<>();

    // Procesar cada herramienta
    for (String toolName : toolNames) {
        List<ToolEntity> tools = toolRepository.findByname(toolName);
        boolean found = false;

        for (ToolEntity t : tools) {
            if (!found && "Disponible".equalsIgnoreCase(t.getInitialState()) && t.getStock() >= 1) {
                // Reducir stock de la herramienta original
                t.setStock(t.getStock() - 1);
                toolRepository.save(t);

                // Crear nueva ToolEntity para el préstamo
                ToolEntity loanedTool = new ToolEntity();
                loanedTool.setName(t.getName());
                loanedTool.setCategory(t.getCategory());
                loanedTool.setValue(t.getValue());
                loanedTool.setInitialState("Prestada");
                loanedTool.setStock(1);
                toolRepository.save(loanedTool);

                // Guardar nombre para el préstamo
                namesForLoan.add(loanedTool.getName());

                // Registrar movimiento en Kardex
                KardexEntity movement = new KardexEntity();
                movement.setRutCustomer(rutCustomer);
                movement.setMovementType("PRESTAMO");
                movement.setMovementDate(LocalDate.now());
                movement.setToolName(loanedTool.getName());
                movement.setToolQuantity(1);
                kardexRepository.save(movement);

                found = true;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("No hay stock disponible para la herramienta: " + toolName);
        }
    }

    // Actualizar cantidad de préstamos del cliente
    customer.setQuantityLoans(customer.getQuantityLoans() + toolNames.size());
    customerRepository.save(customer);

    // Crear el préstamo
    loan.setRutCustomer(rutCustomer);
    loan.setToolNames(namesForLoan);
    loan.setStartDate(startDate);
    loan.setDueDate(dueDate);
    loan.setFine(0.0);
    loan.setRentalFee(0.0);

    return loanRepository.save(loan);
}







    //Requisitos Funcionales
    //• RF2.1 Registrar un préstamo asociando cliente y herramienta, con fecha de entrega y
    //fecha pactada de devolución. Se actualiza el kardex.
    //• RF2.2 Validar disponibilidad antes de autorizar el préstamo. <- list y length.list
    //• RF2.3 Registrar devolución de herramienta, actualizando estado y stock. Se actualiza
    //el kardex.
    //• RF2.4 Calcular automáticamente multas por atraso (tarifa diaria).
    //• RF2.5 Bloquear nuevos préstamos a clientes con atrasos no regularizados.
    //Reglas de Negocio
    //• Condiciones para realizar un préstamo
    //• El cliente debe estar en estado Activo (no restringido).
    //• El cliente no debe tener:
    //o Préstamos vencidos.
    //o Multas impagas.
    //o Deudas por reposición de herramientas.
    //• La herramienta debe estar en estado Disponible y tener stock ≥ 1.
    //• No se permite prestar más herramientas de las disponibles en stock.
    //• El sistema debe verificar que la fecha de devolución no sea anterior a la fecha de
    //entrega.
}
