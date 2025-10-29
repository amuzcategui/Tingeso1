package com.example.lab1.services;


import com.example.lab1.entities.CustomerEntity;
import com.example.lab1.entities.KardexEntity;
import com.example.lab1.entities.LoanEntity;
import com.example.lab1.repositories.CustomerRepository;
import com.example.lab1.repositories.KardexRepository;
import com.example.lab1.repositories.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;



@Service
public class KardexService {
    @Autowired
    private KardexRepository kardexRepository;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private CustomerRepository customerRepository;

    public KardexEntity kardex(String rut, String type, String toolName, int quantity) {
        KardexEntity k = new KardexEntity();
        k.setRutCustomer(rut);
        k.setMovementType(type);
        k.setMovementDate(LocalDate.now());
        k.setToolName(toolName);
        k.setToolQuantity(quantity);
        return k;
    }

    public Map<String, List<LoanEntity>> listActiveLoansGrouped() {
        LocalDate today = LocalDate.now();

        List<LoanEntity> lates = loanRepository.findByEndDateIsNullAndDueDateBefore(today);
        List<LoanEntity> notLates = loanRepository.findByEndDateIsNullAndDueDateGreaterThanEqual(today);

        List<LoanEntity> activosSinDue = new ArrayList<>();
        for (LoanEntity l : loanRepository.findByEndDateIsNull()) {
            if (l.getDueDate() == null) activosSinDue.add(l);
        }
        if (!activosSinDue.isEmpty()) notLates.addAll(activosSinDue);

        Map<String, List<LoanEntity>> resp = new LinkedHashMap<>();
        resp.put("Atrasos", lates);
        resp.put("Vigentes", notLates);
        return resp;
    }

    public List<CustomerEntity> listOverdueCustomers() {
        LocalDate today = LocalDate.now();
        List<LoanEntity> lateLoans = loanRepository.findByEndDateIsNullAndDueDateBefore(today);

        List<CustomerEntity> out = new ArrayList<>();
        List<String> addedRuts = new ArrayList<>();

        for (int i = 0; i < lateLoans.size(); i++) {
            String rut = lateLoans.get(i).getRutCustomer();
            if (rut == null || addedRuts.contains(rut)) continue;

            CustomerEntity c = customerRepository.findByrut(rut);
            if (c != null) {
                out.add(c);
                addedRuts.add(rut);
            }
        }
        return out;
    }

    public List<Object[]> topLoanedTools(LocalDate from, LocalDate to, Integer limit) {
        List<KardexEntity> prestamos = new ArrayList<>();

        if (from != null && to != null) {
            // with range
            prestamos.addAll(kardexRepository.findByMovementTypeAndMovementDateBetween("Préstamo", from, to));
        } else {
            // without range
            prestamos.addAll(kardexRepository.findBymovementType("Préstamo"));
        }

        //acumulate by name
        Map<String, Integer> counter = new HashMap<>();
        for (KardexEntity k : prestamos) {
            String name = k.getToolName();
            Integer quantity = k.getToolQuantity();
            if (name == null || quantity == null || quantity <= 0) continue;
            counter.merge(name, quantity, Integer::sum);
        }

        //order by desc
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counter.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int max = entries.size();
        if (limit != null && limit > 0 && limit < max) max = limit;

        // max listed
        List<Object[]> out = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            out.add(new Object[]{ e.getKey(), e.getValue() });
        }
        return out;
    }

}
