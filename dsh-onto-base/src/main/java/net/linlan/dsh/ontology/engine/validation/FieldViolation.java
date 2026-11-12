package net.linlan.dsh.ontology.engine.validation;

/** 字段级校验违规：field/rule/expected/actual/message。 */
public record FieldViolation(
        String field,
        String rule,
        Object expected,
        Object actual,
        String message) {
}
