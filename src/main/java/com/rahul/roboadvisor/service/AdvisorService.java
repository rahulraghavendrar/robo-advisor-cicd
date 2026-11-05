package com.rahul.roboadvisor.service;

import com.rahul.roboadvisor.model.AdviceResponse;
import com.rahul.roboadvisor.model.RiskLevel;
import org.springframework.stereotype.Service;

/**
 * Simple rule based robo-advisor.
 *
 * Rules used:
 *  - Base equity = 100 - age (classic thumb rule)
 *  - Risk level adds/subtracts 15% equity
 *  - Equity is kept between 10% and 85%
 *  - Gold is fixed at 10%, remaining goes to debt
 *  - Expected returns assumed: equity 12%, debt 7%, gold 8% per year
 */
@Service
public class AdvisorService {

    static final int GOLD_PERCENT = 10;
    static final int MIN_EQUITY = 10;
    static final int MAX_EQUITY = 85;

    static final double EQUITY_RETURN = 12.0;
    static final double DEBT_RETURN = 7.0;
    static final double GOLD_RETURN = 8.0;

    public AdviceResponse getAdvice(int age, String risk, double monthlyAmount, int years) {
        validate(age, monthlyAmount, years);
        RiskLevel level = RiskLevel.from(risk);

        int equity = clamp(100 - age + level.getEquityAdjustment(), MIN_EQUITY, MAX_EQUITY);
        int gold = GOLD_PERCENT;
        int debt = 100 - equity - gold;

        double expectedReturn = (equity * EQUITY_RETURN + debt * DEBT_RETURN + gold * GOLD_RETURN) / 100.0;
        expectedReturn = Math.round(expectedReturn * 100.0) / 100.0;

        long invested = Math.round(monthlyAmount * 12 * years);
        long projected = Math.round(projectSip(monthlyAmount, expectedReturn, years));

        return new AdviceResponse(age, level.name(), equity, debt, gold, expectedReturn, invested, projected);
    }

    /**
     * Future value of a monthly SIP (payment at start of each month).
     */
    public double projectSip(double monthlyAmount, double annualRatePercent, int years) {
        int months = years * 12;
        double r = annualRatePercent / 12.0 / 100.0;
        if (r == 0) {
            return monthlyAmount * months;
        }
        return monthlyAmount * ((Math.pow(1 + r, months) - 1) / r) * (1 + r);
    }

    private void validate(int age, double monthlyAmount, int years) {
        if (age < 18 || age > 100) {
            throw new IllegalArgumentException("Age must be between 18 and 100");
        }
        if (monthlyAmount <= 0) {
            throw new IllegalArgumentException("Monthly amount must be greater than 0");
        }
        if (years < 1 || years > 50) {
            throw new IllegalArgumentException("Years must be between 1 and 50");
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
