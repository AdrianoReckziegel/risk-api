package com.adriano.risk_api.controller;

import com.adriano.risk_api.entity.Risk;
import com.adriano.risk_api.repository.RiskRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/risks")
public class RiskController {

    private final RiskRepository repository;

    public RiskController(RiskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Risk> all() {
        return repository.findAll();
    }

    @PostMapping
    public Risk create(@RequestBody Risk risk) {
        return repository.save(risk);
    }

}
