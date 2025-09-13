package com.example.lab1.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//Es el núcleo del sistema, encargado de
//controlar todo el ciclo de vida de un préstamo: desde la entrega de herramientas a los clientes,
//hasta su devolución, cálculo de multas por atrasos y penalizaciones por daños. Incluye
//validaciones de disponibilidad, restricciones a clientes con deudas, y actualización automática
//de stock y estados.
@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private long id;

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate endDate;
    private String rutCustomer;
    private List<String> toolNames;
    private double rentalFee;
    private double fine;
    private List<String> damagedTools;
    private List<String> discardedTools;

}
