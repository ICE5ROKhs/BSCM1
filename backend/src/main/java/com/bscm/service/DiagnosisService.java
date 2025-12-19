package com.bscm.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DiagnosisService {
    String processDiagnosis(String symptoms, List<MultipartFile> images);
    List<Object> getDiagnosisHistory();
    Object getDiagnosisDetail(Long id);
    void deleteDiagnosis(Long id);
}

