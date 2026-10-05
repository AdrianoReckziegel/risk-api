package com.adriano.risk_api.repository;

import com.adriano.risk_api.entity.VisitorEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorEventRepository extends JpaRepository<VisitorEvent, Long> {

    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM VisitorEvent v WHERE v.sessionId IS NOT NULL AND v.sessionId != ''")
    long countDistinctSessions();

    long countByEventType(String eventType);

    List<VisitorEvent> findAllByOrderByTimestampDesc(Pageable pageable);

}
