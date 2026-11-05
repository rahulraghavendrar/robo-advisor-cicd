package com.rahul.roboadvisor.model;

/**
 * Response returned by the /api/advice endpoint.
 * All allocation values are percentages and always add up to 100.
 */
public record AdviceResponse(
        int age,
        String riskLevel,
        int equityPercent,
        int debtPercent,
        int goldPercent,
        double expectedAnnualReturn,
        long totalInvested,
        long projectedValue
) {
}
