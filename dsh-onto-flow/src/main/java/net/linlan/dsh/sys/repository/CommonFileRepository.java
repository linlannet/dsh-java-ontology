package net.linlan.dsh.sys.repository;

import net.linlan.dsh.sys.entity.CommonFile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

/**
 * 统一文件数据访问层（P1-9）
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Repository
public class CommonFileRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommonFileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<CommonFile> FILE_ROW_MAPPER = (rs, rowNum) -> {
        CommonFile file = new CommonFile();
        file.setId(rs.getLong("id"));
        file.setFileName(rs.getString("file_name"));
        file.setFilePath(rs.getString("file_path"));
        file.setFileType(rs.getString("file_type"));
        long fileSize = rs.getLong("file_size");
        file.setFileSize(rs.wasNull() ? null : fileSize);
        file.setMimeType(rs.getString("mime_type"));
        file.setRefCount(rs.getInt("ref_count"));
        file.setStorageType(rs.getString("storage_type"));
        long tenantId = rs.getLong("tenant_id");
        file.setTenantId(rs.wasNull() ? null : tenantId);
        file.setCreatedBy(rs.getString("created_by"));
        file.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        file.setUpdatedBy(rs.getString("updated_by"));
        file.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        file.setDelFlag(rs.getInt("del_flag"));
        file.setRemark(rs.getString("remark"));
        return file;
    };

    public long insert(CommonFile file) {
        String sql = "INSERT INTO onto_common_file (file_name, file_path, file_type, file_size, mime_type, " +
                "ref_count, storage_type, tenant_id, created_by, remark) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, file.getFileName());
            ps.setString(2, file.getFilePath());
            ps.setString(3, file.getFileType());
            if (file.getFileSize() != null) ps.setLong(4, file.getFileSize()); else ps.setNull(4, java.sql.Types.BIGINT);
            ps.setString(5, file.getMimeType());
            ps.setString(6, file.getStorageType() != null ? file.getStorageType() : "LOCAL");
            if (file.getTenantId() != null) ps.setLong(7, file.getTenantId()); else ps.setNull(7, java.sql.Types.BIGINT);
            ps.setString(8, file.getCreatedBy());
            ps.setString(9, file.getRemark());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Optional<CommonFile> findById(Long id) {
        String sql = "SELECT * FROM onto_common_file WHERE id = ? AND del_flag = 0";
        List<CommonFile> list = jdbcTemplate.query(sql, FILE_ROW_MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<CommonFile> findByTenantId(Long tenantId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM onto_common_file WHERE tenant_id = ? AND del_flag = 0 ORDER BY id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, FILE_ROW_MAPPER, tenantId, pageSize, offset);
    }

    public int incrementRefCount(Long id) {
        String sql = "UPDATE onto_common_file SET ref_count = ref_count + 1 WHERE id = ? AND del_flag = 0";
        return jdbcTemplate.update(sql, id);
    }

    public int decrementRefCount(Long id) {
        String sql = "UPDATE onto_common_file SET ref_count = ref_count - 1 WHERE id = ? AND del_flag = 0 AND ref_count > 0";
        return jdbcTemplate.update(sql, id);
    }

    public int softDelete(Long id) {
        String sql = "UPDATE onto_common_file SET del_flag = 1, updated_at = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
