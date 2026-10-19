# dsh-java-ontology

An ontology knowledge engineering platform with a **base + flow + SIIS** three-layer architecture: static ontology definitions and rule logic at the base layer, dynamic ontology and workspace orchestration at the flow layer, and SIIS traditional business domains (ontology/trace/display/analyze) at the top layer. Ships with two `dsh-java` Java Native Plugins that let AI Agents safely query and search the ontology knowledge base.

## Features

- **Ontology Management**: Multi-ontology configuration with create, delete, and list views
- **AI Chat Assistant**: Natural language ontology queries with streaming output — thought process → tool calls → final conclusion
- **Concept Query**: Retrieve concept definitions, hierarchical relations, and entity properties by ontology ID and query expression
- **Fuzzy Search**: Quickly locate concepts and entities by keyword
- **Multi-Tenant Auth**: JWT + BCrypt authentication with director/researcher/guest three-level roles
- **Workflow Orchestration**: State machine transitions + task dependencies + parallel execution
- **Crawl Engine**: Rate limiting + proxy rotation + retry + scheduled crawling
- **Multilingual NLP**: Chinese/English/French/Arabic strategies + ontology matching + KG normalization
- **Global Knowledge Aggregation**: Three-tier knowledge architecture (personal → common → global) + KG convergence
- **Harness Plugins**: 15 atomic tools registered with dsh-java for Agent invocation

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
├── dsh-onto-visulization/                  Visualization: frontend static assets (HTML/CSS/JS)
├── dsh-onto-app/                           Aggregation app: base + flow + visulization (port 8100)
├── dsh-onto-plugin/                        Plugin output: 3 platform tools (workspace/knowledge/director)
└── dsh-ontoext-siis/                       SIIS subproject
    ├── dsh-ontoext-siis-app/               SIIS app: four-domain business + full schema (port 8101)
    └── dsh-ontoext-siis-plugin/            SIIS plugin output: 12 business tools
```

```text
Browser ──► Admin UI (8100) ──► Admin API ──► Ontology Store
               │
               └─ AI Chat ──► dsh-java (8090)
                                 ├─► dsh-onto-plugin (3 tools) ──► Platform API
                                 └─► dsh-ontoext-siis-plugin (12 tools) ──► SIIS API
```

### Table Naming Convention

| Prefix | Layer | Module | Table Count |
| --- | --- | --- | --- |
| `onto_sys_` | Auth & permissions | flow | 1 |
| `onto_common_` | Personal knowledge precipitation | flow | 4 |
| `onto_global_` | Global knowledge aggregation | flow | 5 |
| `gwy_ontology_` | Ontology library | SIIS | 14 |
| `gwy_track_` | Tracking & collection | SIIS | 11 |
| `gwy_analyze_` | Analysis & alerts | SIIS | 7 |
| `gwy_display_` | Display & presentation | SIIS | 10 |

52 tables total + 2 views (`v_unified_monitor`, `v_unified_kg`).

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

### 3. Start the SIIS App (Four-Domain Business)

```bash
java -jar dsh-ontoext-siis/dsh-ontoext-siis-app/target/dsh-ontoext-siis-app-0.2.1-exec.jar
```

Or in dev mode:

```bash
mvn spring-boot:run -pl dsh-ontoext-siis/dsh-ontoext-siis-app
```

Open <http://127.0.0.1:8101>.

### 4. Use the AI Assistant (Optional)

AI chat depends on `dsh-java` (default `http://127.0.0.1:8090`) for the Agent runtime, and requires the plugins from this repo to be installed (see below).

## Install Harness Plugins

Start `dsh-java` (port 8090) first, then install the platform plugin and SIIS plugin separately:

### Platform Plugin (3 tools)

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

### SIIS Plugin (12 tools)

```bash
JAR="$PWD/dsh-ontoext-siis/dsh-ontoext-siis-plugin/target/dsh-ontoext-siis-plugin-0.2.1.jar"

curl -X POST http://localhost:8090/api/harness/plugins/install \
  -H 'Content-Type: application/json' \
  -d "{\"pluginId\":\"dsh-ontoext-siis\",\"displayName\":\"DSH Ontoext SIIS Plugin\",\"pluginVersion\":\"0.2.1\",\"runtimeType\":\"JAVA_NATIVE\",\"sourcePath\":\"$JAR\",\"entrypoint\":\"dsh-ontoext-siis-plugin-0.2.1.jar\"}"

curl -X POST http://localhost:8090/api/harness/plugins/activate \
  -H 'Content-Type: application/json' \
  -d '{"pluginId":"dsh-ontoext-siis"}'
```

| Tool | Description |
| --- | --- |
| `plugin__dsh-ontoext-siis__ontology_list` | List ontology versions |
| `plugin__dsh-ontoext-siis__ontology_query` | Query concept definitions and hierarchy |
| `plugin__dsh-ontoext-siis__ontology_search` | Fuzzy search concepts |
| `plugin__dsh-ontoext-siis__kg_query` | Knowledge graph query |
| `plugin__dsh-ontoext-siis__kg_search` | Knowledge graph search |
| `plugin__dsh-ontoext-siis__relation_analysis` | Bilateral relation analysis |
| `plugin__dsh-ontoext-siis__alert_query` | Alert query |
| `plugin__dsh-ontoext-siis__topic_etc` | Topic management |
| `plugin__dsh-ontoext-siis__topic_create` | Create topic |
| `plugin__dsh-ontoext-siis__crawl_execute` | Execute crawl |
| `plugin__dsh-ontoext-siis__nlp_extract` | NLP entity extraction |
| `plugin__dsh-ontoext-siis__report_generate` | Report generation |

## Configuration

| Property | Env Var | Default | Description |
| --- | --- | --- | --- |
| `server.port` | — | `8100` / `8101` | Aggregation app / SIIS app port |
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
       ↑                  ↑
       └── dsh-onto-plugin │
                           └── dsh-ontoext-siis-app
                                   └── dsh-ontoext-siis-plugin
dsh-onto-visulization ←── dsh-onto-app
```

- **base**: No business module dependencies; only Spring Boot infrastructure
- **flow**: Depends on base; contains auth + workspace + global aggregation
- **app**: Aggregates base + flow + visulization; independently deployable platform app
- **SIIS app**: Depends on base + flow; contains four-domain business + full schema
- **plugin**: Depends on base; outputs platform tool JAR
- **SIIS plugin**: Depends on base; outputs business tool JAR

## Security Boundary

- **AI pipeline is tool-based**: Plugins register 15 atomic tools; Agents access business capabilities via tool calls.
- Queries default to 15-second timeout and max 100 results.
- JWT authentication + three-level role permissions (director/researcher/guest) + multi-tenant data isolation.
- Production deployment must change the default JWT secret and database password, and configure CORS/CSRF policies.

## Testing

```bash
mvn test
```

7 modules with 115 tests total, covering unit tests, integration tests, and end-to-end tests.
