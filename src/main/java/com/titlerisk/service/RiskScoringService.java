package com.titlerisk.service;

import com.titlerisk.model.Parcel;
import com.titlerisk.service.factors.FactorScore;
import com.titlerisk.service.factors.RiskFactor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Combines every registered {@link RiskFactor} into a single weighted risk
 * score for a parcel.
 *
 * Design note (this is the part I usually get asked about in interviews):
 * this class does NOT know about EncumbranceFactor, LitigationFactor, or any
 * other specific factor. Spring collects every bean implementing RiskFactor
 * into the constructor-injected list below, and this class just loops over
 * whatever it's handed, calling the same two interface methods on each one.
 * That's the Open/Closed Principle in practice: the class is open to
 * extension (drop in a sixth RiskFactor - say, a "boundary dispute" check -
 * and Spring wires it in automatically) but closed to modification (nothing
 * in here has to change to support it, and there's no if/else chain that
 * needs a new branch). Adding a factor is a one-file change.
 */
@Service
public class RiskScoringService {

    private final List<RiskFactor> riskFactors;

    public RiskScoringService(List<RiskFactor> riskFactors) {
        this.riskFactors = riskFactors;
    }

    /**
     * Scores a parcel by evaluating every risk factor and combining the
     * results according to each factor's own weight.
     */
    public RiskResult score(Parcel parcel) {
        List<FactorScore> scores = new ArrayList<>();
        double total = 0.0;

        for (RiskFactor factor : riskFactors) {
            FactorScore factorScore = factor.evaluate(parcel);
            scores.add(factorScore);
            total += factorScore.getWeightedContribution();
        }

        // Sort heaviest-weighted factor first (EC, litigation, layout approval,
        // RERA, MeeBhoomi) purely for a consistent, readable display order -
        // the scoring math above doesn't depend on this at all.
        scores.sort(Comparator.comparingDouble(FactorScore::getWeight).reversed());

        return new RiskResult(total, scores);
    }
}
