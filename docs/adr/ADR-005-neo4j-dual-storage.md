# ADR-005：Neo4j 双存储与图谱同步

**状态**：Deferred（骨架已就位，待 Neo4j 实例部署后激活）  
**日期**：2026-10-18  
**决策者**：P2-17

## 背景

当前知识图谱存储在 MySQL 的 `onto_global_kg_node` + `onto_global_kg_edge` 表中。MySQL 擅长事务和关系查询，但图遍历（多跳查询、最短路径、子图提取）性能差。Neo4j 是原生图数据库，图遍历是其核心优势。

## 决策

**MySQL + Neo4j 双写**，MySQL 为权威源，Neo4j 为加速读副本。

1. **写入路径**：MySQL 先写（事务保证），成功后异步写 Neo4j（最终一致）
2. **读取路径**：图遍历查询走 Neo4j，CRUD 查询走 MySQL
3. **同步机制**：`GraphSyncService` 定时/手动全量同步 + 事件驱动增量同步
4. **条件激活**：`@ConditionalOnProperty(name="dsh.neo4j.enabled", havingValue="true")`，默认不激活

## 实现状态

- **已完成**（2026-10-18，四步工作模式改造阶段3，见 four-step-workflow-design.md 第三章）：
  - `net.linlan.dsh.graph.Neo4jConfig`（@ConditionalOnProperty 条件 Driver，dsh.neo4j.* 配置前缀；排除 Neo4jAutoConfiguration 防默认 Driver 干扰降级判断）
  - `net.linlan.dsh.graph.GraphSyncService`：syncAll / syncIncremental / syncArticle（NLP 实时）+ 定时全量，MERGE 幂等（dsh-onto-flow 原 P2-17 骨架已删除）
  - `net.linlan.dsh.graph.GraphQueryService`：Neo4j 优先、MySQL 自动降级（engine 字段标识）
  - `net.linlan.dsh.graph.GraphController`：/api/graph/{nodes,edges,paths}/query、search、key-factors、regional-network、sync
  - kg_query / kg_search / relation_analysis Tool 改造走 /api/graph/* 端点
  - 实施偏差：采用轻量 neo4j-java-driver 而非 spring-boot-starter-data-neo4j（测试环境无 Neo4j 需零干扰）
- **待实施**（需 Neo4j 实例，docker-compose-neo4j.yml 已就绪）：
  - 生产环境部署 + 数据迁移验证
  - 图算法扩展（PageRank/社区发现）

## 触发条件

- 图遍历查询 P95 > 500ms
- 或图谱节点数 > 10 万
- 或业务提出图算法需求（PageRank/社区发现/最短路径）

## 影响

- 新增 Neo4j 运维成本（1 实例 + 监控 + 备份）
- 双写一致性窗口：MySQL 写成功 → Neo4j 写成功之间有延迟（最终一致）
- 降级策略：Neo4j 不可用时回退 MySQL 查询
