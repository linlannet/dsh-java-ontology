package net.linlan.dsh.sys.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.sys.entity.NotifyMessage;
import net.linlan.dsh.sys.entity.NotifySubscription;
import net.linlan.dsh.sys.security.AuditLog;
import net.linlan.dsh.sys.security.RequiresRole;
import net.linlan.dsh.sys.service.NotifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知订阅控制器（P1-11）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@RestController
@RequestMapping("/api/notify")
public class NotifyController {

    private final NotifyService notifyService;

    public NotifyController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    @PostMapping("/subscribe")
    @RequiresRole(1)
    @AuditLog(action = "SUBSCRIBE", targetType = "notify_subscription")
    public ResponseEntity<Result<Long>> subscribe(@RequestBody NotifySubscription sub) {
        sub.setSubscriberId(TenantContext.getUserId());
        sub.setTenantId(TenantContext.getTenantId());
        sub.setCreatedBy(String.valueOf(TenantContext.getUserId()));
        long id = notifyService.subscribe(sub);
        return ResponseEntity.ok(Result.success("订阅成功", id));
    }

    @DeleteMapping("/subscribe/{id}")
    @RequiresRole(1)
    @AuditLog(action = "UNSUBSCRIBE", targetType = "notify_subscription")
    public ResponseEntity<Result<Integer>> unsubscribe(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(notifyService.unsubscribe(id)));
    }

    @GetMapping("/my-subscriptions")
    @RequiresRole(2)
    public ResponseEntity<Result<List<NotifySubscription>>> mySubscriptions() {
        return ResponseEntity.ok(Result.success(notifyService.findMySubscriptions(TenantContext.getUserId())));
    }

    @GetMapping("/my-messages")
    @RequiresRole(2)
    public ResponseEntity<Result<List<NotifyMessage>>> myMessages(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(Result.success(notifyService.findMyMessages(TenantContext.getUserId(), pageNum, pageSize)));
    }

    @PostMapping("/send")
    @RequiresRole(0)
    @AuditLog(action = "NOTIFY_SEND", targetType = "notify_message")
    public ResponseEntity<Result<Integer>> send(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam String title,
            @RequestParam String content) {
        int count = notifyService.notifySubscribers(targetType, targetId, title, content);
        return ResponseEntity.ok(Result.success("通知" + count + "位订阅者", count));
    }
}
