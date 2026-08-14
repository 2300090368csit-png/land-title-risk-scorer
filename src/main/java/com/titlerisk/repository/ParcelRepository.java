package com.titlerisk.repository;

import com.titlerisk.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Parcel}. All CRUD and query
 * behaviour is provided by Spring Data at runtime — no implementation needed.
 */
public interface ParcelRepository extends JpaRepository<Parcel, Long> {
}
