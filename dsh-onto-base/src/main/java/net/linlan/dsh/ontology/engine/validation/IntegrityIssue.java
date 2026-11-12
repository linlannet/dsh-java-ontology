package net.linlan.dsh.ontology.engine.validation;

/** 完整性检查问题。severity = ERROR/WARNING。 */
public record IntegrityIssue(
        Severity severity,
        String code,
        String message,
        String sourceFile,
        String path) {

    public enum Severity {
        ERROR,
        WARNING
    }
}
