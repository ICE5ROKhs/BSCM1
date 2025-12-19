package com.bscm.controller;

import com.bscm.common.Result;
import com.bscm.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/diagnosis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping("/submit")
    public Result<String> submitDiagnosis(
            @RequestParam("symptoms") String symptoms,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            String result = diagnosisService.processDiagnosis(symptoms, images);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("诊断失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<Object>> getHistory() {
        try {
            List<Object> history = diagnosisService.getDiagnosisHistory();
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取历史记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Object> getDetail(@PathVariable Long id) {
        try {
            Object detail = diagnosisService.getDiagnosisDetail(id);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        try {
            diagnosisService.deleteDiagnosis(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}

