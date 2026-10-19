package net.linlan.dsh.sys.service;

import net.linlan.dsh.sys.entity.NotifyMessage;
import net.linlan.dsh.sys.entity.NotifySubscription;
import net.linlan.dsh.sys.repository.NotifyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务（P1-11）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Service
public class NotifyService {

    private final NotifyRepository repository;

    public NotifyService(NotifyRepository repository) {
        this.repository = repository;
    }

    public long subscribe(NotifySubscription sub) {
        return repository.insertSubscription(sub);
    }

    public int unsubscribe(Long id) {
        return repository.deleteSubscription(id);
    }

    public List<NotifySubscription> findMySubscriptions(Long subscriberId) {
        return repository.findSubscriptionsBySubscriber(subscriberId);
    }

    public int notifySubscribers(String targetType, Long targetId, String title, String content) {
        List<NotifySubscription> subs = repository.findSubscriptionsByTarget(targetType, targetId);
        int count = 0;
        for (NotifySubscription sub : subs) {
            NotifyMessage msg = new NotifyMessage();
            msg.setSubscriptionId(sub.getId());
            msg.setRecipientId(sub.getSubscriberId());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setChannel(sub.getChannel());
            msg.setStatus("PENDING");
            msg.setTenantId(sub.getTenantId());
            msg.setCreatedBy("system");
            long msgId = repository.insertMessage(msg);
            repository.markAsSent(msgId);
            count++;
        }
        return count;
    }

    public List<NotifyMessage> findMyMessages(Long recipientId, int pageNum, int pageSize) {
        return repository.findMessagesByRecipient(recipientId, pageNum, pageSize);
    }
}
