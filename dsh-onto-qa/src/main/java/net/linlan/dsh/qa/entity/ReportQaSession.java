package net.linlan.dsh.qa.entity;

import net.linlan.dsh.common.entity.BaseEntity;

/**
 * 问数会话实体
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 1.0
 */
public class ReportQaSession extends BaseEntity {
    /** 会话名称 */
    private String sessionName;
    /** 用户问题 */
    private String userQuestion;
    /** 上下文（JSON） */
    private String context;
    /** 状态：ACTIVE/CLOSED */
    private String status;

    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }
    public String getUserQuestion() { return userQuestion; }
    public void setUserQuestion(String userQuestion) { this.userQuestion = userQuestion; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
