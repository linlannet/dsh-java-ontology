# 模型快照（v0.2.2）

本目录是七模型 YAML 的**离线快照**，仅用于回滚、diff 与基线复核，不参与构建、不在 classpath 中，不会被 `dsh-onto-base` 的模型加载器扫描。

## 目录

| 路径 | 内容 |
|------|------|
| `current/base/` | P1 完成后的基座模型（`ontology-models/` 7 个 YAML + `manifest.json`） |
| `current/siis/` | P1 完成后的项目层模型（`siis-models/` 7 个 YAML + `manifest.json`） |
| `original/base/` | P1 之前的基座模型（取自平台仓库 `HEAD`，用于 diff/回滚） |
| `original/siis/` | P1 之前的项目层模型（取自 SIIS 仓库 `HEAD`，用于 diff/回滚） |
| `snapshot.py` | 一次性统计脚本，输出聚合根/字段/枚举数 |

## 用法

```bash
python3 docs/0.2.2/model-snapshot/snapshot.py          # 明细
python3 docs/0.2.2/model-snapshot/snapshot.py --json   # JSON
python3 docs/0.2.2/model-snapshot/snapshot.py --check  # 与冻结基线比对
```

依赖 PyYAML（`pip3 install pyyaml`），不新增 Maven 插件。

## 冻结基线

| 层 | 聚合根 | 字段 | 枚举 |
|----|--------|------|------|
| 基座 | 25 | 200 | 16 |
| 项目层 | 27 | 292 | 14 |
| 合计 | 52 | 492 | 30 |

口径与 `AggregateDescriptor.totalAttributeCount()` 一致：字段与枚举递归统计嵌套 `entities`，聚合根只统计顶层。

对应测试：`OntologyYamlBaselineTest`（基座）、`SiisObjectYamlBaselineTest`（项目层）。

## diff 示例

```bash
diff -u docs/0.2.2/model-snapshot/original/base/m1-object-model.yaml \
        docs/0.2.2/model-snapshot/current/base/m1-object-model.yaml
```

## 回滚点（T0.5）

| 仓库 | 回滚点 |
|------|--------|
| `dsh-java-ontology` | tag `0.2.1`；v0.2.2 提交 `c30015b`；P1 改动未提交 |
| `dsh-ontolab-siis` | 提交 `ac0f1ea`；P1 改动未提交 |

回滚方式：代码回退到上表提交（或 `git checkout 0.2.1`），关闭 `dsh.ontology.model.enabled` / `dsh.ontology.schema-validation.enabled` 作为降级；本版本无 schema 变更，无需数据迁移回滚。
