package com.bscm.service.impl;

import com.bscm.entity.DiagnosisRecord;
import com.bscm.repository.DiagnosisRecordRepository;
import com.bscm.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final DiagnosisRecordRepository diagnosisRecordRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public String processDiagnosis(String symptoms, List<MultipartFile> images) {
        log.info("处理诊断请求，症状: {}", symptoms);
        
        // TODO: 集成AI模型进行实际诊断
        // 这里是一个示例实现，实际应该调用AI服务
        
        StringBuilder result = new StringBuilder();
        result.append("<h3>诊断结果</h3>");
        result.append("<p>根据您描述的症状：").append(symptoms).append("</p>");
        result.append("<p><strong>初步诊断：</strong>建议进一步检查</p>");
        result.append("<p><strong>建议：</strong></p>");
        result.append("<ul>");
        result.append("<li>保持良好作息</li>");
        result.append("<li>注意饮食健康</li>");
        result.append("<li>如有不适，请及时就医</li>");
        result.append("</ul>");
        
        String imagePaths = null;
        if (images != null && !images.isEmpty()) {
            result.append("<p>已接收 ").append(images.size()).append(" 张图片</p>");
            // TODO: 保存图片到服务器，并记录路径
            // imagePaths = saveImages(images);
        }
        
        // 保存诊断记录到数据库
        DiagnosisRecord record = new DiagnosisRecord();
        record.setSymptoms(symptoms);
        record.setDiagnosisResult(result.toString());
        record.setImagePaths(imagePaths);
        record.setUserId(null); // TODO: 从认证信息中获取用户ID
        
        diagnosisRecordRepository.save(record);
        log.info("诊断记录已保存，ID: {}", record.getId());
        
        return result.toString();
    }

    @Override
    public List<Object> getDiagnosisHistory() {
        List<DiagnosisRecord> records = diagnosisRecordRepository.findAllOrderByCreatedAtDesc();
        
        return records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("date", record.getCreatedAt().format(DATE_FORMATTER));
            map.put("symptoms", record.getSymptoms());
            map.put("result", record.getDiagnosisResult());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Object getDiagnosisDetail(Long id) {
        DiagnosisRecord record = diagnosisRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("诊断记录不存在: " + id));
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", record.getId());
        detail.put("date", record.getCreatedAt().format(DATE_FORMATTER));
        detail.put("symptoms", record.getSymptoms());
        detail.put("result", record.getDiagnosisResult());
        detail.put("imagePaths", record.getImagePaths());
        detail.put("updatedAt", record.getUpdatedAt() != null ? 
                record.getUpdatedAt().format(DATE_FORMATTER) : null);
        
        return detail;
    }

    @Override
    @Transactional
    public void deleteDiagnosis(Long id) {
        if (!diagnosisRecordRepository.existsById(id)) {
            throw new RuntimeException("诊断记录不存在: " + id);
        }
        diagnosisRecordRepository.deleteById(id);
        log.info("诊断记录已删除，ID: {}", id);
    }
}

