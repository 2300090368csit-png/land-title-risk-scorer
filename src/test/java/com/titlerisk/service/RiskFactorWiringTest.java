package com.titlerisk.service;

import com.titlerisk.service.factors.RiskFactor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The one test that genuinely needs a Spring context: everything else about the
 * scoring engine is verified with plain fakes.
 *
 * <p>What this proves is the claim the whole design rests on — that Spring
 * discovers every {@code @Component} implementing {@link RiskFactor} and injects
 * them as a collection, with no registration list anywhere in the codebase. If
 * someone adds a sixth factor, this test's expected count is the single place
 * that has to change, which is exactly the intended blast radius.</p>
 */
@SpringBootTest
class RiskFactorWiringTest {

    @Autowired
    private List<RiskFactor> injectedFactors;

    @Test
    @DisplayName("Spring auto-collects all five RiskFactor beans")
    void allFactorsAreDiscovered() {
        assertEquals(5, injectedFactors.size());
    }

    @Test
    @DisplayName("the injected weights sum to 1.0")
    void injectedWeightsSumToOne() {
        double sum = injectedFactors.stream().mapToDouble(RiskFactor::getWeight).sum();
        assertEquals(1.0, sum, 0.0001);
    }
}
