package com.bscm.repository;

import com.bscm.entity.DiagnosisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRecordRepository extends JpaRepository<DiagnosisRecord, Long> {
    
    /**
     * 根据用户ID查询诊断记录
     */
    List<DiagnosisRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 查询所有诊断记录，按创建时间倒序
     */
    @Query("SELECT d FROM DiagnosisRecord d ORDER BY d.createdAt DESC")
    List<DiagnosisRecord> findAllOrderByCreatedAtDesc();
    
    /**
     * 根据用户ID删除诊断记录
     */
    void deleteByUserId(Long userId);
}

