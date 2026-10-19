package net.linlan.dsh.global.service;

import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.global.entity.EntityMergeLog;
import net.linlan.dsh.global.repository.EntityMergeLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 实体消歧与合并服务
 * <p>P2-15：支持多租户重复发现实体合并，记录合并日志，迁移引用关系。
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
@Service
public class EntityMergeService {

    private final EntityMergeLogRepository mergeLogRepository;
    private final JdbcTemplate jdbcTemplate;

    public EntityMergeService(EntityMergeLogRepository mergeLogRepository, JdbcTemplate jdbcTemplate) {
        this.mergeLogRepository = mergeLogRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 合并实体：将 source 实体合并到 target 实体
     * <p>步骤：
     * <ol>
     *   <li>记录合并日志</li>
     *   <li>迁移 onto_common_tenant_relation_asset 中的引用（source → target）</li>
     *   <li>累加 mention_count 到 target</li>
     *   <li>软删除 source 实体</li>
     * </ol>
     * @param sourceEntityId 被合并的源实体ID
     * @param targetEntityId 合并目标实体ID
     * @param entityType 实体类型
     * @param similarityScore 相似度得分
     * @param strategy 合并策略
     * @return 合并日志ID
     */
    @Transactional
    public Long mergeEntities(Long sourceEntityId, Long targetEntityId, String entityType,
                              Float similarityScore, String strategy) {
        if (sourceEntityId.equals(targetEntityId)) {
            throw new IllegalArgumentException("源实体和目标实体不能相同");
        }

        Long tenantId = TenantContext.getTenantId();
        Long operatorId = TenantContext.getUserId();
        String operator = operatorId != null ? String.valueOf(operatorId) : "system";

        EntityMergeLog log = new EntityMergeLog();
        log.setSourceEntityId(sourceEntityId);
        log.setTargetEntityId(targetEntityId);
        log.setEntityType(entityType);
        log.setSimilarityScore(similarityScore);
        log.setMergeStrategy(strategy);
        log.setOperatorId(operatorId);
        log.setTenantId(tenantId != null ? tenantId : 0L);
        log.setCreatedBy(operator);
        Long logId = mergeLogRepository.save(log);

        jdbcTemplate.update(
                "UPDATE onto_common_tenant_relation_asset SET subject_entity_id = ? " +
                "WHERE subject_entity_id = ? AND tenant_id = ?",
                targetEntityId, sourceEntityId, tenantId);
        jdbcTemplate.update(
                "UPDATE onto_common_tenant_relation_asset SET object_entity_id = ? " +
                "WHERE object_entity_id = ? AND tenant_id = ?",
                targetEntityId, sourceEntityId, tenantId);

        jdbcTemplate.update(
                "UPDATE onto_common_discovered_entity SET mention_count = mention_count + " +
                "(SELECT mention_count FROM onto_common_discovered_entity WHERE id = ?), " +
                "updated_by = ? WHERE id = ?",
                sourceEntityId, operator, targetEntityId);

        jdbcTemplate.update(
                "UPDATE onto_common_discovered_entity SET del_flag = 1, updated_by = ? WHERE id = ?",
                operator, sourceEntityId);

        return logId;
    }

    /**
     * 自动名称匹配合并：同租户下名称和类型完全相同的实体自动合并
     * @return 合并操作数
     */
    @Transactional
    public int autoMergeByNameMatch(Long tenantId) {
        List<java.util.Map<String, Object>> duplicates = jdbcTemplate.queryForList(
                "SELECT entity_name, entity_type, MIN(id) AS keep_id, COUNT(*) AS cnt " +
                "FROM onto_common_discovered_entity " +
                "WHERE tenant_id = ? AND del_flag = 0 AND status = 'DISCOVERED' " +
                "GROUP BY entity_name, entity_type HAVING cnt > 1",
                tenantId);

        int merged = 0;
        for (java.util.Map<String, Object> dup : duplicates) {
            String entityName = (String) dup.get("entity_name");
            String entityType = (String) dup.get("entity_type");
            Long keepId = ((Number) dup.get("keep_id")).longValue();

            List<Long> sourceIds = jdbcTemplate.queryForList(
                    "SELECT id FROM onto_common_discovered_entity " +
                    "WHERE tenant_id = ? AND del_flag = 0 AND entity_name = ? AND entity_type = ? AND id != ?",
                    Long.class, tenantId, entityName, entityType, keepId);

            for (Long sourceId : sourceIds) {
                TenantContext.setTenantId(tenantId);
                mergeEntities(sourceId, keepId, entityType, 1.0f, "AUTO_NAME_MATCH");
                merged++;
            }
        }
        return merged;
    }

    public List<EntityMergeLog> getMergeLogs(Long tenantId, int pageNum, int pageSize) {
        return mergeLogRepository.findByTenantId(tenantId, pageNum, pageSize);
    }

    public long getMergeLogCount(Long tenantId) {
        return mergeLogRepository.countByTenantId(tenantId);
    }
}
