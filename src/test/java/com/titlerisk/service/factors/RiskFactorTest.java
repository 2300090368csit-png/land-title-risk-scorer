package com.titlerisk.service.factors;

import com.titlerisk.model.EcStatus;
import com.titlerisk.model.LandClassification;
import com.titlerisk.model.LayoutApprovalStatus;
import com.titlerisk.model.LitigationStatus;
import com.titlerisk.model.MeeBhoomiMatch;
import com.titlerisk.model.NalaStatus;
import com.titlerisk.model.Parcel;
import com.titlerisk.model.PattadarMatch;
import com.titlerisk.model.ProhibitedPropertyStatus;
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
 * One case per enum value for each of the nine factors. These are the tests
 * that fail loudly if someone edits a scoring rule without meaning to.
 */
class RiskFactorTest {

    /** A parcel that passes every check; individual tests override the one field they care about. */
    private static Parcel cleanParcel() {
        return new Parcel("1/A", "Test Seller", "Test Village",
                ProhibitedPropertyStatus.NOT_LISTED, LandClassification.PRIVATE_PATTA,
                EcStatus.CLEAN, LitigationStatus.NONE, PattadarMatch.MATCHED,
                LayoutApprovalStatus.APPROVED, NalaStatus.CONVERTED,
                ReraStatus.REGISTERED, MeeBhoomiMatch.MATCHED);
    }

    @ParameterizedTest(name = "22A {0} -> {1}")
    @CsvSource({"NOT_LISTED, 100", "UNDER_REVIEW, 40", "LISTED, 0"})
    void prohibitedScores(ProhibitedPropertyStatus status, double expected) {
        Parcel p = cleanParcel();
        p.setProhibitedStatus(status);
        assertEquals(expected, new ProhibitedPropertyFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @ParameterizedTest(name = "Classification {0} -> {1}")
    @CsvSource({"PRIVATE_PATTA, 100", "ASSIGNED_DPATTA, 5", "ENDOWMENT_WAKF, 0", "GOVERNMENT_PORAMBOKE, 0"})
    void classificationScores(LandClassification status, double expected) {
        Parcel p = cleanParcel();
        p.setLandClassification(status);
        assertEquals(expected, new LandClassificationFactor().evaluate(p).getRawScore(), 0.0001);
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

    @ParameterizedTest(name = "Pattadar {0} -> {1}")
    @CsvSource({"MATCHED, 100", "NAME_MISMATCH, 35", "NOT_IN_RECORD, 10"})
    void pattadarScores(PattadarMatch status, double expected) {
        Parcel p = cleanParcel();
        p.setPattadarMatch(status);
        assertEquals(expected, new PattadarFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @ParameterizedTest(name = "Layout {0} -> {1}")
    @CsvSource({"APPROVED, 100", "PENDING, 55", "UNAPPROVED, 10"})
    void layoutScores(LayoutApprovalStatus status, double expected) {
        Parcel p = cleanParcel();
        p.setLayoutApproval(status);
        assertEquals(expected, new LayoutApprovalFactor().evaluate(p).getRawScore(), 0.0001);
    }

    @ParameterizedTest(name = "NALA {0} -> {1}")
    @CsvSource({"CONVERTED, 100", "NOT_REQUIRED, 95", "PENDING, 50", "NOT_CONVERTED, 20"})
    void nalaScores(NalaStatus status, double expected) {
        Parcel p = cleanParcel();
        p.setNalaStatus(status);
        assertEquals(expected, new NalaFactor().evaluate(p).getRawScore(), 0.0001);
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
    @DisplayName("exactly three conditions impose a ceiling, at the documented values")
    void ceilingsAreWhereExpected() {
        Parcel p = cleanParcel();

        // 1. Section 22A listing — registration legally blocked.
        p.setProhibitedStatus(ProhibitedPropertyStatus.LISTED);
        FactorScore listed = new ProhibitedPropertyFactor().evaluate(p);
        assertTrue(listed.getCeiling().isPresent());
        assertEquals(5, listed.getCeiling().getAsDouble(), 0.0001);
        assertNotNull(listed.getCeilingReason());
        // A pending removal application is serious but not a hard bar.
        p.setProhibitedStatus(ProhibitedPropertyStatus.UNDER_REVIEW);
        assertFalse(new ProhibitedPropertyFactor().evaluate(p).getCeiling().isPresent());
        p.setProhibitedStatus(ProhibitedPropertyStatus.NOT_LISTED);

        // 2. Land that cannot be privately sold.
        p.setLandClassification(LandClassification.ASSIGNED_DPATTA);
        assertEquals(10, new LandClassificationFactor().evaluate(p).getCeiling().getAsDouble(), 0.0001);
        p.setLandClassification(LandClassification.ENDOWMENT_WAKF);
        assertEquals(5, new LandClassificationFactor().evaluate(p).getCeiling().getAsDouble(), 0.0001);
        p.setLandClassification(LandClassification.GOVERNMENT_PORAMBOKE);
        assertEquals(5, new LandClassificationFactor().evaluate(p).getCeiling().getAsDouble(), 0.0001);
        p.setLandClassification(LandClassification.PRIVATE_PATTA);
        assertFalse(new LandClassificationFactor().evaluate(p).getCeiling().isPresent());

        // 3. An active suit.
        p.setLitigationStatus(LitigationStatus.ACTIVE_SUIT);
        assertEquals(45, new LitigationFactor().evaluate(p).getCeiling().getAsDouble(), 0.0001);
        p.setLitigationStatus(LitigationStatus.PENDING);
        assertFalse(new LitigationFactor().evaluate(p).getCeiling().isPresent());

        // Nothing else caps.
        assertFalse(new EncumbranceFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new PattadarFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new LayoutApprovalFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new NalaFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new ReraFactor().evaluate(p).getCeiling().isPresent());
        assertFalse(new MeeBhoomiFactor().evaluate(p).getCeiling().isPresent());
    }

    @Test
    @DisplayName("every factor explains every possible outcome")
    void everyOutcomeIsExplained() {
        Parcel p = cleanParcel();
        for (ProhibitedPropertyStatus s : ProhibitedPropertyStatus.values()) {
            p.setProhibitedStatus(s);
            assertFalse(new ProhibitedPropertyFactor().evaluate(p).getExplanation().isBlank());
        }
        p.setProhibitedStatus(ProhibitedPropertyStatus.NOT_LISTED);
        for (LandClassification s : LandClassification.values()) {
            p.setLandClassification(s);
            assertFalse(new LandClassificationFactor().evaluate(p).getExplanation().isBlank());
        }
        p.setLandClassification(LandClassification.PRIVATE_PATTA);
        for (EcStatus s : EcStatus.values()) {
            p.setEcStatus(s);
            assertFalse(new EncumbranceFactor().evaluate(p).getExplanation().isBlank());
        }
        for (LitigationStatus s : LitigationStatus.values()) {
            p.setLitigationStatus(s);
            assertFalse(new LitigationFactor().evaluate(p).getExplanation().isBlank());
        }
        for (PattadarMatch s : PattadarMatch.values()) {
            p.setPattadarMatch(s);
            assertFalse(new PattadarFactor().evaluate(p).getExplanation().isBlank());
        }
        for (LayoutApprovalStatus s : LayoutApprovalStatus.values()) {
            p.setLayoutApproval(s);
            assertFalse(new LayoutApprovalFactor().evaluate(p).getExplanation().isBlank());
        }
        for (NalaStatus s : NalaStatus.values()) {
            p.setNalaStatus(s);
            assertFalse(new NalaFactor().evaluate(p).getExplanation().isBlank());
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
    @DisplayName("the nine weights still sum to exactly 1.0")
    void weightsSumToOne() {
        // Catches a botched rebalance when a factor is added or reweighted.
        List<RiskFactor> all = List.of(
                new ProhibitedPropertyFactor(), new LandClassificationFactor(), new EncumbranceFactor(),
                new LitigationFactor(), new PattadarFactor(), new LayoutApprovalFactor(),
                new NalaFactor(), new ReraFactor(), new MeeBhoomiFactor());

        double sum = all.stream().mapToDouble(RiskFactor::getWeight).sum();
        assertEquals(1.0, sum, 0.0001);
        assertEquals(9, all.size());
    }
}
