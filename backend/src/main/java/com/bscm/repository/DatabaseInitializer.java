package com.bscm.repository;

import com.bscm.service.EmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {
  private final UserRepository userRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final DiagnosisRecordRepository diagnosisRecordRepository;
  private final JdbcTemplate jdbcTemplate;
  private final EmbeddingService embeddingService;
  private final ObjectMapper objectMapper;

  @Bean
  ApplicationRunner init() {
    return args -> {
      // 修复 email 字段约束：允许为 NULL
      try {
        String checkSql = "SELECT COUNT(*) FROM information_schema.columns " +
            "WHERE table_name = 'users' AND column_name = 'email' " +
            "AND is_nullable = 'NO'";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
        if (count != null && count > 0) {
          log.info("检测到 email 字段为 NOT NULL，正在修复...");
          jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN email DROP NOT NULL");
          log.info("email 字段约束已修复，现在允许为 NULL");
        }
      } catch (Exception e) {
        log.warn("修复 email 字段约束时出错（可能字段不存在或已修复）: {}", e.getMessage());
      }

      // 初始化知识库表
      initKnowledgeBase();

      userRepository.count();
      chatMessageRepository.count();
      diagnosisRecordRepository.count();
      log.info("数据库已初始化完成");
    };
  }

  /** 初始化知识库表和数据 */
  private void initKnowledgeBase() {
    try {
      // 检查表是否存在
      String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables " +
          "WHERE table_name = 'knowledge_base'";
      Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);

      if (tableCount == null || tableCount == 0) {
        log.info("创建 knowledge_base 表...");
        // 创建表
        jdbcTemplate.execute("""
            CREATE TABLE knowledge_base (
                id BIGSERIAL PRIMARY KEY,
                question TEXT NOT NULL,
                answer TEXT NOT NULL,
                type INTEGER NOT NULL,
                vector TEXT,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """);

        // 创建索引
        jdbcTemplate.execute("CREATE INDEX idx_knowledge_base_type ON knowledge_base(type)");
        jdbcTemplate.execute("CREATE INDEX idx_knowledge_base_created_at ON knowledge_base(created_at DESC)");
        log.info("knowledge_base 表创建成功");
      }

      // 检查是否有数据
      Integer dataCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM knowledge_base", Integer.class);
      if (dataCount == null || dataCount == 0) {
        log.info("插入知识库初始数据...");
        insertKnowledgeBaseData();
        log.info("知识库初始数据插入完成");
      } else {
        log.info("知识库已有数据，跳过插入（当前记录数: {}）", dataCount);
      }

      // 检查并填充向量
      initKnowledgeBaseVectors();
    } catch (Exception e) {
      log.error("初始化知识库失败: {}", e.getMessage(), e);
    }
  }

  /** 插入知识库初始数据 */
  private void insertKnowledgeBaseData() {
    // 插入基础知识（type=1）
    String insertBasicSql = """
        INSERT INTO knowledge_base (question, answer, type) VALUES
        (?, ?, 1)
        """;

    Object[][] basicKnowledge = {
        {"什么是脑干海绵状血管畸形？", "脑干海绵状血管畸形（Brainstem Cavernous Malformation, BSCM）是一种先天性脑血管发育异常，属于脑血管畸形的一种。它是由异常扩张的毛细血管组成的血管团，血管壁薄弱，容易破裂出血。脑干是连接大脑和脊髓的重要结构，包含许多重要的神经核团和传导束，因此脑干海绵状血管畸形的治疗需要非常谨慎。"},
        {"脑干海绵状血管畸形有哪些症状？", "脑干海绵状血管畸形的症状取决于病变的位置和大小。常见症状包括：1）头痛，特别是突发性剧烈头痛；2）神经功能障碍，如面部麻木、吞咽困难、声音嘶哑；3）眼球运动障碍，如复视、眼球震颤；4）肢体无力或感觉异常；5）平衡障碍和共济失调；6）意识障碍（严重时）。症状可能是急性发作（出血时）或慢性进展（反复少量出血）。"},
        {"脑干海绵状血管畸形如何诊断？", "脑干海绵状血管畸形的诊断主要依靠影像学检查：1）磁共振成像（MRI）是最重要的检查方法，特别是T2加权像和梯度回波序列（GRE），可以清晰显示病变的\"爆米花\"样特征；2）磁敏感加权成像（SWI）对检测微出血非常敏感；3）CT扫描可以显示急性出血，但对病变本身的显示不如MRI；4）脑血管造影（DSA）通常显示正常，因为病变内血流缓慢。结合患者的临床症状和影像学表现可以做出诊断。"},
        {"脑干海绵状血管畸形的治疗方法有哪些？", "脑干海绵状血管畸形的治疗方法包括：1）保守观察：对于无症状或症状轻微、病变较小的患者，可以定期随访观察；2）药物治疗：主要是对症治疗，如控制癫痫、缓解头痛等；3）手术治疗：对于反复出血、症状进行性加重、病变较大或位于相对安全的区域的患者，可以考虑手术切除。由于脑干结构复杂，手术风险较高，需要经验丰富的神经外科医生操作；4）立体定向放射治疗：对于不适合手术的患者，可以考虑伽马刀等放射治疗，但效果和风险需要仔细评估。"},
        {"脑干海绵状血管畸形的预后如何？", "脑干海绵状血管畸形的预后取决于多个因素：1）病变位置：位于脑干深部或重要功能区的病变预后较差；2）出血频率和严重程度：反复出血会导致神经功能进行性恶化；3）治疗时机：及时诊断和适当治疗可以改善预后；4）患者年龄和一般状况：年轻、身体状况好的患者预后相对较好。总体而言，经过适当治疗，大多数患者可以维持较好的生活质量，但需要长期随访观察。"},
        {"脑干海绵状血管畸形会遗传吗？", "脑干海绵状血管畸形有家族性和散发性两种形式。家族性病例约占20-50%，与多个基因突变有关，最常见的是CCM1、CCM2和CCM3基因突变。家族性病例通常为常染色体显性遗传，但外显率不完全，意味着携带突变基因的人不一定都会发病。散发性病例占大多数，通常为单发，没有明显的家族史。如果家族中有多人患病，建议进行遗传咨询和基因检测。"},
        {"脑干海绵状血管畸形患者需要注意什么？", "脑干海绵状血管畸形患者需要注意：1）定期复查：每6-12个月进行一次MRI检查，监测病变变化；2）避免剧烈运动：特别是可能增加颅内压的活动，如举重、潜水等；3）控制血压：保持血压在正常范围，减少出血风险；4）避免使用抗凝药物：如阿司匹林、华法林等，除非有明确的医疗指征；5）注意症状变化：如出现新的神经症状或原有症状加重，应及时就医；6）保持健康生活方式：规律作息、合理饮食、适度运动。"},
        {"脑干海绵状血管畸形的手术风险有哪些？", "脑干海绵状血管畸形手术风险较高，主要包括：1）神经功能损伤：可能造成永久性的神经功能障碍，如面瘫、吞咽困难、肢体无力等；2）出血：术中或术后出血可能导致严重后果；3）感染：任何手术都有感染风险；4）脑脊液漏：术后可能出现脑脊液漏；5）病变残留或复发：如果病变切除不彻底，可能复发；6）死亡：虽然罕见，但在脑干手术中仍存在死亡风险。手术风险与病变位置、大小、与重要结构的关系以及医生的经验密切相关。"},
        {"脑干海绵状血管畸形与脑干出血有什么区别？", "脑干海绵状血管畸形是导致脑干出血的常见原因之一，但两者不完全等同：1）脑干海绵状血管畸形是一种结构性病变，是血管畸形本身；2）脑干出血是指血液进入脑干组织，可能由多种原因引起，包括海绵状血管畸形、高血压、动脉瘤破裂、外伤等；3）脑干海绵状血管畸形可以反复出血，导致多次脑干出血事件；4）诊断上，脑干海绵状血管畸形需要MRI等影像学检查来确认病变的存在，而脑干出血可以通过CT或MRI直接观察到。"},
        {"脑干海绵状血管畸形的发病率是多少？", "脑干海绵状血管畸形的确切发病率尚不清楚，因为许多患者可能没有症状。根据研究，海绵状血管畸形（包括所有部位）的总体发病率约为0.4-0.8%，其中约20-30%位于脑干。脑干海绵状血管畸形可以发生在任何年龄，但以30-50岁的中青年多见。女性可能略多于男性。家族性病例的发病率在家族成员中明显高于普通人群。"}
    };

    for (Object[] knowledge : basicKnowledge) {
      jdbcTemplate.update(insertBasicSql, knowledge[0], knowledge[1]);
    }

    // 插入实际病例（type=2）
    String insertCaseSql = """
        INSERT INTO knowledge_base (question, answer, type) VALUES
        (?, ?, 2)
        """;

    Object[][] caseKnowledge = {
        {"病例1：患者，女性，42岁，因突发头痛、复视就诊", "患者主诉：突发剧烈头痛，伴有复视，持续2天。查体：左侧外展神经麻痹，左侧面部感觉轻度减退。MRI检查显示：脑桥左侧可见一约1.5cm的异常信号灶，T2加权像呈\"爆米花\"样改变，周围有含铁血黄素环，SWI序列显示多发微出血。诊断：脑桥海绵状血管畸形伴急性出血。治疗：给予对症治疗，控制血压，密切观察。3个月后复查MRI，病变稳定，症状逐渐缓解。随访1年，患者症状基本消失，病变无明显变化。"},
        {"病例2：患者，男性，38岁，反复头痛伴左侧肢体无力", "患者主诉：近2年来反复出现头痛，每次持续数天，伴有左侧肢体轻度无力。既往史：3年前曾有一次类似发作。查体：左侧肢体肌力4级，左侧巴氏征阳性。MRI检查：中脑右侧可见一约2.0cm的海绵状血管畸形，周围有陈旧性出血征象。诊断：中脑海绵状血管畸形，反复出血。治疗：考虑到反复出血和进行性神经功能损害，经过多学科讨论，决定行手术治疗。手术采用右侧颞下入路，在神经导航和电生理监测下，完整切除病变。术后患者恢复良好，左侧肢体肌力恢复至5级，随访6个月无复发。"},
        {"病例3：患者，女性，35岁，体检发现脑干病变", "患者主诉：无特殊不适，体检时MRI发现脑干异常信号。查体：神经系统检查正常。MRI检查：延髓背侧可见一约0.8cm的海绵状血管畸形，无明显出血征象。诊断：延髓海绵状血管畸形（无症状）。治疗：考虑到病变较小、无症状，且位于延髓重要功能区，决定保守观察。每6个月复查MRI，连续随访2年，病变大小和信号无明显变化，患者无任何症状。继续定期随访观察。"},
        {"病例4：患者，男性，45岁，突发意识障碍", "患者主诉：工作中突然出现剧烈头痛，随后意识不清，被同事送至急诊。查体：昏迷状态，双侧瞳孔不等大，右侧肢体无自主活动。急诊CT显示：脑桥大量出血。MRI进一步检查显示：脑桥中央可见一约2.5cm的海绵状血管畸形，伴急性大量出血。诊断：脑桥海绵状血管畸形破裂，大量出血。治疗：急诊行脑室外引流术，降低颅内压。患者意识逐渐恢复，但遗留右侧肢体偏瘫和吞咽困难。3个月后，经过康复治疗，患者可以独立行走，但仍有轻度吞咽困难。建议定期随访，必要时考虑手术治疗。"},
        {"病例5：患者，女性，28岁，家族性病例", "患者主诉：头痛、复视1周。家族史：母亲和舅舅均患有脑干海绵状血管畸形。查体：右侧外展神经麻痹，右侧面部感觉减退。MRI检查：脑桥右侧可见多发海绵状血管畸形，最大者约1.2cm，伴急性出血。基因检测：CCM1基因突变阳性。诊断：家族性脑干海绵状血管畸形。治疗：给予对症治疗，症状逐渐缓解。建议家族成员进行基因检测和MRI筛查。随访中发现患者的姐姐也发现无症状的脑干海绵状血管畸形。"},
        {"病例6：患者，男性，50岁，手术后恢复良好", "患者主诉：反复头痛、吞咽困难2年，进行性加重。查体：左侧软腭上抬无力，左侧声带麻痹，左侧肢体轻度无力。MRI检查：延髓左侧可见一约1.8cm的海绵状血管畸形，周围有含铁血黄素沉积，提示反复出血。诊断：延髓海绵状血管畸形，反复出血。治疗：经过充分评估，采用左侧远外侧入路，在神经导航和术中电生理监测下，完整切除病变。术后患者出现一过性吞咽困难加重，经过康复治疗，3个月后吞咽功能明显改善，6个月后基本恢复正常。随访1年，无复发，MRI显示病变完全切除。"},
        {"病例7：患者，女性，40岁，保守治疗成功", "患者主诉：偶发头痛，无明显其他症状。查体：神经系统检查正常。MRI检查：中脑背侧可见一约0.6cm的海绵状血管畸形，无明显出血征象。诊断：中脑海绵状血管畸形（无症状）。治疗：考虑到病变小、无症状，且位于中脑重要功能区，手术风险高，决定保守观察。每6个月复查MRI，连续随访3年，病变稳定，患者无任何症状，生活质量未受影响。继续定期随访。"},
        {"病例8：患者，男性，33岁，急性出血后恢复", "患者主诉：运动后突发剧烈头痛、恶心呕吐，伴有右侧面部麻木。查体：右侧三叉神经感觉减退，右侧肢体共济失调。急诊MRI显示：脑桥右侧急性出血，周围可见海绵状血管畸形。诊断：脑桥海绵状血管畸形急性出血。治疗：绝对卧床休息，控制血压，对症治疗。1周后症状明显缓解，2周后基本恢复正常。3个月后复查MRI，出血吸收，病变稳定。建议避免剧烈运动，定期随访。"}
    };

    for (Object[] knowledge : caseKnowledge) {
      jdbcTemplate.update(insertCaseSql, knowledge[0], knowledge[1]);
    }
  }

  /** 初始化知识库向量 */
  private void initKnowledgeBaseVectors() {
    try {
      // 查询所有没有向量的记录
      String selectSql = "SELECT id, question FROM knowledge_base WHERE vector IS NULL OR vector = ''";
      List<Map<String, Object>> records =
          jdbcTemplate.queryForList(selectSql);

      if (records.isEmpty()) {
        log.info("所有知识库记录已包含向量，跳过向量生成");
        return;
      }

      log.info("开始为 {} 条知识库记录生成向量...", records.size());

      String updateSql = "UPDATE knowledge_base SET vector = ? WHERE id = ?";
      int successCount = 0;
      int failCount = 0;

      for (Map<String, Object> record : records) {
        Long id = ((Number) record.get("id")).longValue();
        String question = (String) record.get("question");

        try {
          // 生成向量（使用问题文本）
          List<Double> embedding = embeddingService.getEmbedding(question);
          if (embedding.isEmpty()) {
            log.warn("记录 ID {} 的向量生成失败（返回空向量）", id);
            failCount++;
            continue;
          }

          // 将向量转换为JSON字符串
          String vectorJson = objectMapper.writeValueAsString(embedding);
          
          // 验证JSON字符串长度（1536维向量，JSON格式大约25000-30000字符）
          log.debug("记录 ID {} 向量JSON长度: {} 字符", id, vectorJson.length());

          // 更新数据库
          int updatedRows = jdbcTemplate.update(updateSql, vectorJson, id);
          if (updatedRows > 0) {
            successCount++;
            log.debug("记录 ID {} 向量已成功存储到数据库", id);
          } else {
            log.warn("记录 ID {} 向量更新失败（未找到记录）", id);
            failCount++;
          }

          // 添加延迟，避免API调用过快
          Thread.sleep(100); // 100ms延迟

          if (successCount % 5 == 0) {
            log.info("已生成向量: {}/{}", successCount, records.size());
          }
        } catch (Exception e) {
          log.error("为记录 ID {} 生成向量失败: {}", id, e.getMessage());
          failCount++;
          // 继续处理下一条记录
        }
      }

      log.info("向量生成完成：成功 {} 条，失败 {} 条", successCount, failCount);
      
      // 验证向量存储情况
      if (successCount > 0) {
        verifyVectorStorage();
      }
    } catch (Exception e) {
      log.error("初始化知识库向量失败: {}", e.getMessage(), e);
    }
  }

  /** 验证向量存储情况 */
  private void verifyVectorStorage() {
    try {
      String verifySql = "SELECT id, question, LENGTH(vector) as vector_length, " +
          "LEFT(vector, 50) as vector_preview FROM knowledge_base " +
          "WHERE vector IS NOT NULL AND vector != '' LIMIT 3";
      
      List<Map<String, Object>> samples = jdbcTemplate.queryForList(verifySql);
      
      log.info("向量存储验证（采样前3条记录）：");
      for (Map<String, Object> sample : samples) {
        Long id = ((Number) sample.get("id")).longValue();
        String question = (String) sample.get("question");
        Integer vectorLength = sample.get("vector_length") != null 
            ? ((Number) sample.get("vector_length")).intValue() : 0;
        String vectorPreview = (String) sample.get("vector_preview");
        
        log.info("  记录 ID {}: 问题=\"{}\", 向量长度={} 字符, 向量预览={}...", 
            id, question.length() > 30 ? question.substring(0, 30) + "..." : question,
            vectorLength, vectorPreview != null && vectorPreview.length() > 0 ? vectorPreview : "空");
      }
      
      // 检查是否有无效的向量
      String invalidSql = "SELECT COUNT(*) FROM knowledge_base " +
          "WHERE vector IS NOT NULL AND vector != '' " +
          "AND (vector NOT LIKE '[%' OR vector NOT LIKE '%]')";
      Integer invalidCount = jdbcTemplate.queryForObject(invalidSql, Integer.class);
      if (invalidCount != null && invalidCount > 0) {
        log.warn("发现 {} 条记录的向量格式可能不正确（不是JSON数组格式）", invalidCount);
      } else {
        log.info("所有向量格式验证通过（JSON数组格式）");
      }
    } catch (Exception e) {
      log.warn("验证向量存储时出错: {}", e.getMessage());
    }
  }
}
