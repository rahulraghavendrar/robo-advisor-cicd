package com.rahul.roboadvisor;

import com.rahul.roboadvisor.model.AdviceResponse;
import com.rahul.roboadvisor.service.AdvisorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvisorServiceTest {

    private final AdvisorService service = new AdvisorService();

    @Test
    void allocationAlwaysAddsUpTo100() {
        for (String risk : new String[]{"LOW", "MEDIUM", "HIGH"}) {
            for (int age = 18; age <= 100; age += 7) {
                AdviceResponse a = service.getAdvice(age, risk, 1000, 5);
                assertEquals(100, a.equityPercent() + a.debtPercent() + a.goldPercent());
            }
        }
    }

    @Test
    void mediumRiskUsesHundredMinusAgeRule() {
        AdviceResponse a = service.getAdvice(30, "MEDIUM", 5000, 10);
        assertEquals(70, a.equityPercent());
        assertEquals(20, a.debtPercent());
        assertEquals(10, a.goldPercent());
    }

    @Test
    void highRiskGetsMoreEquityThanLowRisk() {
        AdviceResponse high = service.getAdvice(35, "HIGH", 5000, 10);
        AdviceResponse low = service.getAdvice(35, "LOW", 5000, 10);
        assertTrue(high.equityPercent() > low.equityPercent());
    }

    @Test
    void youngerInvestorGetsMoreEquity() {
        AdviceResponse young = service.getAdvice(22, "MEDIUM", 5000, 10);
        AdviceResponse old = service.getAdvice(60, "MEDIUM", 5000, 10);
        assertTrue(young.equityPercent() > old.equityPercent());
    }

    @Test
    void equityStaysWithinLimits() {
        assertEquals(85, service.getAdvice(18, "HIGH", 1000, 5).equityPercent());
        assertEquals(10, service.getAdvice(95, "LOW", 1000, 5).equityPercent());
    }

    @Test
    void projectedValueIsMoreThanInvested() {
        AdviceResponse a = service.getAdvice(25, "MEDIUM", 5000, 10);
        assertEquals(600000, a.totalInvested());
        assertTrue(a.projectedValue() > a.totalInvested());
    }

    @Test
    void sipWithZeroReturnEqualsAmountInvested() {
        assertEquals(12000.0, service.projectSip(1000, 0, 1), 0.001);
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.getAdvice(10, "LOW", 1000, 5));
        assertThrows(IllegalArgumentException.class, () -> service.getAdvice(30, "LOW", 0, 5));
        assertThrows(IllegalArgumentException.class, () -> service.getAdvice(30, "LOW", 1000, 0));
        assertThrows(IllegalArgumentException.class, () -> service.getAdvice(30, "CRAZY", 1000, 5));
    }
}
