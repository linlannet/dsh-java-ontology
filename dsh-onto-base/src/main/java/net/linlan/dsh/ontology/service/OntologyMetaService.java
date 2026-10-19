package net.linlan.dsh.ontology.service;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.ontology.repository.OntologyMetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 本体元数据CRUD服务类
 * <p>封装底层共享层 6 类本体元数据（term/eventType/location/entity/version/i18n）的业务操作。</p>
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Service
public class OntologyMetaService {

    private final OntologyMetaRepository repository;

    public OntologyMetaService(OntologyMetaRepository repository) {
        this.repository = repository;
    }

    /** 判断分类是否属于底层本体元数据.
     * @param category 本体分类名称
     * @return 是否为元数据分类
     */
    public boolean isMetaCategory(String category) {
        return repository.isMetaCategory(category);
    }

    /** 获取全部元数据分类名称.
     * @return 分类名称列表
     */
    public List<String> getCategories() {
        return List.copyOf(OntologyMetaRepository.META_TABLE_TO_ENTITY.keySet());
    }

    /** Find all by category. 根据分类获取所有未删除的实体列表.
     * @param category 本体分类名称|term
     * @return 实体列表
     */
    public List<Map<String, Object>> findAll(String category) {
        return repository.findAll(category);
    }

    /** Find page by category. 根据分类分页获取实体列表.
     * @param category 本体分类名称|term
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Map<String, Object>> findPage(String category, int pageNum, int pageSize) {
        return repository.findPage(category, pageNum, pageSize);
    }

    /** Find entity by id. 根据分类和主键获取实体详细信息.
     * @param category 本体分类名称|term
     * @param id 主键ID
     * @return 实体数据；不存在时返回empty
     */
    public Optional<Map<String, Object>> findById(String category, Long id) {
        return repository.findById(category, id);
    }

    /** Create entity. 新增本体元数据实体.
     * @param category 本体分类名称|term
     * @param data 实体属性键值对
     * @return 新增记录的主键ID
     */
    @Transactional
    public Long create(String category, Map<String, Object> data) {
        return repository.insert(category, data);
    }

    /** Update entity. 修改本体元数据实体.
     * @param category 本体分类名称|term
     * @param id 主键ID
     * @param data 需要更新的属性键值对
     * @return 受影响的行数
     */
    @Transactional
    public int update(String category, Long id, Map<String, Object> data) {
        return repository.update(category, id, data);
    }

    /** Delete entity. 软删除本体元数据实体.
     * @param category 本体分类名称|term
     * @param id 主键ID
     * @return 受影响的行数
     */
    @Transactional
    public int delete(String category, Long id) {
        return repository.softDelete(category, id);
    }

    /** Count entities by category. 统计指定分类的未删除实体数量.
     * @param category 本体分类名称|term
     * @return 实体总数
     */
    public long count(String category) {
        return repository.count(category);
    }

    /** Search entities by keyword. 关键词搜索本体元数据实体.
     * @param keyword 搜索关键词
     * @param category 限定分类，为空则搜索全部元数据分类
     * @param limit 返回条数限制
     * @return 匹配的实体列表
     */
    public List<Map<String, Object>> search(String keyword, String category, int limit) {
        return repository.search(keyword, category, limit);
    }

    /** Query by filters. 根据过滤条件查询实体.
     * @param category 本体分类名称|term
     * @param filters 过滤条件键值对
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的实体列表
     */
    public List<Map<String, Object>> queryByFilters(String category, Map<String, Object> filters, int pageNum, int pageSize) {
        return repository.queryByCategoryAndFilters(category, filters, pageNum, pageSize);
    }

    /** Find i18n translations. 获取实体的所有多语种翻译.
     * @param entityId 实体ID
     * @param category 本体分类名称
     * @return 翻译列表
     */
    public List<Map<String, Object>> findI18n(Long entityId, String category) {
        return repository.findI18n(entityId, category);
    }

    /** Upsert i18n translation. 新增或更新多语种翻译.
     * @param entityId 实体ID
     * @param category 本体分类名称
     * @param lang 语种
     * @param name 该语种名称
     * @param description 该语种描述
     * @return 翻译记录ID
     */
    @Transactional
    public Long upsertI18n(Long entityId, String category, String lang, String name, String description) {
        return repository.upsertI18n(entityId, category, lang, name, description);
    }

    /** Delete i18n translation. 删除多语种翻译.
     * @param entityId 实体ID
     * @param category 本体分类名称
     * @param lang 语种
     * @return 受影响的行数
     */
    @Transactional
    public int deleteI18n(Long entityId, String category, String lang) {
        return repository.deleteI18n(entityId, category, lang);
    }

    /** Search i18n by keyword. 在翻译表中按关键词搜索.
     * @param keyword 搜索关键词
     * @param lang 语种，为空则搜索所有语种
     * @param limit 返回条数限制
     * @return 匹配的翻译列表
     */
    public List<Map<String, Object>> searchI18n(String keyword, String lang, int limit) {
        return repository.searchI18n(keyword, lang, limit);
    }
}
