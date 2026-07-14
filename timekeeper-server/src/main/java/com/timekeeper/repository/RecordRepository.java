package com.timekeeper.repository;

import com.timekeeper.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findByUserIdOrderByStartTimeDesc(Long userId);

    @Query("SELECT r FROM Record r WHERE r.userId = :userId AND r.startTime >= :start AND r.startTime < :end ORDER BY r.startTime DESC")
    List<Record> findByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Record r WHERE r.userId = :userId AND r.startTime >= :start AND r.startTime < :end")
    List<Record> findByUserIdAndDateRangeUnordered(@Param("userId") Long userId,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    List<Record> findByUserId(Long userId);
}
