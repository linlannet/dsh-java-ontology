package net.linlan.dsh.workspace.service;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.common.service.CrudService;
import net.linlan.dsh.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 知识沉淀域CRUD服务类
 * @author Linlan
 * CreatedAt 2026-10-13
 * @version 1.0
 * @since 1.0
 */
@Service
public class WorkspaceCrudService implements CrudService {

    private final WorkspaceRepository repository;

    public WorkspaceCrudService(WorkspaceRepository repository) {
        this.repository = repository;
    }

    /** Find all entities by category. 根据分类获取当前租户所有未删除的实体列表.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return 实体列表
     */
    public List<Map<String, Object>> findAll(String category) {
        return repository.findAll(category);
    }

    /** Find all typed entities by category. 根据分类获取当前租户所有未删除的类型化知识沉淀实体列表.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return 类型化实体列表
     */
    public <T> List<T> findAllTyped(String category) {
        return repository.findAllTyped(category);
    }

    /** Find page by category. 根据分类分页获取当前租户实体列表.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Map<String, Object>> findPage(String category, int pageNum, int pageSize) {
        return repository.findPage(category, pageNum, pageSize);
    }

    /** Find entity by id. 根据分类和主键获取当前租户实体详细信息.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param id 主键ID
     * @return 实体数据；不存在时返回empty
     */
    public Optional<Map<String, Object>> findById(String category, Long id) {
        return repository.findById(category, id);
    }

    /** Create entity. 新增知识沉淀实体.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param data 实体属性键值对
     * @return 新增记录的主键ID
     */
    @Transactional
    public Long create(String category, Map<String, Object> data) {
        return repository.insert(category, data);
    }

    /** Update entity. 修改知识沉淀实体.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param id 主键ID
     * @param data 需要更新的属性键值对
     * @return 受影响的行数
     */
    @Transactional
    public int update(String category, Long id, Map<String, Object> data) {
        return repository.update(category, id, data);
    }

    /** Delete entity. 软删除知识沉淀实体.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param id 主键ID
     * @return 受影响的行数
     */
    @Transactional
    public int delete(String category, Long id) {
        return repository.softDelete(category, id);
    }

    /** Count entities by category. 统计指定分类的当前租户未删除实体数量.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @return 实体总数
     */
    public long count(String category) {
        return repository.count(category);
    }

    /** Search entities by keyword. 关键词搜索知识沉淀实体.
     * @param keyword 搜索关键词
     * @param category 限定分类，为空则搜索全部分类
     * @param limit 返回条数限制
     * @return 匹配的实体列表
     */
    public List<Map<String, Object>> search(String keyword, String category, int limit) {
        return repository.search(keyword, category, limit);
    }

    /** Query by filters. 根据过滤条件查询当前租户实体.
     * @param category 知识沉淀分类名称|discoveredEntity
     * @param filters 过滤条件键值对
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的实体列表
     */
    public List<Map<String, Object>> queryByFilters(String category, Map<String, Object> filters, int pageNum, int pageSize) {
        return repository.queryByCategoryAndFilters(category, filters, pageNum, pageSize);
    }

    /** Get all categories. 获取所有支持的知识沉淀分类列表.
     * @return 分类名称列表
     */
    public List<String> getCategories() {
        return List.copyOf(WorkspaceRepository.TABLE_TO_ENTITY.keySet());
    }
}
