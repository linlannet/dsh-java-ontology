package net.linlan.dsh.sys.service;

import net.linlan.dsh.sys.entity.SysAuditLog;
import net.linlan.dsh.sys.repository.SysAuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计日志服务
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Service
public class SysAuditLogService {

    private final SysAuditLogRepository repository;

    public SysAuditLogService(SysAuditLogRepository repository) {
        this.repository = repository;
    }

    @Async
    public void asyncSave(SysAuditLog logEntry) {
        repository.insert(logEntry);
    }

    public long save(SysAuditLog logEntry) {
        return repository.insert(logEntry);
    }

    public List<SysAuditLog> findPage(int pageNum, int pageSize) {
        return repository.findPage(pageNum, pageSize);
    }

    public List<SysAuditLog> findByUserId(Long userId, int pageNum, int pageSize) {
        return repository.findByUserId(userId, pageNum, pageSize);
    }

    public List<SysAuditLog> findByAction(String action, int pageNum, int pageSize) {
        return repository.findByAction(action, pageNum, pageSize);
    }

    public long count() {
        return repository.count();
    }
}
