package net.linlan.dsh.common.controller;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.service.CrudService;
import net.linlan.dsh.sys.security.RequiresRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通用 CRUD 控制器基类
 * <p>P2-16：收敛 Track/Analyze/Display/Workspace 四份 176 行重复 Controller。
 * 子类只需标注 {@code @RequestMapping} 前缀并传入 {@link CrudService} 实例。
 * <p>所有端点要求 roleLevel &lt;= 1（主任 + 研究员）。
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public abstract class GenericCrudController {

    protected abstract CrudService crudService();

    @GetMapping("/categories")
    @RequiresRole(1)
    public ResponseEntity<Result<List<String>>> categories() {
        return ResponseEntity.ok(Result.success(crudService().getCategories()));
    }

    @GetMapping("/{category}")
    @RequiresRole(1)
    public ResponseEntity<Result<List<Map<String, Object>>>> list(@PathVariable String category) {
        return ResponseEntity.ok(Result.success(crudService().findAll(category)));
    }

    @GetMapping("/{category}/typed")
    @RequiresRole(1)
    public ResponseEntity<Result<List<?>>> listTyped(@PathVariable String category) {
        return ResponseEntity.ok(Result.success(crudService().findAllTyped(category)));
    }

    @GetMapping("/{category}/page")
    @RequiresRole(1)
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> page(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(Result.success(crudService().findPage(category, pageNum, pageSize)));
    }

    @GetMapping("/{category}/{id}")
    @RequiresRole(1)
    public ResponseEntity<Result<Map<String, Object>>> getById(
            @PathVariable String category,
            @PathVariable Long id) {
        return crudService().findById(category, id)
                .map(e -> ResponseEntity.ok(Result.success(e)))
                .orElseGet(() -> ResponseEntity.ok(Result.notFound("记录不存在: " + id)));
    }

    @PostMapping("/{category}")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> create(
            @PathVariable String category,
            @RequestBody Map<String, Object> data) {
        Long id = crudService().create(category, data);
        return ResponseEntity.ok(Result.success("创建成功", id));
    }

    @PutMapping("/{category}/{id}")
    @RequiresRole(1)
    public ResponseEntity<Result<Integer>> update(
            @PathVariable String category,
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        int rows = crudService().update(category, id, data);
        return ResponseEntity.ok(Result.success("更新" + rows + "条记录", rows));
    }

    @DeleteMapping("/{category}/{id}")
    @RequiresRole(1)
    public ResponseEntity<Result<Integer>> delete(
            @PathVariable String category,
            @PathVariable Long id) {
        int rows = crudService().delete(category, id);
        return ResponseEntity.ok(Result.success("删除" + rows + "条记录", rows));
    }

    @GetMapping("/{category}/count")
    @RequiresRole(1)
    public ResponseEntity<Result<Long>> count(@PathVariable String category) {
        return ResponseEntity.ok(Result.success(crudService().count(category)));
    }

    @GetMapping("/search")
    @RequiresRole(1)
    public ResponseEntity<Result<List<Map<String, Object>>>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(Result.success(crudService().search(keyword, category, limit)));
    }

    @PostMapping("/{category}/query")
    @RequiresRole(1)
    public ResponseEntity<Result<List<Map<String, Object>>>> query(
            @PathVariable String category,
            @RequestBody Map<String, Object> filters,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(Result.success(crudService().queryByFilters(category, filters, pageNum, pageSize)));
    }
}
