package net.linlan.dsh.sys.service;

import net.linlan.dsh.sys.entity.CommonFile;
import net.linlan.dsh.sys.repository.CommonFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 统一文件服务（P1-9）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Service
public class CommonFileService {

    private final CommonFileRepository repository;

    public CommonFileService(CommonFileRepository repository) {
        this.repository = repository;
    }

    public long upload(CommonFile file) {
        return repository.insert(file);
    }

    public Optional<CommonFile> findById(Long id) {
        return repository.findById(id);
    }

    public List<CommonFile> findByTenantId(Long tenantId, int pageNum, int pageSize) {
        return repository.findByTenantId(tenantId, pageNum, pageSize);
    }

    public int incrementRefCount(Long id) {
        return repository.incrementRefCount(id);
    }

    public int decrementRefCount(Long id) {
        return repository.decrementRefCount(id);
    }

    public int delete(Long id) {
        return repository.softDelete(id);
    }
}
