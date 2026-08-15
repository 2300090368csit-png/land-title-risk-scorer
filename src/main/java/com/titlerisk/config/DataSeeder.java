package com.titlerisk.config;

import com.titlerisk.model.LitigationStatus;
import com.titlerisk.model.Parcel;
import com.titlerisk.model.User;
import com.titlerisk.repository.ParcelRepository;
import com.titlerisk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.titlerisk.model.EcStatus.CLEAN;
import static com.titlerisk.model.EcStatus.FLAGGED;
import static com.titlerisk.model.LandClassification.ASSIGNED_DPATTA;
import static com.titlerisk.model.LandClassification.ENDOWMENT_WAKF;
import static com.titlerisk.model.LandClassification.GOVERNMENT_PORAMBOKE;
import static com.titlerisk.model.LandClassification.PRIVATE_PATTA;
import static com.titlerisk.model.LayoutApprovalStatus.APPROVED;
import static com.titlerisk.model.LayoutApprovalStatus.UNAPPROVED;
import static com.titlerisk.model.LitigationStatus.ACTIVE_SUIT;
import static com.titlerisk.model.LitigationStatus.NONE;
import static com.titlerisk.model.MeeBhoomiMatch.MATCHED;
import static com.titlerisk.model.MeeBhoomiMatch.MISMATCH;
import static com.titlerisk.model.NalaStatus.CONVERTED;
import static com.titlerisk.model.NalaStatus.NOT_CONVERTED;
import static com.titlerisk.model.NalaStatus.NOT_REQUIRED;
import static com.titlerisk.model.PattadarMatch.NAME_MISMATCH;
import static com.titlerisk.model.PattadarMatch.NOT_IN_RECORD;
import static com.titlerisk.model.ProhibitedPropertyStatus.LISTED;
import static com.titlerisk.model.ProhibitedPropertyStatus.NOT_LISTED;
import static com.titlerisk.model.ProhibitedPropertyStatus.UNDER_REVIEW;
import static com.titlerisk.model.ReraStatus.NOT_APPLICABLE;
import static com.titlerisk.model.ReraStatus.NOT_REGISTERED;
import static com.titlerisk.model.ReraStatus.REGISTERED;

/**
 * Loads 20 sample parcels on startup so the app has something to look at
 * without needing a database dump or manual data entry. Locations are spread
 * across Andhra Pradesh's active land markets — the Amaravati capital region
 * (Guntur/Krishna), the Visakhapatnam outskirts, Vijayawada's Gannavaram
 * corridor, the Tirupati belt, and Nellore/Ongole/Rajahmundry/Anantapur.
 *
 * The mix is chosen to exercise every branch of the scoring engine, including
 * the three ceiling conditions that no amount of clean paperwork can offset:
 *   - a Section 22A listing (registration legally blocked)
 *   - assigned / government / endowment land (not privately saleable)
 *   - an active suit (a court can freeze or unwind the sale)
 * Parcels 16 and 17 exist specifically to show a ceiling overriding otherwise
 * excellent paperwork, which is the behaviour a purely additive score got wrong.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ParcelRepository parcelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(ParcelRepository parcelRepository, UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.parcelRepository = parcelRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedDemoAccount();

        // Argument order: surveyNo, seller, location, 22A, classification, EC,
        //                 litigation, pattadar, layout, NALA, RERA, MeeBhoomi
        List<Parcel> parcels = List.of(

                // ---- Clean across all nine checks -------------------------------
                new Parcel("142/2A", "K. Venkata Ramana Reddy", "Tullur, Guntur",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, CONVERTED, REGISTERED, MATCHED),
                new Parcel("88/1", "P. Lakshmi Devi", "Mangalagiri, Guntur",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, CONVERTED, NOT_APPLICABLE, MATCHED),
                new Parcel("215", "D. Krishna Murthy", "Gannavaram, Krishna",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        com.titlerisk.model.LayoutApprovalStatus.PENDING, CONVERTED, REGISTERED, MATCHED),
                new Parcel("97", "M. Anjali", "Nelapadu, Guntur",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, NOT_REQUIRED, REGISTERED, MISMATCH),

                // ---- Good, with one soft issue ----------------------------------
                new Parcel("91/3", "K. Sandeep Reddy", "Kakinada Rural, East Godavari",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        UNAPPROVED, CONVERTED, REGISTERED, MATCHED),
                new Parcel("118/2B", "V. Swapna", "Ongole Outskirts, Prakasam",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        com.titlerisk.model.LayoutApprovalStatus.PENDING, NOT_REQUIRED, NOT_REGISTERED, MATCHED),
                new Parcel("76", "M. Venkateswara Rao", "Anantapur Outskirts",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, NOT_REQUIRED, NOT_REGISTERED, MISMATCH),

                // ---- Agricultural land not converted, ownership unclear ----------
                new Parcel("264", "E. Rajendra Prasad", "Muthukur, Nellore",
                        NOT_LISTED, PRIVATE_PATTA, FLAGGED, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, NOT_REQUIRED, REGISTERED, MATCHED),
                new Parcel("45/2A", "T. Kavitha", "Anandapuram, Visakhapatnam",
                        NOT_LISTED, PRIVATE_PATTA, FLAGGED, NONE, NAME_MISMATCH,
                        UNAPPROVED, NOT_CONVERTED, NOT_REGISTERED, MATCHED),
                new Parcel("189/1", "J. Sudhakar Reddy", "Gajuwaka, Visakhapatnam",
                        NOT_LISTED, PRIVATE_PATTA, FLAGGED, LitigationStatus.PENDING, NAME_MISMATCH,
                        UNAPPROVED, NOT_CONVERTED, REGISTERED, MATCHED),
                new Parcel("33/2", "B. Sarojini", "Tadepalligudem, West Godavari",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, NONE, NOT_IN_RECORD,
                        APPROVED, NOT_CONVERTED, NOT_APPLICABLE, MISMATCH),

                // ---- Seller isn't the recorded owner (GPA / unmutated) -----------
                new Parcel("210/B", "Y. Prashanth Chowdary", "Yerpedu, Chittoor",
                        NOT_LISTED, PRIVATE_PATTA, FLAGGED, LitigationStatus.PENDING, NOT_IN_RECORD,
                        UNAPPROVED, NOT_CONVERTED, NOT_REGISTERED, MISMATCH),
                new Parcel("19/2", "N. Suneetha", "Machilipatnam, Krishna",
                        NOT_LISTED, PRIVATE_PATTA, FLAGGED, ACTIVE_SUIT, NOT_IN_RECORD,
                        UNAPPROVED, NOT_CONVERTED, NOT_REGISTERED, MISMATCH),

                // ---- Active litigation (ceiling 45) ------------------------------
                new Parcel("56/3B", "S. Padmavathi", "Rayapudi, Guntur",
                        NOT_LISTED, PRIVATE_PATTA, FLAGGED, ACTIVE_SUIT, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, CONVERTED, REGISTERED, MATCHED),
                new Parcel("128/3", "B. Ramesh Babu", "Madhurawada, Visakhapatnam",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, ACTIVE_SUIT, NAME_MISMATCH,
                        UNAPPROVED, NOT_CONVERTED, REGISTERED, MISMATCH),
                // Deliberate showcase: flawless paperwork, but a live suit caps it.
                new Parcel("33/1A", "Farida Begum", "Rajahmundry Rural, East Godavari",
                        NOT_LISTED, PRIVATE_PATTA, CLEAN, ACTIVE_SUIT, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, CONVERTED, REGISTERED, MATCHED),

                // ---- Section 22A listed: registration legally blocked -------------
                // Also deliberately clean elsewhere, to show a 22A listing overriding
                // an otherwise near-perfect file.
                new Parcel("301/A", "G. Appala Naidu", "Bhogapuram, Vizianagaram",
                        LISTED, PRIVATE_PATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, CONVERTED, REGISTERED, MATCHED),
                new Parcel("233/4", "R. Narasimha Rao", "Renigunta, Chittoor",
                        UNDER_REVIEW, PRIVATE_PATTA, FLAGGED, LitigationStatus.PENDING, NAME_MISMATCH,
                        UNAPPROVED, NOT_CONVERTED, NOT_REGISTERED, MISMATCH),

                // ---- Not privately saleable at all --------------------------------
                new Parcel("62/1B", "A. Bhavani", "Pedakakani, Guntur",
                        NOT_LISTED, ASSIGNED_DPATTA, CLEAN, NONE, com.titlerisk.model.PattadarMatch.MATCHED,
                        APPROVED, NOT_REQUIRED, NOT_APPLICABLE, MATCHED),
                new Parcel("158/2", "L. Manjula", "Kovur, Nellore",
                        NOT_LISTED, ENDOWMENT_WAKF, FLAGGED, NONE, NOT_IN_RECORD,
                        UNAPPROVED, NOT_CONVERTED, NOT_APPLICABLE, MISMATCH),
                new Parcel("7/1", "S. Ravi Teja", "Kanuru, Krishna",
                        NOT_LISTED, GOVERNMENT_PORAMBOKE, FLAGGED, NONE, NOT_IN_RECORD,
                        UNAPPROVED, NOT_CONVERTED, NOT_REGISTERED, MISMATCH)
        );

        parcelRepository.saveAll(parcels);
    }

    /**
     * Seeds one demo account (username {@code demo}, password {@code demo1234})
     * so anyone cloning the repo can sign in immediately without registering
     * first — the login page hints at these credentials. Registering a real
     * account works exactly the same way, this is just a convenience.
     */
    private void seedDemoAccount() {
        if (!userRepository.existsByUsername("demo")) {
            userRepository.save(new User("demo", passwordEncoder.encode("demo1234")));
        }
    }
}
