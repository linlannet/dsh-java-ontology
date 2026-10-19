package net.linlan.dsh.common.controller;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.entity.BaseEntity;
import net.linlan.dsh.common.service.BaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<T extends BaseEntity> {

    protected abstract BaseService<T> getService();

    @GetMapping("/{id}")
    public ResponseEntity<Result<T>> getById(@PathVariable Long id) {
        return getService().findById(id)
                .map(e -> ResponseEntity.ok(Result.success(e)))
                .orElseGet(() -> ResponseEntity.ok(Result.notFound("记录不存在: " + id)));
    }

    @GetMapping
    public ResponseEntity<Result<List<T>>> getAll() {
        return ResponseEntity.ok(Result.success(getService().findAll()));
    }

    @GetMapping("/page")
    public ResponseEntity<Result<PageResult<T>>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(Result.success(getService().findPage(pageNum, pageSize)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Integer>> delete(@PathVariable Long id) {
        int rows = getService().softDelete(id);
        return ResponseEntity.ok(Result.success("删除" + rows + "条记录", rows));
    }

    @GetMapping("/count")
    public ResponseEntity<Result<Long>> count() {
        return ResponseEntity.ok(Result.success(getService().count()));
    }
}
