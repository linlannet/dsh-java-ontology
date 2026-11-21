package net.linlan.dsh.plat.model;

import java.util.List;

/** 建模预览请求：aliases 指定聚合根；all=true 表示覆盖全部。 */
public record MaterializeRequest(List<String> aliases, boolean all) {

    public MaterializeRequest {
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }
}
