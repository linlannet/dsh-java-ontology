#!/usr/bin/env bash
#
# 构建 dsh-java-ontology 并组装调试运行目录 dist/
#
# 产物：
#   dist/
#   ├── dsh-onto-app.jar          ← dsh-onto-app 的 exec fat jar（端口 8100）
#   ├── dsh-onto-plugin.jar       ← dsh-onto-plugin 瘦 jar（安装到 dsh-java 8090）
#   ├── start-local.sh            ← 本地 H2 模式启动平台（零外部依赖）
#   ├── start-mysql.sh            ← MySQL 模式启动平台
#   └── install-plugin.sh         ← 把插件安装+激活到 Harness 8090
#
# 仓库根目录同样保留 dsh-onto-app.jar / dsh-onto-plugin.jar 副本。
#
# 用法：
#   ./package.sh                  构建（默认 0.2.2，不跑测试）
#   ./package.sh --with-tests     构建并跑测试
#   ./package.sh -h               帮助
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
DIST_DIR="${REPO_DIR}/dist"
VERSION="${VERSION:-0.2.2}"
RUN_TESTS=0

usage() {
  cat <<'EOF'
Usage: ./package.sh [--with-tests]

Options:
  --with-tests   构建时执行测试（默认 -DskipTests）
  -h, --help     显示帮助

说明：在工程根执行 Maven 构建平台七包，收集 dsh-onto-app exec jar 与
      dsh-onto-plugin jar，组装 dist/（含启动与插件安装脚本）。
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --with-tests) RUN_TESTS=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
  esac
done

cd "${REPO_DIR}"
echo "== 构建 dsh-java-ontology ${VERSION}（含 app + plugin）=="
if [[ "${RUN_TESTS}" -eq 1 ]]; then
  mvn -pl dsh-onto-app,dsh-onto-plugin -am package
else
  mvn -q -pl dsh-onto-app,dsh-onto-plugin -am package -DskipTests
fi

APP_SRC="${REPO_DIR}/dsh-onto-app/target/dsh-onto-app-${VERSION}-exec.jar"
PLUGIN_SRC="${REPO_DIR}/dsh-onto-plugin/target/dsh-onto-plugin-${VERSION}.jar"
for f in "${APP_SRC}" "${PLUGIN_SRC}"; do
  if [[ ! -f "${f}" ]]; then
    echo "未找到构建产物：${f}" >&2
    exit 1
  fi
done

echo "== 组装 ${DIST_DIR} =="
rm -rf "${DIST_DIR}"
mkdir -p "${DIST_DIR}"
cp "${APP_SRC}" "${DIST_DIR}/dsh-onto-app.jar"
cp "${PLUGIN_SRC}" "${DIST_DIR}/dsh-onto-plugin.jar"
cp "${APP_SRC}" "${REPO_DIR}/dsh-onto-app.jar"
cp "${PLUGIN_SRC}" "${REPO_DIR}/dsh-onto-plugin.jar"
echo "  dsh-onto-app.jar (exec) + dsh-onto-plugin.jar  ->  dist/ 及仓库根"

# 本地 H2 启动：exec jar 内不含 H2（test scope），用 PropertiesLauncher + loader.path 注入 m2 中的 H2
cat > "${DIST_DIR}/start-local.sh" <<'EOF'
#!/usr/bin/env bash
# 本地零依赖启动 ontology 平台（H2 内存库，MODE=MySQL），端口 8100。
# 通过 PropertiesLauncher 注入 m2 中的 H2 驱动；Flyway 迁移会在启动时自动建表。
set -euo pipefail
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

JAR="${JAR_PATH:-./dsh-onto-app.jar}"
[[ -f "${JAR}" ]] || { echo "未找到 ${JAR}，请先执行 ./package.sh" >&2; exit 1; }

H2_JAR="${H2_JAR:-$(find "${HOME}/.m2/repository/com/h2database/h2" -name 'h2-2.2.224.jar' -print 2>/dev/null | head -1)}"
[[ -n "${H2_JAR}" && -f "${H2_JAR}" ]] || { echo "未找到 H2 驱动，请先 mvn dependency:go-offline 或设置 H2_JAR" >&2; exit 1; }

JAVA_BIN="${JAVA_HOME:+${JAVA_HOME}/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"
echo "启动 ontology 平台（H2 内存库）: http://127.0.0.1:8100"
echo "  H2 控制台: http://127.0.0.1:8100/h2-console  (JDBC: jdbc:h2:mem:ontology;MODE=MySQL)"
exec "${JAVA_BIN}" ${JAVA_OPTS:-} \
  -Dloader.path="${H2_JAR}" \
  -cp "${JAR}" org.springframework.boot.loader.launch.PropertiesLauncher \
  --spring.datasource.url='jdbc:h2:mem:ontology;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE' \
  --spring.datasource.driver-class-name=org.h2.Driver \
  --spring.datasource.username=sa \
  --spring.datasource.password= \
  --spring.datasource.hikari.connection-test-query='SELECT 1' \
  --spring.flyway.locations=classpath:db/migration \
  "$@"
EOF

# MySQL 模式启动
cat > "${DIST_DIR}/start-mysql.sh" <<'EOF'
#!/usr/bin/env bash
# MySQL 模式启动 ontology 平台，端口 8100。
# 数据库参数优先用环境变量：DB_HOST / DB_PORT / DB_NAME / DB_USER / DB_PASSWORD
set -euo pipefail
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

JAR="${JAR_PATH:-./dsh-onto-app.jar}"
[[ -f "${JAR}" ]] || { echo "未找到 ${JAR}，请先执行 ./package.sh" >&2; exit 1; }

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-dsh_java_ontodb}"

# 账号密码默认取 application.yml（DB_USER / DB_PASSWORD），仅在显式设置环境变量时覆盖，
# 避免这里写死的默认值（曾为 root/root）优先于配置文件导致 Access denied。
DB_ARGS=(--spring.datasource.url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true")
[[ -n "${DB_USER:-}" ]] && DB_ARGS+=(--spring.datasource.username="${DB_USER}")
[[ -n "${DB_PASSWORD:-}" ]] && DB_ARGS+=(--spring.datasource.password="${DB_PASSWORD}")

echo "启动 ontology 平台（MySQL）: http://127.0.0.1:8100"
echo "  数据源: ${DB_USER:-application.yml 默认账号}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
JAVA_BIN="${JAVA_HOME:+${JAVA_HOME}/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"
exec "${JAVA_BIN}" ${JAVA_OPTS:-} -jar "${JAR}" "${DB_ARGS[@]}" "$@"
EOF

# 插件安装 + 激活到 Harness 8090
cat > "${DIST_DIR}/install-plugin.sh" <<'EOF'
#!/usr/bin/env bash
# 把 dsh-onto-plugin 安装并激活到 dsh-java Harness（默认 http://127.0.0.1:8090）。
# 前置：Harness 8090 已启动；ontology 平台 8100 已启动（插件工具会回调平台 API）。
set -euo pipefail
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

HARNESS="${HARNESS_BASE_URL:-http://127.0.0.1:8090}"
PLUGIN_JAR="${PLUGIN_JAR:-$PWD/dsh-onto-plugin.jar}"
PLUGIN_ID="dsh-onto-plugin"
VERSION="${PLUGIN_VERSION:-0.2.2}"
[[ -f "${PLUGIN_JAR}" ]] || { echo "未找到插件 jar：${PLUGIN_JAR}" >&2; exit 1; }

echo "== 安装插件 ${PLUGIN_ID} ${VERSION} 到 ${HARNESS} =="
INSTALL_BODY=$(printf '{"pluginId":"%s","displayName":"DSH Onto Plugin","pluginVersion":"%s","runtimeType":"JAVA_NATIVE","sourcePath":"%s","entrypoint":"%s"}' \
  "${PLUGIN_ID}" "${VERSION}" "${PLUGIN_JAR}" "dsh-onto-plugin-${VERSION}.jar")
resp=$(curl -sS -X POST "${HARNESS}/api/harness/plugins/install" \
  -H 'Content-Type: application/json' -d "${INSTALL_BODY}")
echo "  install: ${resp}"

echo "== 激活插件 =="
resp=$(curl -sS -X POST "${HARNESS}/api/harness/plugins/activate" \
  -H 'Content-Type: application/json' -d "{\"pluginId\":\"${PLUGIN_ID}\"}")
echo "  activate: ${resp}"

echo "== 插件状态 =="
curl -sS "${HARNESS}/api/harness/plugins/${PLUGIN_ID}/status"; echo
echo ""
echo "完成。3 个工具（需 Agent 会话验证）："
echo "  plugin__dsh-onto-plugin__workspace_overview"
echo "  plugin__dsh-onto-plugin__knowledge_aggregate"
echo "  plugin__dsh-onto-plugin__director_overview"
EOF

chmod +x "${DIST_DIR}"/start-local.sh "${DIST_DIR}"/start-mysql.sh "${DIST_DIR}"/install-plugin.sh

echo ""
echo "完成：${DIST_DIR}"
echo "  本地调试：  ${DIST_DIR}/start-local.sh"
echo "  MySQL 调试：${DIST_DIR}/start-mysql.sh"
echo "  安装插件：  ${DIST_DIR}/install-plugin.sh（需先启动 Harness 8090）"
