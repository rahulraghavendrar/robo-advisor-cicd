package com.rahul.roboadvisor.controller;

import com.rahul.roboadvisor.model.AdviceResponse;
import com.rahul.roboadvisor.service.AdvisorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdvisorController {

    private final AdvisorService advisorService;
    private final String appVersion;

    public AdvisorController(AdvisorService advisorService,
                             @Value("${app.version}") String appVersion) {
        this.advisorService = advisorService;
        this.appVersion = appVersion;
    }

    // Used by Kubernetes readiness/liveness probes and the pipeline verify step
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "version", appVersion);
    }

    // Example: /api/advice?age=21&risk=HIGH&amount=5000&years=10
    @GetMapping("/advice")
    public AdviceResponse advice(@RequestParam int age,
                                 @RequestParam(defaultValue = "MEDIUM") String risk,
                                 @RequestParam double amount,
                                 @RequestParam(defaultValue = "10") int years) {
        return advisorService.getAdvice(age, risk, amount, years);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadInput(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
