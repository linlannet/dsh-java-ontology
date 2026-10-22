# dsh-java-ontology

An ontology knowledge engineering platform with a **base + flow + domain capability packs** three-layer architecture: static ontology definitions, rules, and shared infrastructure at the base layer (`dsh-onto-base`); dynamic ontology, authentication, and workspace orchestration at the flow layer (`dsh-onto-flow`); and composable domain capability packs at the top layer (Q&A `qa`, visualization `viz`, metrics `metric`). `dsh-onto-app` aggregates them into an independently deployable platform app (port 8100), while `dsh-onto-plugin` ships a `dsh-java` Java Native Plugin (3 atomic tools) that lets AI Agents safely query and search the ontology knowledge base. The repo also ships 3 Agent configs and 4 Skill packs.

## Features

- **Ontology Management**: Multi-ontology configuration with create, delete, and list views
- **AI Chat Assistant**: Natural language ontology queries with streaming output — thought process → tool calls → final conclusion
- **Concept Query**: Retrieve concept definitions, hierarchical relations, and entity properties by ontology ID and query expression
- **Fuzzy Search**: Quickly locate concepts and entities by keyword
- **Multi-Tenant Auth**: JWT + BCrypt authentication with director/researcher/guest three-level roles
- **Workflow Orchestration**: State machine transitions + task dependencies + parallel execution
- **Global Knowledge Aggregation**: Three-tier knowledge architecture (personal → common → global) + KG convergence
- **Q&A Reporting**: Report and Q&A orchestration across template/section/chart/session/task
- **Metric Modeling**: Business object/process/decision + atom/derived/composite metrics + dependency tree
- **Harness Plugin**: 3 atomic tools registered with dsh-java for Agent invocation

## Architecture

```text
dsh-java-ontology/
├── dsh-onto-base/                          Base: static ontology definitions + rules + logic
│   ├── src/main/java/net/linlan/dsh/common/        Common infrastructure (PageRequest/Result/BaseEntity/BaseRepository etc.)
│   ├── src/main/java/net/linlan/dsh/ontology/plugin/  Plugin base classes (AdminApiClient + BaseOntologyTool)
│   └── src/main/resources/ontology-models/          Seven-model YAML (m1~m7 + manifest)
├── dsh-onto-flow/                          Flow: dynamic ontology + workspace + multi-user auth
│   ├── src/main/java/net/linlan/dsh/sys/           Auth (JWT + BCrypt + role interceptor)
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
└── docs/                                   Design docs (0.2.1 specs / 0.2.2 designs / ADR / contracts / ops)
```

```text
Browser ──► Admin UI (8100) ──► Admin API ──► Onto DB (default `ontology_db`)
               │
               └─ AI Chat ──► dsh-java (8090)
                                 └─► dsh-onto-plugin (3 tools) ──► Platform API
```

### Table Naming Convention

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

## Requirements

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Optional: dsh-java (AI chat depends on it for the Agent runtime)

## Quick Start

### 1. Build

```bash
git clone git@github.com:linlannet/dsh-java-ontology.git
cd dsh-java-ontology
mvn package -DskipTests
```

### 2. Start the Aggregation App (Platform)

```bash
java -jar dsh-onto-app/target/dsh-onto-app-0.2.1-exec.jar
```

Or in dev mode:

```bash
mvn spring-boot:run -pl dsh-onto-app
```

Open <http://127.0.0.1:8100>.

### 3. Use the AI Assistant (Optional)

AI chat depends on `dsh-java` (default `http://127.0.0.1:8090`) for the Agent runtime, and requires the plugin from this repo to be installed (see below).

### 4. Install the Harness Plugin (3 tools)

Start `dsh-java` (port 8090) first:

```bash
JAR="$PWD/dsh-onto-plugin/target/dsh-onto-plugin-0.2.1.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-onto\",\"displayName\":\"DSH Onto Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-onto-plugin-0.2.1.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-onto"}'
```

| Tool | Description |
| --- | --- |
| `plugin__dsh-onto__workspace_overview` | Workspace overview |
| `plugin__dsh-onto__knowledge_aggregate` | Knowledge aggregation |
| `plugin__dsh-onto__director_overview` | Director decision overview |

> The 12 additional business tools for the four-domain business are provided by the companion project `dsh-ontolab-siis`; see [dsh-ontolab-siis/README.md](dsh-ontolab-siis/README.md) for installation.

## Configuration

| Property | Env Var | Default | Description |
| --- | --- | --- | --- |
| `server.port` | — | `8100` | Aggregation app port |
| `spring.datasource.url` | `DB_HOST` / `DB_PORT` / `DB_NAME` | `127.0.0.1:3306/ontology_db` | Database connection |
| `spring.datasource.username` | `DB_USER` | `root` | Database username |
| `spring.datasource.password` | `DB_PASSWORD` | `root` | Database password |
| `dsh.ontology.data-directory` | — | `~/.dsh-java-ontology` | Ontology config storage directory |
| `dsh.ontology.query-timeout-seconds` | — | `15` | Query execution timeout |
| `dsh.ontology.max-results` | — | `100` | Max query result count |
| `dsh.ontology.harness.base-url` | `DSH_HARNESS_BASE_URL` | `http://127.0.0.1:8090` | Harness URL |
| `dsh.ontology.jwt.secret` | `JWT_SECRET` | (built-in default) | JWT signing secret |

## Module Dependencies

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

## Companion Project

| Project | Location | Description |
| --- | --- | --- |
| `dsh-ontolab-siis` | `dsh-ontolab-siis/` (standalone directory with its own parent pom, not tracked in this repo) | Four-domain business (ontology/trace/display/analyze) + 38 `gwy_*` tables + 12 business tools, port 8101; see [dsh-ontolab-siis/README.md](dsh-ontolab-siis/README.md) |

## Security Boundary

- **AI pipeline is tool-based**: This repo registers 3 atomic tools; Agents access platform capabilities via tool calls.
- Queries default to 15-second timeout and max 100 results.
- JWT authentication + three-level role permissions (director/researcher/guest) + multi-tenant data isolation.
- Production deployment must change the default JWT secret and database password, and configure CORS/CSRF policies.

## Documentation

- `docs/0.2.1`: Platform design & spec, ONTO_DB_DESIGN, architecture comparison, plugin split plan, hotspot analysis spec & task list
- `docs/0.2.2`: Design review, static/dynamic ontology implementation plans, methodology & practice articles
- `docs/adr`: ADR-001~005 architecture decision records
- `docs/contract/permissions.yaml`: Permission contract
- `docs/dev-ops/dsh_java_ontodb.sql`: Ontology database schema

## Testing

```bash
mvn test
```

13 test classes (3 in `dsh-onto-base`, 10 in `dsh-onto-app`) with 74 test methods total, covering unit tests, integration tests, and end-to-end tests.
