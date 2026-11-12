package net.linlan.dsh.common.exception;

import net.linlan.dsh.ontology.engine.validation.FieldViolation;

import java.util.List;

/** 写库前 Schema 校验失败；由全局异常处理器映射为 HTTP 400 + Result.badRequest。 */
public class SchemaValidationException extends RuntimeException {

    private final transient List<FieldViolation> violations;
    private final String category;

    public SchemaValidationException(String category, List<FieldViolation> violations) {
        super("本体字段校验失败: category=" + category + ", violations=" + violations.size());
        this.category = category;
        this.violations = List.copyOf(violations);
    }

    public String category() {
        return category;
    }

    public List<FieldViolation> violations() {
        return violations;
    }
}
