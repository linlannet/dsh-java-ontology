package net.linlan.dsh.ontology.engine.validation;

import java.util.List;

/** 完整性报告：启动日志摘要 + 明细。 */
public record IntegrityReport(
        List<IntegrityIssue> issues,
        int aggregateCount,
        int attributeCount,
        int enumCount) {

    public IntegrityReport {
        issues = List.copyOf(issues);
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(i -> i.severity() == IntegrityIssue.Severity.ERROR);
    }

    public List<IntegrityIssue> errors() {
        return issues.stream().filter(i -> i.severity() == IntegrityIssue.Severity.ERROR).toList();
    }

    public List<IntegrityIssue> warnings() {
        return issues.stream().filter(i -> i.severity() == IntegrityIssue.Severity.WARNING).toList();
    }
}
