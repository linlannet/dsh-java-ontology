# ADR-002 向量检索方案选型

| 项目 | 内容 |
| --- | --- |
| 编号 | ADR-002 |
| 标题 | 向量检索方案选型 |
| 状态 | Accepted |
| 决策日期 | 2026-10-18 |
| 适用阶段 | v0.2.1 |
| 关联模块 | dsh-java-ontology / gwy_common_personal_vector_index |

## 1. 背景

当前系统通过 `gwy_common_personal_vector_index` 表存储向量数据（字段类型为 `BLOB`），但仅完成向量持久化，**未引入任何向量检索引擎**，无法执行 ANN（近似最近邻）检索。

随着 `dsh-java-ontology` 知识图谱模块（`KgNode` / `KgEdge`）与个人画像向量索引的逐步落地，需要明确：**是否在 v0.2.1 阶段引入外部向量库以支撑语义检索能力**。

当前检索能力现状：

- **全文检索**：依赖 MySQL `FULLTEXT` 索引（`AGAINST ... IN BOOLEAN MODE`）。
- **结构化检索**：依赖 `KgNode` / `KgEdge` 图谱节点与边的关系查询。
- **向量存储**：`personal_vector_index` 表仅落库，未参与检索。

## 2. 决策选项

### 选项 A：引入 Milvus（外部专用向量库）

- **优点**：高性能 ANN 检索，支持 IVF / HNSW 等多种索引，水平扩展能力强。
- **缺点**：引入独立组件，增加部署与运维复杂度（etcd + MinIO + Milvus 三件套）；当前团队无 Milvus 运维经验。
- **适用场景**：向量数据量 > 千万级、P95 延迟要求 < 100ms。

### 选项 B：引入 pgvector（PostgreSQL 扩展）

- **优点**：与关系库一体，运维成本低于 Milvus；SQL 原生支持向量检索。
- **缺点**：当前技术栈为 MySQL，引入 PostgreSQL 需新增数据源、双写一致性方案与连接池配置；pgvector 索引性能在高并发下弱于专用向量库。
- **适用场景**：已使用 PostgreSQL 或可接受栈迁移。

### 选项 C：暂不引入向量库，仅用全文检索 + 知识图谱

- **优点**：零新增组件，运维成本不变；复用现有 MySQL `FULLTEXT` 与 `KgNode` / `KgEdge` 能力；迭代速度快。
- **缺点**：语义相似度检索能力缺失；召回精度依赖分词与图谱建模质量。
- **适用场景**：数据量 < 百万级、检索以关键词 + 图谱关系为主。

## 3. 决策

**采用选项 C —— 暂仅全文检索 + 知识图谱（v0.2.1 阶段）。**

v0.2.1 不引入任何外部向量库，检索链路保持：

```
Query → MySQL FULLTEXT (关键词召回)
      → KgNode / KgEdge (图谱关系扩展)
      → 业务排序与过滤
```

`personal_vector_index` 表保留物理结构，但标注为暂不启用，详见第 6 节。

## 4. 理由

1. **数据量不足以需要专用向量库**：当前个人画像向量规模在万级，远低于 Milvus / pgvector 的性能拐点（百万级以上）。
2. **现有能力已满足检索需求**：MySQL `FULLTEXT` + `KgNode` / `KgEdge` 在关键词召回与图谱关系扩展上已覆盖 v0.2.1 全部检索场景，且经过 `dsh-java-ontology` 模块验证可用。
3. **运维成本与 ROI 不匹配**：引入 Milvus 需新增 3 个常驻组件（etcd / MinIO / Milvus），引入 pgvector 需新增 PostgreSQL 数据源与双写逻辑；当前检索 QPS 与延迟均未触及瓶颈，投入产出比不足。
4. **架构演进可逆**：向量检索能力可在后续阶段以插件化方式接入（`dsh-java-{module}` 模块化约定天然支持），当前决策不构成技术债锁定。
5. **后续可 revisit**：当触发条件（第 5 节）满足时，重新评估选项 A / B。

## 5. 后续触发条件

满足以下任一条件时，重新启动向量检索选型评审（revisit 本 ADR）：

| 触发条件 | 阈值 | 监控指标来源 |
| --- | --- | --- |
| 向量检索 P95 延迟 | > 500 ms | 检索接口埋点 |
| 向量数据量 | > 1,000,000 条 | `personal_vector_index` 表行数 |
| 语义召回准确率 | < 业务可接受下限 | 离线评测集 |
| 选项 A / B 运维成本下降 | Milvus Lite 或 pgvector on MySQL 方案成熟 | 社区动态 |

触发后应输出新 ADR（如 ADR-00x）替代本决策，并将本 ADR 状态置为 `Superseded by ADR-00x`。

## 6. 影响与后续动作

### 6.1 数据库层面

- `gwy_common_personal_vector_index` 表**保留**物理结构，不删除、不迁移。
- 在建表脚本与实体类上标注 `@Deprecated(planned)`，注明"待向量检索选型 revisit 后启用或迁移"。
- 新增注释字段 `enable_flag`（默认 `0`），明确该表暂不参与检索链路。

### 6.2 代码层面

- `dsh-java-ontology` 检索入口不引用向量召回分支；向量写入逻辑保留但隔离到独立 Service（如 `PersonalVectorIndexWriteService`），便于后续切换。
- 检索接口契约不变，后续切换向量库时对上层透明。

### 6.3 文档与监控

- 在 `dsh-java-ontology/docs/` 中记录当前检索链路与埋点指标。
- 配置 P95 延迟与表行数监控告警，阈值对齐第 5 节触发条件。

## 7. 相关文档

- ADR-001（如有）：知识图谱建模选型
- `dsh-java-ontology` 模块 README
- `gwy_common_personal_vector_index` 建表 DDL
