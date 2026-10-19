# ADR-004：报告双表收敛

**状态**：Accepted  
**日期**：2026-10-18  
**决策者**：P2-20

## 背景

系统中存在两个报告表：
- `onto_report_instance` — 新报告模块（dsh-onto-report），有 template_id/parameters/status/section 结构化字段
- `gwy_display_rpt_report` — 旧展示交互域（display），被 DisplayRepository 和 WorklineOrchestrator 引用

两者功能重叠，数据冗余，维护成本高。

## 决策

**`onto_report_instance` 为唯一主表**，`gwy_display_rpt_report` 废弃为快照表。

1. 新增 `legacy_report_id` 列到 `onto_report_instance`，用于迁移后回填旧表 ID
2. `gwy_display_rpt_report` 保留只读，不再写入新数据
3. `DisplayRepository` 中 `rptReport` 分类标记为 `@Deprecated`，查询路由到 `onto_report_instance`
4. `WorklineOrchestrator` 中 `gwy_display_rpt_report` 的 COUNT 查询改为查 `onto_report_instance`

## 迁移路径

1. **阶段 1（当前）**：加 `legacy_report_id` 列，双表并存，新数据只写 `onto_report_instance`
2. **阶段 2**：数据迁移脚本将 `gwy_display_rpt_report` 历史数据导入 `onto_report_instance`，回填 `legacy_report_id`
3. **阶段 3**：下线 `gwy_display_rpt_report`，删除 DisplayRepository 中的 `rptReport` 分类

## 影响

- `ReportRepository` — 无变化（已使用 `onto_report_instance`）
- `DisplayRepository` — `rptReport` 分类标记废弃，查询转发
- `WorklineOrchestrator` — COUNT 查询改表名
- Flyway V1.0.6 — ALTER TABLE 加列
