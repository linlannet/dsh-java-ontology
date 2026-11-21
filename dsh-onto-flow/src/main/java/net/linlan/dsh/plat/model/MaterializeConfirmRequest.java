package net.linlan.dsh.plat.model;

import java.util.List;

/** 建表确认请求：必须携带预览令牌与显式确认短语，且 aliases 与预览一致。 */
public record MaterializeConfirmRequest(String token, String confirmPhrase, List<String> aliases) {

    public MaterializeConfirmRequest {
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }
}
