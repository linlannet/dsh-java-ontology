# dsh-java-ontology

[简体中文](./README.md) | English

> **TL;DR** · An ontology knowledge engineering platform (Java) with a base + flow + domain-pack architecture. The aggregation app listens on **8100** and ships 3 Harness tools; the companion SIIS project provides the four-domain business and 12 business tools.
> **Positioning** · A downstream resource service in the WorkBuddy-aligned ecosystem (`dsh-java` / DSH-Desktop); unified identity and tenants are provided by **linlan-emis (BuddyOne)**, and this platform acts as an OAuth2 Resource Server (0.3.0 plan).
> **Keywords**: Ontology, Spring Boot, Java 17, MyBatis, Flyway, JWT, OIDC, Harness Plugin, Knowledge Graph
> **AI entry**: [llms.txt](./llms.txt) · [AGENTS.md](./AGENTS.md)

[![License](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
![java](https://img.shields.io/badge/Java-17-orange.svg)
![spring boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)

## Introduction

An ontology knowledge engineering platform with a **base + flow + domain capability packs** three-layer architecture: static ontology definitions, rules, and shared infrastructure at the base layer (`dsh-onto-base`); dynamic ontology, authentication, and workspace orchestration at the flow layer (`dsh-onto-flow`); and composable domain capability packs at the top layer (Q&A `qa`, visualization `viz`, metrics `metric`). `dsh-onto-app` aggregates them into an independently deployable platform app (port 8100), while `dsh-onto-plugin` ships a `dsh-java` Java Native Plugin (3 atomic tools) that lets AI Agents safely query and search the ontology knowledge base. The repo also ships 3 Agent configs and 4 Skill packs.

Current version: **0.2.2**, contract-aligned with `dsh-java` 0.2.2.

**Identity layering (0.3.0 plan)**: `linlan-emis/backend` (Java) and `../../linlan-emis/server` (Python) are the two BuddyOne routes and issue OIDC tokens uniformly; this platform no longer runs its own IdP and instead validates tokens via JWKS and consumes `tenant_id` / `role_level`. See [docs/0.3.0/auth-tenant-design.md](docs/0.2.3/auth-tenant-design.md). Version 0.2.2 remains on the built-in JWT + BCrypt transition path.

## Features

- **Ontology Management**: Multi-ontology configuration with create, delete, and list views
- **AI Chat Assistant**: Natural language ontology queries with streaming output — thought process → tool calls → final conclusion
- **Concept Query**: Retrieve concept definitions, hierarchical relations, and entity properties by ontology ID and query expression
- **Fuzzy Search**: Quickly locate concepts and entities by keyword
- **Multi-Tenant Auth (transition)**: JWT + BCrypt with director/researcher/guest roles; from 0.3.0 identity is issued by linlan-emis and this platform only verifies tokens
- **Workflow Orchestration**: State machine transitions + task dependencies + parallel execution
- **Global Knowledge Aggregation**: Three-tier knowledge architecture (personal → common → global) + KG convergence
- **Q&A Reporting**: Report and Q&A orchestration across template/section/chart/session/task
- **Metric Modeling**: Business object/process/decision + atom/derived/composite metrics + dependency tree
- **Harness Plugin**: 3 atomic tools registered with dsh-java for Agent invocation

## Version History

All changes are recorded in [CHANGELOG](./CHANGELOG.md). This README keeps only stable architecture, module and usage documentation.

## Table of Contents

- [1. Architecture](#1-architecture)
- [2. Table Naming Convention](#2-table-naming-convention)
- [3. Requirements](#3-requirements)
- [4. Quick Start](#4-quick-start)
- [5. Configuration](#5-configuration)
- [6. Module Dependencies](#6-module-dependencies)
- [7. Companion Project](#7-companion-project)
- [8. Directory Layout](#8-directory-layout)
- [9. Security Boundary](#9-security-boundary)
- [10. Known Items](#10-known-items)
- [11. Testing](#11-testing)
- [12. Documentation Index](#12-documentation-index)
- [Contributing](#contributing)
- [License](#license)

---

## 1. Architecture

```text
dsh-java-ontology/
├── dsh-onto-base/                          Base: static ontology definitions + rules + logic
│   ├── src/main/java/net/linlan/dsh/common/        Common infrastructure (PageRequest/Result/BaseEntity/BaseRepository etc.)
│   ├── src/main/java/net/linlan/dsh/ontology/plugin/  Plugin base classes (AdminApiClient + BaseOntologyTool)
│   └── src/main/resources/ontology-models/          Seven-model YAML (m1~m7 + manifest)
├── dsh-onto-flow/                          Flow: dynamic ontology + workspace + multi-user auth
│   ├── src/main/java/net/linlan/dsh/sys/           Auth (JWT + BCrypt + role interceptor; JWKS verification from 0.3.0)
│   ├── src/main/java/net/linlan/dsh/common/entity/  Common entities (DiscoveredEntity/LlmChatHistory etc.)
│   ├── src/main/java/net/linlan/dsh/plat/          Platform orchestration (OntologyController/HarnessAi/Workline)
│   ├── src/main/java/net/linlan/dsh/global/        Global knowledge aggregation (Aggregation/GlobalKnowledge/KnowledgeAggregator)
│   └── src/main/java/net/linlan/dsh/workspace/     Unified workspace (WorkspaceController)
├── dsh-onto-qa/                            QA: data analysis + reporting + Q&A (template/section/chart/session/task)
├── dsh-onto-viz/                           Visualization: frontend static assets (HTML/CSS/JS)
├── dsh-onto-metric/                        Metric: business object/process/decision + atom/derived/composite metric + dependency tree
├── dsh-onto-app/                           Aggregation app: base + flow + qa + viz + metric (port 8100)
├── dsh-onto-plugin/                        Plugin output: 3 platform tools (workspace/knowledge/director)
├── agents/                                 3 Agent configs (director-assistant/research-assistant/topic-creator)
├── skills/                                 4 Skill packs (hotspot-analysis/knowledge-extraction/report-generation/topic-creation)
└── docs/                                   Design docs (0.2.1 specs / 0.2.2 designs / contracts / ops)
```

```text
`ontology_db` was only a legacy default placeholder; the canonical name is `dsh_java_ontodb`. Browser ──► Admin UI (8100) ──► Admin API ──► Onto DB (default `dsh_java_ontodb`)
               │
               └─ AI Chat ──► dsh-java (8090)
                                 └─► dsh-onto-plugin (3 tools) ──► Platform API
```

## 2. Table Naming Convention

| Prefix | Domain | Module | Table Count |
| --- | --- | --- | --- |
| `onto_sys_` | Auth & permissions | flow | 4 |
| `onto_common_` | Personal knowledge precipitation | flow | 6 |
| `onto_notify_` | Messages & subscriptions | flow | 2 |
| `onto_ontology_` | Ontology library | base | 9 |
| `onto_report_` | Q&A & reporting | qa | 7 |
| `onto_global_` | Global knowledge aggregation | flow | 5 |
| `onto_metric_` | Metrics | metric | 5 |

38 tables total, created by Flyway scripts `V1.0.0~V1.0.1` (`dsh-onto-app`) and `V1.0.2` (`dsh-onto-metric`).

> Split leftover: `V1.0.0__init_schema.sql` still contains 36 `gwy_*` business tables from the merged era; they will be removed from this repo once SIIS is extracted as a standalone project.

## 3. Requirements

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Optional: `dsh-java` (AI chat depends on it for the Agent runtime)

## 4. Quick Start

### 4.1 Build

```bash
git clone git@github.com:linlannet/dsh-java-ontology.git
cd dsh-java-ontology

# Build only the platform modules (recommended; the root pom still declares the nested SIIS modules)
mvn -pl dsh-onto-app -am package -DskipTests
```

> The root pom's `<modules>` still includes `dsh-ontolab-siis/*`, which is ignored by `.gitignore`. Without the SIIS sources, a full `mvn package` fails on the missing child modules; use `-pl dsh-onto-app -am`, or place the SIIS sources first as described in [Companion Project](#7-companion-project).

### 4.2 Start the Aggregation App (Platform)

```bash
java -jar dsh-onto-app/target/dsh-onto-app-0.2.2-exec.jar
```

Or in dev mode:

```bash
mvn spring-boot:run -pl dsh-onto-app
```

Open <http://127.0.0.1:8100>.

### 4.3 Use the AI Assistant (Optional)

AI chat depends on `dsh-java` (default `http://127.0.0.1:8090`) for the Agent runtime, and requires the plugin from this repo to be installed (see 4.4).

### 4.4 Install the Harness Plugin (3 tools)

Start `dsh-java` (port 8090) first:

```bash
JAR="$PWD/dsh-onto-plugin/target/dsh-onto-plugin-0.2.2.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-onto-plugin\",\"displayName\":\"DSH Onto Plugin\",\"pluginVersion\":\"0.2.2\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-onto-plugin-0.2.2.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-onto-plugin"}'
```

| Tool | Description |
| --- | --- |
| `plugin__dsh-onto-plugin__workspace_overview` | Workspace overview |
| `plugin__dsh-onto-plugin__knowledge_aggregate` | Knowledge aggregation |
| `plugin__dsh-onto-plugin__director_overview` | Director decision overview |

> The 12 additional business tools for the four-domain business are provided by the companion project `dsh-ontolab-siis`; see [dsh-ontolab-siis](https://github.com/linlannet/dsh-ontolab-siis) for installation.

## 5. Configuration

| Property | Env Var | Default | Description |
| --- | --- | --- | --- |
| `server.port` | — | `8100` | Aggregation app port |
| `spring.datasource.url` | `DB_HOST` / `DB_PORT` / `DB_NAME` | `127.0.0.1:3306/dsh_java_ontodb` | Database connection |
| `spring.datasource.username` | `DB_USER` | `root` | Database username |
| `spring.datasource.password` | `DB_PASSWORD` | `root` | Database password |
| `dsh.ontology.data-directory` | — | `~/.dsh-java-ontology` | Ontology config storage directory |
| `dsh.ontology.query-timeout-seconds` | — | `15` | Query execution timeout |
| `dsh.ontology.max-results` | — | `100` | Max query result count |
| `dsh.ontology.harness.base-url` | `DSH_HARNESS_BASE_URL` | `http://127.0.0.1:8090` | Harness URL |
| `dsh.ontology.jwt.secret` | `JWT_SECRET` | (built-in default) | JWT signing secret; must be overridden in production |

> 0.3.0 plan: verification switches to **linlan-emis** `issuer` + JWKS (`dsh.auth.issuer` / `dsh.auth.jwks-uri` / `dsh.auth.audience`); `dsh.ontology.jwt.secret` is kept only for the transition. See [docs/0.3.0/auth-tenant-design.md](docs/0.2.3/auth-tenant-design.md).

## 6. Module Dependencies

```text
dsh-onto-base  ←── dsh-onto-flow  ←── dsh-onto-app
       ↑
       └── dsh-onto-plugin
dsh-onto-qa / dsh-onto-viz / dsh-onto-metric ←── dsh-onto-app
```

- **base**: No business module dependencies; only Spring Boot infrastructure
- **flow**: Depends on base; contains auth + workspace + global aggregation
- **qa**: Depends on base + flow; reporting and Q&A orchestration
- **viz**: Frontend static asset pack
- **metric**: Depends on base; metric modeling and dependency tree
- **app**: Aggregates base + flow + qa + viz + metric; independently deployable platform app
- **plugin**: Depends on base; outputs the 3-tool platform plugin JAR

## 7. Companion Project

| Project | Location | Description |
| --- | --- | --- |
| `dsh-ontolab-siis` | `dsh-ontolab-siis/` (standalone directory + parent pom; ignored by this repo's `.gitignore`) | Four-domain business (ontology/trace/display/analyze) + 38 `gwy_*` tables + 12 business tools, port 8101; see [dsh-ontolab-siis](https://github.com/linlannet/dsh-ontolab-siis) |
| `linlan-emis` (BuddyOne) | `/Users/mac/Workspace/linlannet/linlan-emis` (sibling repo) | Unified identity/tenant platform: Java `backend` + Python `py-api`; this platform acts as its Resource Server (0.3.0 plan) |

Dependency: `dsh-ontolab-siis` depends on this repo's `dsh-onto-base` / `flow` / `qa` / `viz` / `metric` 0.2.2 and must sit under the same parent directory when building. See its README for details.

## 8. Directory Layout

```text
dsh-java-ontology/
├── dsh-onto-base/ / dsh-onto-flow/ / dsh-onto-qa/
├── dsh-onto-viz/ / dsh-onto-metric/ / dsh-onto-app/ / dsh-onto-plugin/
├── agents/                  3 Agent configs
├── skills/                  4 Skill packs
├── docs/
│   ├── 0.2.1/               Platform design, specs, DB design, architecture comparison, plugin split, hotspot analysis
│   ├── 0.2.2/               Design review, static/dynamic ontology plans
│   ├── 0.3.0/               Identity/tenant integration design (linlan-emis = BuddyOne, this platform = RS)
│   ├── contract/            Permission contract (permissions.yaml)
│   └── dev-ops/             Ontology schema (dsh_java_ontodb.sql)
├── AGENTS.md                AI agent conventions
├── llms.txt                 AI agent quick index
└── CHANGELOG.md             Version history
```

## 9. Security Boundary

- **AI pipeline is tool-based**: This repo registers 3 atomic tools; Agents access platform capabilities via tool calls.
- Queries default to a 15-second timeout and max 100 results.
- JWT authentication + three-level role permissions (director/researcher/guest) + multi-tenant data isolation (transition); from 0.3.0 tokens are issued by linlan-emis and this platform no longer self-signs.
- Production deployment must change the default JWT secret and database password, and configure CORS/CSRF policies.

## 10. Known Items

- The root pom's `<modules>` contains only the seven platform modules; `dsh-ontolab-siis` uses its own parent pom, so building the platform does not require the SIIS sources.
- `dsh.app.version` and the `version` in `plugin.yaml` are both aligned to `0.2.2` with the pom.
- `V1.0.0__init_schema.sql` still keeps 36 `gwy_*` split leftovers.
- The platform plugin ships 3 tools; the 12 business tools live in `dsh-ontolab-siis`.

## 11. Testing

```bash
mvn -pl dsh-onto-base,dsh-onto-app -am test
```

16 test classes (6 in `dsh-onto-base`, 10 in `dsh-onto-app`) with 110 test methods total, covering unit tests, model engine, integration tests, and end-to-end tests.

## 12. Documentation Index

| Document | Content |
| --- | --- |
| [CHANGELOG.md](./CHANGELOG.md) | Version history, index and AI retrieval hints |
| [AGENTS.md](./AGENTS.md) | Layout, commands and constraints for AI agents |
| [llms.txt](./llms.txt) | Machine-readable summary and command index |
| [docs/0.2.1/](./docs/0.2.1) | Platform design & spec, `ONTO_DB_DESIGN`, architecture comparison, plugin split, hotspot analysis |
| [docs/0.2.2/](./docs/0.2.2) | Design review, static/dynamic ontology plans |
| [docs/0.3.0/auth-tenant-design.md](docs/0.2.3/auth-tenant-design.md) | Identity/tenant integration: linlan-emis (BuddyOne) as IdP, this platform as Resource Server |
| [docs/contract/permissions.yaml](./docs/contract/permissions.yaml) | Permission contract |
| [docs/dev-ops/dsh_java_ontodb.sql](./docs/dev-ops/dsh_java_ontodb.sql) | Ontology schema |
| [dsh-ontolab-siis](https://github.com/linlannet/dsh-ontolab-siis) | Companion SIIS project |

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/xxxx`
3. Run `mvn -pl dsh-onto-base,dsh-onto-app -am test` before committing
4. Open a Pull Request

## License

[MIT © 2026 shuxiaobo](./LICENSE)