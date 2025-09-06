package com.example.lab1.controllers;
import com.example.lab1.entities.ToolEntity;
import com.example.lab1.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/tool")
@CrossOrigin("*")
public class ToolController {
    @Autowired
    ToolService toolService;

    // Crear herramienta (admin la registra)
    @PostMapping("/save")
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

    // Dar de baja herramienta (solo admin)
    @PutMapping("/deactivate")
    public ResponseEntity<?> deactivateTool(
            @RequestParam Long idTool,
            @RequestParam String rutCustomer,
            @RequestParam int quantityToDeactivate) {
        try {
            boolean result = toolService.deactivateTool(idTool, rutCustomer,quantityToDeactivate);
            return ResponseEntity.ok(result);
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

}
