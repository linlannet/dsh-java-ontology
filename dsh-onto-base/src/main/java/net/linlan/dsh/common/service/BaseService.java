package net.linlan.dsh.common.service;

import net.linlan.dsh.common.PageResult;
import net.linlan.dsh.common.entity.BaseEntity;
import net.linlan.dsh.common.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T extends BaseEntity> {

    protected abstract BaseRepository<T> getRepository();

    public Optional<T> findById(Long id) {
        return getRepository().findById(id);
    }

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public PageResult<T> findPage(int pageNum, int pageSize) {
        return getRepository().findPage(pageNum, pageSize);
    }

    @Transactional
    public int softDelete(Long id) {
        return getRepository().softDelete(id);
    }

    public long count() {
        return getRepository().count();
    }
}
