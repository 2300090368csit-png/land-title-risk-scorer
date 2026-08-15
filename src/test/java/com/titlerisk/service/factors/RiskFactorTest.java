package com.titlerisk.service.factors;

import com.titlerisk.model.EcStatus;
import com.titlerisk.model.LayoutApprovalStatus;
import com.titlerisk.model.LitigationStatus;
import com.titlerisk.model.MeeBhoomiMatch;
import com.titlerisk.model.Parcel;
import com.titlerisk.model.ReraStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One case per enum value for each of the five factors. These are the tests
 * that fail loudly if someone edits a scoring rule without meaning to.
 */
class RiskFactorTest {

    /** A parcel that passes every check; individual tests override the one field they care about. */
    private static Parcel cleanParcel() {
        return new Parcel("1/A", "Test Seller", "Test Village",
                EcStatus.CLEAN, LitigationStatus.NONE, LayoutApprovalStatus.APPROVED,
                ReraStatus.REGISTERED, MeeBhoomiMatch.MATCHED);
    }

    @ParameterizedTest(name = "EC {0} -> {1}")
    @CsvSource({"CLEAN, 100", "FLAGGED, 15"})
    void encumbranceScores(EcStatus status, double expected) {
        Parcel p = cleanParcel();
        p.setEcStatus(status);
        assertEquals(expected, new EncumbranceFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @ParameterizedTest(name = "Litigation {0} -> {1}")
    @CsvSource({"NONE, 100", "PENDING, 50", "ACTIVE_SUIT, 5"})
    void litigationScores(LitigationStatus status, double expected) {
        Parcel p = cleanParcel();
        p.setLitigationStatus(status);
        assertEquals(expected, new LitigationFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @ParameterizedTest(name = "Layout {0} -> {1}")
    @CsvSource({"APPROVED, 100", "PENDING, 55", "UNAPPROVED, 10"})
    void layoutScores(LayoutApprovalStatus status, double expected) {
        Parcel p = cleanParcel();
        p.setLayoutApproval(status);
        assertEquals(expected, new LayoutApprovalFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @ParameterizedTest(name = "RERA {0} -> {1}")
    @CsvSource({"REGISTERED, 100", "NOT_APPLICABLE, 85", "NOT_REGISTERED, 30"})
    void reraScores(ReraStatus status, double expected) {
        Parcel p = cleanParcel();
        p.setReraStatus(status);
        assertEquals(expected, new ReraFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @ParameterizedTest(name = "MeeBhoomi {0} -> {1}")
    @CsvSource({"MATCHED, 100", "MISMATCH, 25"})
    void meeBhoomiScores(MeeBhoomiMatch status, double expected) {
        Parcel p = cleanParcel();
        p.setMeeBhoomiMatch(status);
        assertEquals(expected, new MeeBhoomiFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @Test
    @DisplayName("only an active suit imposes a ceiling")
    void onlyActiveSuitCaps() {
        Parcel p = cleanParcel();

        p.setLitigationStatus(LitigationStatus.ACTIVE_SUIT);
        FactorScore capped = new LitigationFactor().evaluate(p);
        assertTrue(capped.getCeiling().isPresent());
        assertEquals(45, capped.getCeiling().getAsDouble(), 0.0001);
        assertNotNull(capped.getCeilingReason());

        p.setLitigationStatus(LitigationStatus.PENDING);
        assertFalse(new LitigationFactor().evaluate(p).getCeiling().isPresent());

        assertFalse(new EncumbranceFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new LayoutApprovalFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new ReraFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new MeeBhoomiFactor().evaluate(p).getCeiling().isPresent());
    }

    @Test
    @DisplayName("every factor returns a non-empty explanation for every enum value")
    void everyOutcomeIsExplained() {
        Parcel p = cleanParcel();
        for (EcStatus s : EcStatus.values()) {
            p.setEcStatus(s);
            assertFalse(new EncumbranceFactor().evaluate(p).getExplanation().isBlank());
        }
        for (LitigationStatus s : LitigationStatus.values()) {
            p.setLitigationStatus(s);
            assertFalse(new LitigationFactor().evaluate(p).getExplanation().isBlank());
        }
        for (LayoutApprovalStatus s : LayoutApprovalStatus.values()) {
            p.setLayoutApproval(s);
            assertFalse(new LayoutApprovalFactor().evaluate(p).getExplanation().isBlank());
        }
        for (ReraStatus s : ReraStatus.values()) {
            p.setReraStatus(s);
            assertFalse(new ReraFactor().evaluate(p).getExplanation().isBlank());
        }
        for (MeeBhoomiMatch s : MeeBhoomiMatch.values()) {
            p.setMeeBhoomiMatch(s);
            assertFalse(new MeeBhoomiFactor().evaluate(p).getExplanation().isBlank());
        }
    }

    @Test
    @DisplayName("the five weights still sum to exactly 1.0")
    void weightsSumToOne() {
        // This is the test that catches a botched rebalance when a 6th factor is added.
        List<RiskFactor> all = List.of(
                new EncumbranceFactor(), new LitigationFactor(), new LayoutApprovalFactor(),
                new ReraFactor(), new MeeBhoomiFactor());

        double sum = all.stream().mapToDouble(RiskFactor::getWeight).sum();
        assertEquals(1.0, sum, 0.0001);
    }
}
