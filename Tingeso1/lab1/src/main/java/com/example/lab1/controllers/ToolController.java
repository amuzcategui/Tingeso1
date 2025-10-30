package com.example.lab1.controllers;

import com.example.lab1.entities.ToolEntity;
import com.example.lab1.repositories.ToolRepository;
import com.example.lab1.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tool")
@CrossOrigin("*")
public class ToolController {
    @Autowired
    ToolService toolService;
    @Autowired
    ToolRepository toolRepository;

    // save a tool
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> saveTool(
            @RequestBody ToolEntity tool,
            @RequestParam String rutAdmin) {
        try {
            ToolEntity saved = toolService.saveTool(tool, rutAdmin);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // changes status -> deactive
    @PutMapping("/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> deactivateTool(
            @RequestParam Long idTool,
            @RequestParam String rutCustomer,
            @RequestParam int quantityToDeactivate) {
        try {
            ToolEntity result = toolService.deactivateTool(idTool, rutCustomer, quantityToDeactivate);
            return ResponseEntity.ok(result);
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //changes status -> available
    @PutMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> availableTool(
            @RequestParam Long idTool,
            @RequestParam String rutCustomer,
            @RequestParam int quantityToActivate) {
        try {
            ToolEntity result = toolService.availableTool(idTool, rutCustomer, quantityToActivate);
            return ResponseEntity.ok(result);
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // available -> loaned
    @PutMapping("/loan")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> loanTool(
            @RequestParam Long idTool,
            @RequestParam String rutCustomer,
            @RequestParam int quantityToLoan) {
        try {
            ToolEntity result = toolService.loanTool(idTool, rutCustomer, quantityToLoan);
            return ResponseEntity.ok(result);
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // changes status -> to repair
    @PutMapping("/repair")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> repairTool(
            @RequestParam Long idTool,
            @RequestParam String rutCustomer,
            @RequestParam int quantityToRepair) {
        try {
            ToolEntity result = toolService.repairTool(idTool, rutCustomer, quantityToRepair);
            return ResponseEntity.ok(result);
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // update replacement value
    @PutMapping("/update-value")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateReplacementValue(
            @RequestParam Long idTool,
            @RequestParam double replacementValue) {
        try {
            ToolEntity t = toolService.updateReplacementValue(idTool, replacementValue);
            return ResponseEntity.ok(t);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all tool marked as available
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getAllTools() {
        try {
            return ResponseEntity.ok(toolRepository.findAllByinitialState("Disponible"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //get ALL tools
    @GetMapping("/inventory/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllToolsForAdmin() {
        try {
            return ResponseEntity.ok(toolRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // get tool by id
    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getToolById(@PathVariable Long id) {
        try {
            ToolEntity tool = toolRepository.findById(id).orElse(null);

            if (tool != null) {
                return ResponseEntity.ok(tool);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //get tool by name
    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getToolByName(@PathVariable String name) {
        try{
            return ResponseEntity.ok(toolRepository.findByname(name));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //update tool fee
    @PutMapping("/update-fee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateToolFee(@RequestParam Long id, @RequestParam double fee) {
        try {
            ToolEntity updatedTool = toolService.updateFee(id, fee);
            return ResponseEntity.ok(updatedTool);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
