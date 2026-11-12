package net.linlan.dsh.ontology.engine.validation;

import java.util.List;

/** 模型完整性校验失败（存在 ERROR 级问题）。 */
public class ModelIntegrityException extends RuntimeException {

    private final transient List<IntegrityIssue> issues;

    public ModelIntegrityException(IntegrityReport report) {
        super("模型完整性校验失败: " + report.errors());
        this.issues = report.errors();
    }

    public List<IntegrityIssue> issues() {
        return issues;
    }
}
