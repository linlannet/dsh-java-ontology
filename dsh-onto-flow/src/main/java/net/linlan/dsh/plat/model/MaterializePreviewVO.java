package net.linlan.dsh.plat.model;

import java.util.List;

/** 建模预览结果：仅供审阅，不含任何写库动作。 */
public record MaterializePreviewVO(
        String token,
        long expiresAtMillis,
        int tableCount,
        List<TableDesignVO> tables,
        String drawioXml,
        String warning) {

    public MaterializePreviewVO {
        tables = tables == null ? List.of() : List.copyOf(tables);
    }
}
