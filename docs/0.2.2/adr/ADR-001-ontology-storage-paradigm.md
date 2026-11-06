# ADR-001：本体存储范式决策 — 专表 + 元数据注册表（路线 A）

> **状态**：Accepted
> **日期**：2026-10-18
> **评审关联**：设计评审 3.1 节"本体库存储范式没有定论"

## 上下文

本体库存在两种互斥的建模哲学：

| | 泛化模型（路线 B） | 专表模型（路线 A，当前实现） |
|---|---|---|
| 表结构 | entity + category + property + relation + rule + i18n | country / organization / person / hotspot / term / event_type / location / media / book / paper / bilateral_relation 等专表 |
| 新增实体类型 | 加一行 category + property 定义，不改表 | 建新表 + 改 `TABLE_TO_ENTITY` 白名单 + 改 Repository |
| 查询性能 | 弱（EAV 拼接、多表 UNION） | 强（每类独立索引） |
| 与"本体建模"理念 | 一致：模型可配置 | 冲突：模型写死在 schema 里 |

**当前代码走专表模型**：`OntologyRepository.TABLE_TO_ENTITY` 是硬编码 `Map.ofEntries`，14 个分类固定。前端 `modeling.html` 承诺"AI 生成任意实体建模"，但后端 `getTableName()` 对未知分类抛 `IllegalArgumentException`。

## 决策

**选择路线 A（专表 + 元数据注册表）**，在专表基础上增加元数据层，使新增类型可控可扩展。

### 实现方案

1. **新增 `onto_ontology_type` 元数据表**（底层库 `dsh_java_ontodb`）：

```sql
CREATE TABLE onto_ontology_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(64) NOT NULL UNIQUE,   -- 如 "policy"、"risk"
    table_name VARCHAR(128) NOT NULL,         -- 对应物理表名
    display_name VARCHAR(128),                -- 展示名称
    domain VARCHAR(32) DEFAULT 'siis',        -- 业务域隔离
    field_schema TEXT,                        -- JSON：字段定义（名称、类型、可写、可搜、展示）
    display_config TEXT,                      -- JSON：列表页/详情页展示配置
    status TINYINT DEFAULT 1,                 -- 0=禁用 1=启用
    del_flag TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

2. **`TABLE_TO_ENTITY` 改为元数据缓存**：启动时从 `onto_ontology_type` 加载，替代硬编码 `Map.ofEntries`。`getTableName()` 先查缓存，未命中再查库并刷新缓存。

3. **新增实体类型流程**（不再需要改 Java 代码）：
   - 插入 `onto_ontology_type` 一行（type_code + table_name + field_schema）
   - 由 Flyway 迁移脚本建对应物理表
   - 缓存自动刷新或重启后生效
   - `OntologyRepository` 自动路由到新表

4. **`WRITABLE_FIELDS` 白名单也改为从 `field_schema` 动态生成**，而非硬编码。

### 不选路线 B 的理由

- MySQL 上 EAV 拼接性能不可接受：多跳图谱查询需多次 UNION，百万级数据延迟到分钟级
- 当前 14 类实体已有专表且索引优化，回退到泛化模型是倒退
- 真正的可配置本体适合在图数据库（Neo4j）层实现，而非 MySQL

## 后果

### 正面
- 保留专表查询性能（每类独立索引）
- 新增实体类型不再需要改 Java 代码，只需元数据 + 迁移脚本
- `field_schema` 驱动前端动态渲染，modeling.html 承诺可兑现
- 白名单从元数据生成，安全性不降级

### 负面 / 限制
- 新增类型仍需建物理表（不能纯配置化），但比改 Java 代码轻量得多
- `onto_ontology_type` 缓存需刷新策略（启动加载 + 手动刷新 API）
- 跨类型查询仍需遍历多表（但这是专表模型的固有代价，可接受）

### 需要同步修改的代码
- `OntologyRepository.TABLE_TO_ENTITY` → 改为从 `onto_ontology_type` 加载
- `OntologyRepository.WRITABLE_FIELDS` → 改为从 `field_schema` 解析
- `OntologyRepository.SEARCH_COLUMNS` → 同上
- 新增 `OntologyTypeService`：管理元数据 CRUD + 缓存刷新

## 验收标准

- [ ] `onto_ontology_type` 表建表 + 种子数据（14 个现有类型）
- [ ] `OntologyRepository` 启动时从元数据加载，14 个现有类型全部通过
- [ ] 新增类型（如 "policy"）只需插入元数据 + 建表，不改 Java 代码即可 CRUD
- [ ] `modeling.html` 新建类型表单提交后，后端不抛 `IllegalArgumentException`
- [ ] 全量测试通过
