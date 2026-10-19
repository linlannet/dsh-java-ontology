package net.linlan.dsh.common.entity;

/**
 * LLM对话历史实体类
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
public class CommonLlmChatHistory extends BaseEntity {
    /** Agent名称 */
    private String agentName;
    /** 角色：user/assistant/system */
    private String role;
    /** 内容 */
    private String content;
    /** 会话ID */
    private String sessionId;

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
