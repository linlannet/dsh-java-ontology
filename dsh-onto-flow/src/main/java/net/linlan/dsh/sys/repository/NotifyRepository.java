package net.linlan.dsh.sys.repository;

import net.linlan.dsh.sys.entity.NotifySubscription;
import net.linlan.dsh.sys.entity.NotifyMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 通知订阅数据访问层（P1-11）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Repository
public class NotifyRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotifyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<NotifySubscription> SUB_ROW_MAPPER = (rs, rowNum) -> {
        NotifySubscription sub = new NotifySubscription();
        sub.setId(rs.getLong("id"));
        sub.setSubscriberId(rs.getLong("subscriber_id"));
        sub.setTargetType(rs.getString("target_type"));
        sub.setTargetId(rs.getLong("target_id"));
        sub.setChannel(rs.getString("channel"));
        sub.setEnabled(rs.getInt("enabled"));
        long tenantId = rs.getLong("tenant_id");
        sub.setTenantId(rs.wasNull() ? null : tenantId);
        sub.setCreatedBy(rs.getString("created_by"));
        sub.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        sub.setDelFlag(rs.getInt("del_flag"));
        return sub;
    };

    private static final RowMapper<NotifyMessage> MSG_ROW_MAPPER = (rs, rowNum) -> {
        NotifyMessage msg = new NotifyMessage();
        msg.setId(rs.getLong("id"));
        long subId = rs.getLong("subscription_id");
        msg.setSubscriptionId(rs.wasNull() ? null : subId);
        msg.setRecipientId(rs.getLong("recipient_id"));
        msg.setTitle(rs.getString("title"));
        msg.setContent(rs.getString("content"));
        msg.setChannel(rs.getString("channel"));
        msg.setStatus(rs.getString("status"));
        msg.setSentTime(rs.getTimestamp("sent_time") != null ? rs.getTimestamp("sent_time").toLocalDateTime() : null);
        long tenantId = rs.getLong("tenant_id");
        msg.setTenantId(rs.wasNull() ? null : tenantId);
        msg.setCreatedBy(rs.getString("created_by"));
        msg.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return msg;
    };

    public long insertSubscription(NotifySubscription sub) {
        String sql = "INSERT INTO onto_notify_subscription (subscriber_id, target_type, target_id, channel, enabled, tenant_id, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, sub.getSubscriberId());
            ps.setString(2, sub.getTargetType());
            ps.setLong(3, sub.getTargetId());
            ps.setString(4, sub.getChannel() != null ? sub.getChannel() : "email");
            ps.setInt(5, sub.getEnabled() != null ? sub.getEnabled() : 1);
            if (sub.getTenantId() != null) ps.setLong(6, sub.getTenantId()); else ps.setNull(6, java.sql.Types.BIGINT);
            ps.setString(7, sub.getCreatedBy());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<NotifySubscription> findSubscriptionsByTarget(String targetType, Long targetId) {
        String sql = "SELECT * FROM onto_notify_subscription WHERE target_type = ? AND target_id = ? AND enabled = 1 AND del_flag = 0";
        return jdbcTemplate.query(sql, SUB_ROW_MAPPER, targetType, targetId);
    }

    public List<NotifySubscription> findSubscriptionsBySubscriber(Long subscriberId) {
        String sql = "SELECT * FROM onto_notify_subscription WHERE subscriber_id = ? AND del_flag = 0 ORDER BY id DESC";
        return jdbcTemplate.query(sql, SUB_ROW_MAPPER, subscriberId);
    }

    public int deleteSubscription(Long id) {
        String sql = "UPDATE onto_notify_subscription SET del_flag = 1, updated_at = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public long insertMessage(NotifyMessage msg) {
        String sql = "INSERT INTO onto_notify_message (subscription_id, recipient_id, title, content, channel, status, tenant_id, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            if (msg.getSubscriptionId() != null) ps.setLong(1, msg.getSubscriptionId()); else ps.setNull(1, java.sql.Types.BIGINT);
            ps.setLong(2, msg.getRecipientId());
            ps.setString(3, msg.getTitle());
            ps.setString(4, msg.getContent());
            ps.setString(5, msg.getChannel() != null ? msg.getChannel() : "email");
            ps.setString(6, msg.getStatus() != null ? msg.getStatus() : "PENDING");
            if (msg.getTenantId() != null) ps.setLong(7, msg.getTenantId()); else ps.setNull(7, java.sql.Types.BIGINT);
            ps.setString(8, msg.getCreatedBy());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int markAsSent(Long messageId) {
        String sql = "UPDATE onto_notify_message SET status = 'SENT', sent_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, messageId);
    }

    public List<NotifyMessage> findMessagesByRecipient(Long recipientId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_notify_message WHERE recipient_id = ? ORDER BY id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, MSG_ROW_MAPPER, recipientId, pageSize, offset);
    }
}
