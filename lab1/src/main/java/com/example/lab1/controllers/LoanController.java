package com.example.lab1.controllers;

import com.example.lab1.entities.LoanEntity;
import com.example.lab1.entities.ToolEntity;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loan")
@CrossOrigin("*")
public class LoanController {
    @Autowired
    LoanService loanService;

    @Autowired
    LoanRepository loanRepository;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> createLoan(@RequestBody LoanEntity loan) {
        try {
            LoanEntity savedLoan = loanService.createLoan(
                    loan,
                    loan.getRutCustomer(),
                    loan.getToolNames(),
                    loan.getStartDate(),
                    loan.getDueDate()
            );
            return ResponseEntity.ok(savedLoan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/return/{idLoan}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> returnTools(
            @PathVariable long idLoan,
            @RequestParam(defaultValue = "0") double dailyLateFee,
            @RequestParam(defaultValue = "0") double repairCost,
            @RequestParam(value = "damaged", required = false) List<String> damagedTools,
            @RequestParam(value = "discarded", required = false) List<String> discardedTools) {

        try {
            LoanEntity updatedLoan = loanService.returnTools(
                    idLoan,
                    dailyLateFee,
                    repairCost,
                    damagedTools != null ? damagedTools : new ArrayList<>(),
                    discardedTools != null ? discardedTools : new ArrayList<>()
            );
            return ResponseEntity.ok(updatedLoan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{idLoan}/pay")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> payLoan(@PathVariable long idLoan) {
        try {
            LoanEntity updated = loanService.markLoanAsPaid(idLoan);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-loans")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LoanEntity>> getMyLoans(@RequestParam String rut) {
        try {

            List<LoanEntity> loans = loanService.findLoansByCustomerRut(rut);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {

            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/all-loans")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<LoanEntity>> getAllLoans() {
        try {

            List<LoanEntity> loans = loanRepository.findAll();
            return ResponseEntity.ok(loans);
        } catch (Exception e) {

            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{idLoan}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> getLoanById(@PathVariable long idLoan) {
        try {
            // Find the loan or throw an error if it doesnt exist
            LoanEntity loan = loanRepository.findById(idLoan)
                    .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado con ID: " + idLoan));
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
