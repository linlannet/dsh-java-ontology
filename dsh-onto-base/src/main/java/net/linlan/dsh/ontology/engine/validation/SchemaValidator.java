package net.linlan.dsh.ontology.engine.validation;

import net.linlan.dsh.ontology.engine.model.AggregateDescriptor;
import net.linlan.dsh.ontology.engine.model.AttributeDescriptor;
import net.linlan.dsh.ontology.engine.registry.OntologyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 写库前字段校验：必填、类型、枚举、未知字段。
 * partial=true 时只校验提交字段；enabled=false 直接返回空列表。
 */
public class SchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);

    private final OntologyRegistry registry;
    private final boolean enabled;
    private final String unknownFieldPolicy;

    public SchemaValidator(OntologyRegistry registry, boolean enabled, String unknownFieldPolicy) {
        this.registry = registry;
        this.enabled = enabled;
        this.unknownFieldPolicy = unknownFieldPolicy == null ? "warn" : unknownFieldPolicy;
    }

    public boolean supports(String category) {
        return category != null && registry.byCategory(category).isPresent();
    }

    public List<FieldViolation> validate(String category, Map<String, Object> data, boolean partial) {
        if (!enabled) {
            return List.of();
        }
        AggregateDescriptor descriptor = registry.byCategory(category)
                .orElseThrow(() -> new IllegalArgumentException("未知本体类型: " + category));
        List<FieldViolation> violations = new ArrayList<>();
        Set<String> known = new HashSet<>();

        for (AttributeDescriptor attribute : descriptor.attributes()) {
            known.add(attribute.name());
            known.add(toSnakeCase(attribute.name()));
            boolean present = data.containsKey(attribute.name()) || data.containsKey(toSnakeCase(attribute.name()));
            if (partial && !present) {
                continue;
            }
            Object value = data.containsKey(attribute.name())
                    ? data.get(attribute.name())
                    : data.get(toSnakeCase(attribute.name()));
            // 主键由数据库自增生成，写入时允许缺省
            boolean autoId = "id".equals(attribute.name()) && !present;
            if (attribute.required() && !autoId && isEmpty(value)) {
                violations.add(new FieldViolation(attribute.name(), "required", "非空", value,
                        "字段 " + attribute.name() + " 为必填"));
                continue;
            }
            if (value != null) {
                checkType(attribute, value, violations);
            }
        }

        for (String field : data.keySet()) {
            if (!known.contains(field)) {
                if ("reject".equals(unknownFieldPolicy)) {
                    violations.add(new FieldViolation(field, "unknown", known, field,
                            "未知字段 " + field));
                } else {
                    log.warn("本体类型 {} 提交未知字段 {}，已按 warn 策略忽略", category, field);
                }
            }
        }
        return violations;
    }

    private void checkType(AttributeDescriptor attribute, Object value, List<FieldViolation> violations) {
        String type = attribute.type() == null ? "String" : attribute.type();
        switch (type) {
            case "Long", "Integer" -> {
                if (!isIntegral(value)) {
                    violations.add(typeViolation(attribute, value, "整数"));
                }
            }
            case "Float", "Double" -> {
                if (!isNumeric(value)) {
                    violations.add(typeViolation(attribute, value, "数字"));
                }
            }
            case "Boolean" -> {
                if (!(value instanceof Boolean) && !"true".equals(String.valueOf(value))
                        && !"false".equals(String.valueOf(value))) {
                    violations.add(typeViolation(attribute, value, "true/false"));
                }
            }
            case "Enum" -> {
                if (!attribute.enumValues().contains(String.valueOf(value))) {
                    violations.add(new FieldViolation(attribute.name(), "enum", attribute.enumValues(),
                            value, "字段 " + attribute.name() + " 取值不在枚举范围"));
                }
            }
            default -> {
                // String/Text/DateTime/Json/Blob：仅做非空检查，类型由上层序列化兜底
            }
        }
    }

    private FieldViolation typeViolation(AttributeDescriptor attribute, Object value, String expected) {
        return new FieldViolation(attribute.name(), "type", expected, value,
                "字段 " + attribute.name() + " 类型不符，期望 " + expected);
    }

    private static String toSnakeCase(String name) {
        StringBuilder builder = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(ch));
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static boolean isEmpty(Object value) {
        return value == null || (value instanceof String s && s.isBlank());
    }

    private static boolean isIntegral(Object value) {
        if (value instanceof Number number) {
            return new BigDecimal(number.toString()).stripTrailingZeros().scale() <= 0;
        }
        try {
            Long.parseLong(String.valueOf(value));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isNumeric(Object value) {
        if (value instanceof Number) {
            return true;
        }
        try {
            new BigDecimal(String.valueOf(value));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
