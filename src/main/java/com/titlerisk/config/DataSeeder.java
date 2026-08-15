package com.titlerisk.config;

import com.titlerisk.model.LayoutApprovalStatus;
import com.titlerisk.model.LitigationStatus;
import com.titlerisk.model.MeeBhoomiMatch;
import com.titlerisk.model.Parcel;
import com.titlerisk.model.ReraStatus;
import com.titlerisk.model.User;
import com.titlerisk.repository.ParcelRepository;
import com.titlerisk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.titlerisk.model.EcStatus.CLEAN;
import static com.titlerisk.model.EcStatus.FLAGGED;
import static com.titlerisk.model.LayoutApprovalStatus.APPROVED;
import static com.titlerisk.model.LayoutApprovalStatus.PENDING;
import static com.titlerisk.model.LayoutApprovalStatus.UNAPPROVED;
import static com.titlerisk.model.LitigationStatus.ACTIVE_SUIT;
import static com.titlerisk.model.LitigationStatus.NONE;
import static com.titlerisk.model.MeeBhoomiMatch.MATCHED;
import static com.titlerisk.model.MeeBhoomiMatch.MISMATCH;
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
 * The mix below isn't random - I picked enum combinations that land in four
 * rough score bands (given the 30/25/20/15/10 factor weights, a single bad
 * factor alone can't drag a parcel below ~70, so the "one serious flag" and
 * "multiple flags" groups below combine two or more correlated issues, which
 * is realistic anyway - a parcel under active litigation often shows up
 * flagged on its EC too):
 *   - clean parcels                    -> roughly 85-100
 *   - one dominant flag + a minor one  -> roughly 30-55
 *   - several flags stacked together   -> under 30
 *   - a couple of mild/moderate issues -> roughly 55-85 (borderline)
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

        List<Parcel> parcels = List.of(

                // ---- Clean across the board (expect ~85-100) ----------------------
                new Parcel("142/2A", "K. Venkata Ramana Reddy", "Tullur, Guntur",
                        CLEAN, NONE, APPROVED, REGISTERED, MATCHED),
                new Parcel("88/1", "P. Lakshmi Devi", "Mangalagiri, Guntur",
                        CLEAN, NONE, APPROVED, NOT_APPLICABLE, MATCHED),
                new Parcel("215", "D. Krishna Murthy", "Gannavaram, Krishna",
                        CLEAN, NONE, PENDING, REGISTERED, MATCHED),
                new Parcel("174", "Ch. Srinivasa Rao", "Undavalli, Guntur",
                        CLEAN, LitigationStatus.PENDING, APPROVED, REGISTERED, MATCHED),
                new Parcel("97", "M. Anjali", "Nelapadu, Guntur",
                        CLEAN, NONE, APPROVED, REGISTERED, MISMATCH),

                // ---- One dominant flag, small secondary issue (expect ~30-55) -----
                new Parcel("56/3B", "S. Padmavathi", "Rayapudi, Guntur",
                        FLAGGED, ACTIVE_SUIT, APPROVED, REGISTERED, MATCHED),
                new Parcel("301/A", "G. Appala Naidu", "Bhogapuram, Vizianagaram",
                        FLAGGED, ACTIVE_SUIT, APPROVED, NOT_APPLICABLE, MATCHED),
                new Parcel("128/3", "B. Ramesh Babu", "Madhurawada, Visakhapatnam",
                        CLEAN, ACTIVE_SUIT, UNAPPROVED, REGISTERED, MISMATCH),
                new Parcel("45/2A", "T. Kavitha", "Anandapuram, Visakhapatnam",
                        FLAGGED, NONE, UNAPPROVED, NOT_REGISTERED, MATCHED),
                new Parcel("189/1", "J. Sudhakar Reddy", "Gajuwaka, Visakhapatnam",
                        FLAGGED, LitigationStatus.PENDING, UNAPPROVED, REGISTERED, MATCHED),

                // ---- Multiple flags stacked together (expect under 30) ------------
                new Parcel("19/2", "N. Suneetha", "Machilipatnam, Krishna",
                        FLAGGED, ACTIVE_SUIT, UNAPPROVED, NOT_REGISTERED, MISMATCH),
                new Parcel("62/1B", "A. Bhavani", "Pedakakani, Guntur",
                        FLAGGED, ACTIVE_SUIT, UNAPPROVED, REGISTERED, MISMATCH),
                new Parcel("233/4", "R. Narasimha Rao", "Renigunta, Chittoor",
                        FLAGGED, ACTIVE_SUIT, PENDING, NOT_REGISTERED, MISMATCH),
                new Parcel("210/B", "Y. Prashanth Chowdary", "Yerpedu, Chittoor",
                        FLAGGED, LitigationStatus.PENDING, UNAPPROVED, NOT_REGISTERED, MISMATCH),
                new Parcel("158/2", "L. Manjula", "Kovur, Nellore",
                        FLAGGED, ACTIVE_SUIT, UNAPPROVED, NOT_APPLICABLE, MISMATCH),

                // ---- Mixed / borderline: a couple of mild issues (expect ~55-85) --
                new Parcel("264", "E. Rajendra Prasad", "Muthukur, Nellore",
                        FLAGGED, NONE, APPROVED, REGISTERED, MATCHED),
                new Parcel("33/1A", "Farida Begum", "Rajahmundry Rural, East Godavari",
                        CLEAN, ACTIVE_SUIT, APPROVED, REGISTERED, MATCHED),
                new Parcel("91/3", "K. Sandeep Reddy", "Kakinada Rural, East Godavari",
                        CLEAN, NONE, UNAPPROVED, REGISTERED, MATCHED),
                new Parcel("118/2B", "V. Swapna", "Ongole Outskirts, Prakasam",
                        CLEAN, NONE, PENDING, NOT_REGISTERED, MATCHED),
                new Parcel("76", "M. Venkateswara Rao", "Anantapur Outskirts",
                        CLEAN, NONE, APPROVED, NOT_REGISTERED, MISMATCH)
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
