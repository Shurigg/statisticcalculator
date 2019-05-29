package com.nngu.fqw.statisticcalculator.repo;

import com.nngu.fqw.statisticcalculator.model.FrameData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FrameDataRepo extends CrudRepository<FrameData, Long> {

    @Query("SELECT Min(time) FROM FrameData")
    LocalDateTime findMinTimeOfPacket();

    @Query("SELECT Max(time) FROM FrameData")
    LocalDateTime findMaxTimeOfPacket();

    @Query("SELECT Count(time) FROM FrameData f WHERE f.time >= ?1 AND f.time < ?2")
    Integer findCountStatistic(LocalDateTime start, LocalDateTime end);

    @Query("SELECT Count(time) FROM FrameData f WHERE f.time >= ?1 AND f.time < ?2 AND f.protocol = ?3")
    Integer findCountStatisticByProtocol(LocalDateTime currentStart, LocalDateTime currentEnd, String protocol);

    @Query("SELECT Avg(length) FROM FrameData f WHERE f.time >= ?1 AND f.time < ?2")
    Double findAvgStatistic(LocalDateTime currentStart, LocalDateTime currentEnd);

    @Query("SELECT Avg(length) FROM FrameData f WHERE f.time >= ?1 AND f.time < ?2 AND f.protocol = ?3")
    Double findAvgStatisticByProtocol(LocalDateTime currentStart, LocalDateTime currentEnd, String protocol);
}
