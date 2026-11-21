package net.linlan.dsh.plat.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.ontology.engine.OntologyModelEngine;
import net.linlan.dsh.ontology.engine.loader.ModelManifest;
import net.linlan.dsh.ontology.engine.model.AggregateDescriptor;
import net.linlan.dsh.ontology.engine.validation.IntegrityReport;
import net.linlan.dsh.plat.model.AggregateVO;
import net.linlan.dsh.plat.model.DomainGroupVO;
import net.linlan.dsh.plat.model.IntegrityVO;
import net.linlan.dsh.plat.model.ManifestVO;
import net.linlan.dsh.plat.model.MaterializeConfirmRequest;
import net.linlan.dsh.plat.model.MaterializePreviewVO;
import net.linlan.dsh.plat.model.MaterializeRequest;
import net.linlan.dsh.plat.model.MaterializeResultVO;
import net.linlan.dsh.plat.service.ModelMaterializeService;
import net.linlan.dsh.sys.security.RequiresRole;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/** 模型元数据只读 API（启动快照缓存，不重复解析 YAML）。 */
@RestController
@RequestMapping("/api/model")
public class ModelController {

    private final OntologyModelEngine engine;
    private final ModelMaterializeService materializeService;

    public ModelController(ObjectProvider<OntologyModelEngine> engineProvider,
                           ModelMaterializeService materializeService) {
        this.engine = engineProvider.getIfAvailable();
        this.materializeService = materializeService;
    }

    private static <T> ResponseEntity<Result<T>> unavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Result.fail("模型引擎未启用（dsh.ontology.model.enabled=false）"));
    }

    @GetMapping("/manifest")
    @RequiresRole(1)
    public ResponseEntity<Result<ManifestVO>> manifest() {
        if (engine == null) {
            return unavailable();
        }
        List<ModelManifest> manifests = engine.snapshot().manifests();
        ModelManifest base = manifests.isEmpty() ? null : manifests.get(0);
        ModelManifest project = manifests.size() > 1 ? manifests.get(1) : null;
        IntegrityReport report = engine.integrityReport();
        long projectModels = engine.snapshot().sourceFiles().stream()
                .filter(file -> file.startsWith("siis-models/"))
                .count();
        return ResponseEntity.ok(Result.success(new ManifestVO(
                base == null ? null : base.platform(),
                base == null ? null : base.version(),
                project == null ? null : project.version(),
                base == null ? List.of() : base.baseModels(),
                (int) projectModels,
                report.aggregateCount(),
                report.attributeCount(),
                report.enumCount(),
                engine.snapshot().projectLayerPresent())));
    }

    @GetMapping("/aggregates")
    @RequiresRole(1)
    public ResponseEntity<Result<List<DomainGroupVO>>> aggregates() {
        if (engine == null) {
            return unavailable();
        }
        Map<String, List<AggregateVO>> grouped = new LinkedHashMap<>();
        Map<String, Set<String>> seen = new LinkedHashMap<>();
        for (AggregateDescriptor descriptor : engine.registry().all()) {
            List<String> tags = descriptor.tags().isEmpty() ? List.of("未分组") : descriptor.tags();
            AggregateVO vo = new AggregateVO(descriptor.alias(), descriptor.name(),
                    descriptor.category(), descriptor.totalAttributeCount());
            for (String tag : tags) {
                grouped.computeIfAbsent(tag, key -> new ArrayList<>());
                if (seen.computeIfAbsent(tag, key -> new HashSet<>()).add(descriptor.alias())) {
                    grouped.get(tag).add(vo);
                }
            }
        }
        List<DomainGroupVO> groups = grouped.entrySet().stream()
                .map(entry -> new DomainGroupVO(entry.getKey(), entry.getValue().size(), entry.getValue()))
                .toList();
        return ResponseEntity.ok(Result.success(groups));
    }

    @GetMapping("/aggregates/{alias}")
    @RequiresRole(1)
    public ResponseEntity<Result<AggregateDescriptor>> aggregate(@PathVariable("alias") String alias) {
        if (engine == null) {
            return unavailable();
        }
        return engine.registry().byAlias(alias)
                .map(descriptor -> ResponseEntity.ok(Result.success(descriptor)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.notFound("未知聚合根: " + alias)));
    }

    @GetMapping("/integrity")
    @RequiresRole(1)
    public ResponseEntity<Result<IntegrityVO>> integrity() {
        if (engine == null) {
            return unavailable();
        }
        IntegrityReport report = engine.integrityReport();
        int duplicateAliases = count(report, "DUPLICATE_ALIAS");
        int danglingRefs = count(report, "DANGLING_REFERENCE");
        return ResponseEntity.ok(Result.success(new IntegrityVO(
                report.aggregateCount(),
                report.attributeCount(),
                report.enumCount(),
                duplicateAliases,
                danglingRefs,
                report.hasErrors(),
                report.issues())));
    }

    /**
     * 建模预览（阶段一）：只读模型快照，生成表设计 DDL、draw.io ER 图与一次性确认令牌。
     * <p>此端点<b>绝不写库</b>；用户审阅设计后须调用 confirm 端点并二次确认。</p>
     */
    @org.springframework.web.bind.annotation.PostMapping("/materialize/preview")
    @RequiresRole(1)
    public Result<MaterializePreviewVO> preview(@org.springframework.web.bind.annotation.RequestBody MaterializeRequest request) {
        return Result.success(materializeService.preview(request));
    }

    /**
     * 确认建表（阶段二）：令牌 + 确认短语 + aliases + 模型指纹四重校验通过后才执行建表。
     * <p>需主任权限（role_level=0）。</p>
     */
    @org.springframework.web.bind.annotation.PostMapping("/materialize/confirm")
    @RequiresRole(0)
    public Result<MaterializeResultVO> confirm(@org.springframework.web.bind.annotation.RequestBody MaterializeConfirmRequest request) {
        return Result.success(materializeService.confirm(request));
    }

    private static int count(IntegrityReport report, String code) {
        return (int) report.issues().stream().filter(issue -> code.equals(issue.code())).count();
    }
}
