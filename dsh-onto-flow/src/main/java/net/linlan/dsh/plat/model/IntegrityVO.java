package net.linlan.dsh.plat.model;

import net.linlan.dsh.ontology.engine.validation.IntegrityIssue;

import java.util.List;

/** 完整性报告视图：计数与问题明细，不返回堆栈或 YAML 原文。 */
public record IntegrityVO(
        int aggregateCount,
        int attributeCount,
        int enumCount,
        int duplicateAliases,
        int danglingRefs,
        boolean hasErrors,
        List<IntegrityIssue> issues) {

    public IntegrityVO {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}
