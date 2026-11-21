# 模型物化（建模落库）两阶段确认操作指引

> 目标：从 `dsh-onto-base/src/main/resources/ontology-models/*.yaml` 的分析结果生成表设计，
> 让用户先用 draw.io 审阅，再由用户**明确下达入库指令并二次确认**后，才在数据库建表。
>
> **安全铁律**：`preview` 只读不写库；`confirm` 必须携带一次性令牌 + 确认短语 + 一致的 aliases，
> 四重校验全部通过才执行 `CREATE TABLE IF NOT EXISTS`。任何一步不符都不建表。

---

## 1. 链路总览

```text
ontology-models/*.yaml（启动加载为内存快照 OntologyModelEngine）
        │
        │ ① POST /api/model/materialize/preview  （只读，不写库）
        ▼
表设计 = DDL + draw.io ER 图 + 一次性确认令牌（10 分钟有效）
        │
        │ 用户用 draw.io 打开图、审阅 DDL，明确说"确认建表"
        ▼
② POST /api/model/materialize/confirm  （四重校验）
        │  token 未过期 + confirmPhrase=CONFIRM_CREATE_TABLE
        │  + aliases 与预览一致 + 模型指纹未变
        ▼
CREATE TABLE IF NOT EXISTS onto_model_* （真正落库）
```

---

## 2. 前置条件

1. 平台 8100 已启动：`docs/0.2.0` 或 `scripts/start-local-jar.sh`（H2）/ `start-mysql-jar.sh`（MySQL）。
2. 模型引擎启用（默认）：`dsh.ontology.model.enabled=true`。
3. 所有 `/api/**` 端点受 `RoleInterceptor` fail-closed 保护，请求头必须带角色：
   - 预览：`X-Role-Level: 1`（研究员及以上）
   - 确认建表：`X-Role-Level: 0`（主任）

---

## 3. 阶段一：生成表设计并审阅

### 3.1 调 API

```bash
# 预览指定模型（SysUser / OntologyVersion 等 alias）
curl -s -X POST 'http://127.0.0.1:8100/api/model/materialize/preview' \
  -H 'Content-Type: application/json' \
  -H 'X-Role-Level: 1' \
  -d '{"aliases":["SysUser","OntologyVersion"],"all":false}'
```

返回要点：
- `token`：一次性确认令牌（10 分钟内有效，用后即失效）
- `tables[].ddl`：可直接执行的建表 SQL（字段映射、NOT NULL/UNIQUE、枚举注释、审计字段）
- `tables[].columns[]`：字段级设计（来源 `sourcePath` 可追溯到 YAML 行）
- `drawioXml`：完整 draw.io 文件 XML（表节点 + 父子实体连线）

### 3.2 用 draw.io 查看设计图

```bash
# 把 drawioXml 保存为 .drawio 文件（从返回 JSON 提取）
# jq -r '.data.drawioXml' > model-design.drawio
open -a "draw.io" model-design.drawio     # macOS；或用浏览器打开 https://app.diagrams.net 导入该文件
```

图中每张表是一个节点，标注表名、字段名、SQL 类型、`*` 必填、`[UQ]` 唯一、`{枚举值}`；
子实体通过 `owner` 连线指向父表。

### 3.3 审阅要点

- 字段类型映射是否合理（如 `Enum` → `VARCHAR(128)`，`Long` → `BIGINT`）
- `NOT NULL` / 唯一键是否符合预期
- 父子表 `owner_id` 关联是否正确
- 表名前缀固定为 `onto_model_`，不会与 Flyway 管理的 `onto_*` 表冲突

---

## 4. 阶段二：用户明确确认后建表

> 必须用户先看懂表设计、明确说"确认建表"后才执行。Agent 侧由 `model_confirm` 工具强制此顺序。

```bash
curl -s -X POST 'http://127.0.0.1:8100/api/model/materialize/confirm' \
  -H 'Content-Type: application/json' \
  -H 'X-Role-Level: 0' \
  -d '{
        "token": "<预览返回的 token>",
        "confirmPhrase": "CONFIRM_CREATE_TABLE",
        "aliases": ["SysUser","OntologyVersion"]
      }'
```

四重校验（任一不满足均返回错误且**不建表**）：
1. `confirmPhrase` 必须精确等于 `CONFIRM_CREATE_TABLE`
2. `token` 存在、未过期（10 分钟）、未使用（一次性）
3. `aliases` 与该次预览完全一致
4. 模型指纹未变化（预览后 YAML/版本变了则须重新预览）

返回 `createdTables`（本次新建）与 `existingTables`（已存在，被 `IF NOT EXISTS` 跳过）。

---

## 5. 通过 dsh-java 输入框测试（端到端）

1. 确保 Harness 8090 已启动，且已安装并激活 `dsh-onto-plugin`（见 `scripts/install-plugin.sh`）。
2. 确保平台 8100 已启动。
3. 在 dsh-java 会话输入框输入：

   ```
   用 model_preview 预览 SysUser 和 OntologyVersion 的表设计，把 drawio 图链接给我看
   ```

4. 审阅返回的 DDL 与 ER 图后，输入：

   ```
   确认建表，用刚才的 token 调 model_confirm，aliases 保持 SysUser 和 OntologyVersion
   ```

5. Agent 应调用 `plugin__dsh-onto-plugin__model_confirm`；若你没明确说"确认建表"，它不应调用该工具。

---

## 6. 验证落库结果

```bash
# MySQL
mysql -uroot -p -e "SHOW TABLES LIKE 'onto_model_%'; DESCRIBE onto_model_sys_user;" dsh_java_ontodb

# H2 控制台：http://127.0.0.1:8100/h2-console
# SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'ONTO_MODEL_%';
```

---

## 7. 设计说明与边界

- 生成的表是**模型快照的物化视图**，不修改 Flyway 管理的历史表；表名统一 `onto_model_` 前缀。
- 当前版本每次 `confirm` 执行 `CREATE TABLE IF NOT EXISTS`，已存在的表不覆盖、不 `ALTER`。
- 模型字段类型 → SQL 类型映射见 `ModelMaterializeService.sqlType()`；未知类型统一 `VARCHAR(255)`。
- 令牌存储在服务实例内存（`ConcurrentHashMap`），服务重启后失效，须重新预览。
- draw.io XML 由服务端生成，可直接用 draw.io Desktop 或 app.diagrams.net 打开。
