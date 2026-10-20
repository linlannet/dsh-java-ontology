package net.linlan.dsh.onto.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.onto.model.AppInfo;
import net.linlan.dsh.onto.model.EndpointDescriptor;
import net.linlan.dsh.onto.service.AppMetaService;
import net.linlan.dsh.sys.security.PublicApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 聚合应用入口控制类
 * <p>应用层唯一端点前缀 {@code /api/app}，提供应用元信息、入口端点清单与健康自检，
 * 不与 base/flow/qa/viz/metric 模块端点（/api/ontologies、/api/workline、/api/auth 等）重复。</p>
 * <p>元信息端点标记 {@code @PublicApi}：无业务数据泄露，供网关探活与运维使用。</p>
 * @author Linlan
 * CreatedAt 2026-10-19
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/app")
public class AppController {

    private final AppMetaService metaService;

    public AppController(AppMetaService metaService) {
        this.metaService = metaService;
    }

    /** 应用元信息 */
    @PublicApi
    @GetMapping("/info")
    public Result<AppInfo> info() {
        return Result.success(metaService.appInfo());
    }

    /** 全部入口端点清单（按模块分组） */
    @PublicApi
    @GetMapping("/endpoints")
    public Result<Map<String, List<EndpointDescriptor>>> endpoints() {
        return Result.success(metaService.endpoints());
    }

    /** 应用健康自检（数据源 + Flyway 迁移状态） */
    @PublicApi
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success(metaService.health());
    }
}
