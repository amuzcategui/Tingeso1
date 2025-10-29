package com.example.lab1.services;

import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.entities.LoanEntity;
import com.example.lab1.entities.ToolEntity;

import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.LoanRepository;
import com.example.lab1.repositories.ToolRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private ToolService toolService;

    @Autowired
    private CustomerService customerService;

    @Transactional
    public LoanEntity createLoan(
            LoanEntity loan,
            String rutCustomer,
            List<String> toolNames,
            LocalDate startDate,
            LocalDate dueDate) {

        CustomerEntity customer = customerRepository.findByrut(rutCustomer);
        if (customer == null) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }
        customerService.updateRestriction(customer);

        boolean hasOverdue = loanRepository
                .existsByRutCustomerAndEndDateIsNullAndDueDateBefore(rutCustomer, LocalDate.now());
        boolean hasUnpaid = loanRepository
                .existsByRutCustomerAndPaidIsFalseAndEndDateNotNull(rutCustomer);

        if (hasOverdue || hasUnpaid) {
            throw new IllegalArgumentException("El cliente no está activo (tiene atrasos o deudas sin pagar)");
        }

        if (!"Activo".equalsIgnoreCase(customer.getStatus())) {
            throw new IllegalArgumentException("El cliente no está activo");
        }

        if (dueDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la de inicio");
        }
        double totalDays = ChronoUnit.DAYS.between(startDate, dueDate);

        if(totalDays < 1) {
            throw new IllegalArgumentException("El arriendo debe ser mayor a un día");
        }

        List<String> inThisLoan = new ArrayList<>();
        List<LoanEntity> activos = loanRepository.findByRutCustomerAndEndDateIsNull(rutCustomer);
        List<String> activeTools = new ArrayList<>();
        for (LoanEntity l : activos) {
            List<String> ln = l.getToolNames();
            if (ln != null) {
                for (String n : ln) {
                    if (!activeTools.contains(n)) {
                        activeTools.add(n);
                    }
                }
            }
        }

        List<String> namesForLoan = new ArrayList<>();
        double totalDailyFee = 0.0;

        for (String toolName : toolNames) {
            List<ToolEntity> tools = toolRepository.findByname(toolName);
            boolean found = false;

            for (ToolEntity t : tools) {
                if ((!found
                        && validateLoan(t)
                        && !inThisLoan.contains(t.getName())
                        && !activeTools.contains(t.getName()))) {

                    totalDailyFee += t.getRentalFee();

                    toolService.loanTool(t.getId(), rutCustomer, 1);
                    namesForLoan.add(t.getName());
                    inThisLoan.add(t.getName());
                    found = true;
                }
            }

            if (!found) {
                throw new IllegalArgumentException("No hay stock disponible para la herramienta: " + toolName);
            }
        }

        // update quantity
        customer.setQuantityLoans(customer.getQuantityLoans() + 1);
        customerRepository.save(customer);

        // Create loan
        loan.setRutCustomer(rutCustomer);
        loan.setToolNames(namesForLoan);
        loan.setStartDate(startDate);
        loan.setDueDate(dueDate);
        loan.setFine(0.0);
        loan.setRentalFee(calculateFee(startDate, dueDate, totalDailyFee));

        return loanRepository.save(loan);
    }

    public boolean validateLoan(ToolEntity tool) {
        return "Disponible".equalsIgnoreCase(tool.getInitialState()) && tool.getStock() >= 1;
    }

    public LoanEntity returnTools(long idLoan, double dailyLateFee, double repairCost, List <String> damaged, List<String> discarded) {
        LoanEntity loan = loanRepository.findByid(idLoan);
        if (loan == null) {
            throw new IllegalArgumentException("Préstamo no encontrado");
        }

        if (loan.isPaid()){
            throw new IllegalArgumentException("Ya el préstamo fue devuelto");
        }
        loan.setDamagedTools(damaged);
        loan.setDiscardedTools(discarded);
        loanRepository.save(loan);
        List<String> toolNames = loan.getToolNames();
        String rutCustomer = loan.getRutCustomer();
        LocalDate dueDate = loan.getDueDate();
        LocalDate endDate = LocalDate.now();
        loan.setEndDate(endDate);
        double lateFee = calculateLateFee(dueDate, endDate, dailyLateFee);
        loan.setFine(lateFee);

        CustomerEntity customer = customerRepository.findByrut(rutCustomer);
        customer.setQuantityLoans(customer.getQuantityLoans() - 1);
        customerRepository.save(customer);

        for (String toolName : toolNames) {
            List<ToolEntity> tools = toolRepository.findByname(toolName);
            boolean updated = false;

            for (ToolEntity t : tools) {
                if (!updated && "Prestada".equalsIgnoreCase(t.getInitialState())) {

                    if (discarded.contains(t.getName())) {
                        //->deactivate
                        toolService.deactivateTool(t.getId(), rutCustomer, 1);
                        loan.setFine(loan.getFine() + t.getToolValue());

                    } else if (damaged.contains(t.getName())) {
                        // repair
                        toolService.repairTool(t.getId(), rutCustomer, 1);
                        //Puede cobrarse el valor de reparación
                        loan.setFine(loan.getFine() + repairCost);

                    } else {
                        // available
                        toolService.availableTool(t.getId(), rutCustomer, 1);
                    }

                    updated = true;
                }
            }

            if (!updated) {
                throw new IllegalArgumentException("No se encontró herramienta en estado 'Prestada': " + toolName);
            }
        }

        loanRepository.save(loan);


        return loan;
    }


    public double calculateFee(LocalDate startDate, LocalDate dueDate, double rentalDailyFee) {

        double totalDays = ChronoUnit.DAYS.between(startDate, dueDate);
        return totalDays * rentalDailyFee;
    }

    public double calculateLateFee(LocalDate dueDate, LocalDate endDate, double dailyLateFee) {

        double latefee = 0;
        if(endDate.isAfter(dueDate)){
            double totalDaysLate = ChronoUnit.DAYS.between(dueDate, endDate);
            latefee = totalDaysLate * dailyLateFee;

        }

        return latefee;
    }

    @Transactional
    public LoanEntity markLoanAsPaid(long idLoan) {
        LoanEntity loan = loanRepository.findByid(idLoan);
        if (loan == null) throw new IllegalArgumentException("Préstamo no encontrado");
        loan.setPaid(true);
        loanRepository.save(loan);
        CustomerEntity c = customerRepository.findByrut(loan.getRutCustomer());
        if (c != null) customerService.updateRestriction(c);
        return loan;
    }

    public List<LoanEntity> findLoansByCustomerRut(String rutCustomer) {
        List<LoanEntity> loans = loanRepository.findByrutCustomer(rutCustomer);
        return loans;
    }

}
