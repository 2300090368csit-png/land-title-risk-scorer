package com.titlerisk.repository;

import com.titlerisk.model.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA repository for {@link ViewHistory}. */
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    List<ViewHistory> findByUsernameOrderByViewedAtDesc(String username);
}
