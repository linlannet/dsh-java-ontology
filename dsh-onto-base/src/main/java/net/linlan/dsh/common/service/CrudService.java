package net.linlan.dsh.common.service;

import net.linlan.dsh.common.PageResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 通用 CRUD 服务接口
 * <p>P2-16：提取 Track/Analyze/Display/Workspace 四份重复 Service 的统一接口，
 * 供 {@code GenericCrudController} 基类使用。
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public interface CrudService {

    List<String> getCategories();

    List<Map<String, Object>> findAll(String category);

    <T> List<T> findAllTyped(String category);

    PageResult<Map<String, Object>> findPage(String category, int pageNum, int pageSize);

    Optional<Map<String, Object>> findById(String category, Long id);

    Long create(String category, Map<String, Object> data);

    int update(String category, Long id, Map<String, Object> data);

    int delete(String category, Long id);

    long count(String category);

    List<Map<String, Object>> search(String keyword, String category, int limit);

    List<Map<String, Object>> queryByFilters(String category, Map<String, Object> filters, int pageNum, int pageSize);
}
