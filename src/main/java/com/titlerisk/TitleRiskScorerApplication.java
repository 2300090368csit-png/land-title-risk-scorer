package com.titlerisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Land Title Risk Scorer application.
 *
 * <p>This is a portfolio-style due-diligence tool that scores the legal risk of a
 * land parcel's title, modeled on real checks performed for land in Andhra
 * Pradesh, India (Encumbrance Certificate, litigation status, CRDA/VMRDA/DTCP
 * layout approval, AP RERA registration, and MeeBhoomi digital record matching).</p>
 *
 * <p>This single Spring Boot process serves both halves of the app: a JSON
 * REST API under {@code /api/parcels} ({@link com.titlerisk.controller.ParcelApiController})
 * and the static HTML/CSS/JS frontend that consumes it, from
 * {@code src/main/resources/static}.</p>
 */
@SpringBootApplication
public class TitleRiskScorerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TitleRiskScorerApplication.class, args);
    }
}
