package com.example.lab1.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

//(nombre, categoría, estado
//inicial, valor de reposición)
//Estados válidos: Disponible, Prestada, En reparación, Dada de baja
// Solo los Administradores pueden dar de baja herramientas.
@Entity
@Table(name = "tools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String name;
    private double value;
    private String initialState;
    private String category;
    private int stock;
}
