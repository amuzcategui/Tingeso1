package com.example.lab1.controllers;

import com.example.lab1.entities.KardexEntity;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.services.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kardex")
@CrossOrigin("*")
public class KardexController {

    @Autowired
    private KardexRepository kardexRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private KardexService kardexService;

    // RF5.2: History by tool
    @GetMapping("/tool-history")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> toolHistory(
            @RequestParam String toolName
    ) {
        try {
            if (toolName == null || toolName.isBlank()) {
                return ResponseEntity.badRequest().body("toolName es requerido");
            }
            List<KardexEntity> result;
            result = kardexRepository.findBytoolName(toolName);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // RF5.3: Movements by range
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> movementsInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam String movementType
    ) {
        try {
            if (to.isBefore(from)) {
                return ResponseEntity.badRequest().body("El rango de fechas es inválido (to < from)");
            }

            List<KardexEntity> result;
            result = kardexRepository.findByMovementTypeAndMovementDateBetween(
                    movementType, from, to);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/loans/active/grouped")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> activeLoansGrouped() {
        try {
            return ResponseEntity.ok(kardexService.listActiveLoansGrouped());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/tools/top")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> topTools(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer limit
    ) {
        try {
            if (from != null && to != null && to.isBefore(from)) {
                return ResponseEntity.badRequest().body("El rango de fechas es inválido (to < from)");
            }

            List<Object[]> result = kardexService.topLoanedTools(from, to, limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> allKardex() {
        try{
            List<KardexEntity> result = kardexRepository.findAll();
            return ResponseEntity.ok(result);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
