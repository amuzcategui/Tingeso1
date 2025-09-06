package com.example.lab1.controllers;

import com.example.lab1.entities.LoanEntity;
import com.example.lab1.entities.ToolEntity;
import com.example.lab1.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan")
@CrossOrigin("*")
public class LoanController {
    @Autowired
    LoanService loanService;
//          LoanEntity loan,
//        String rutCustomer,
//        List<String> toolNames,
//        LocalDateTime startDate,
//        LocalDateTime dueDate
    @PostMapping("/create")
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

}
