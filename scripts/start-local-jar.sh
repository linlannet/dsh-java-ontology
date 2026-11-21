#!/usr/bin/env bash
# 启动 dsh-java-ontology 平台（本地 H2 内存库，零外部数据库依赖）。
#
# 对应：exec jar 内不含 H2（test scope），这里用 Spring Boot PropertiesLauncher
#       通过 -Dloader.path 注入 m2 中的 H2 驱动，以 MODE=MySQL 兼容 Flyway 迁移。
#
# 用法：
#   ./start-local-jar.sh                  直接运行已构建 jar（dsh-onto-app/target/*.jar）
#   ./start-local-jar.sh --build          先 mvn 构建（含 app + plugin）再运行
#   JAVA_OPTS="-Xmx1g" ./start-local-jar.sh   追加 JVM 参数
#   ./start-local-jar.sh --server.port=8101   其余参数透传给 Spring Boot
#
# 启动后：
#   平台地址    http://127.0.0.1:8100
#   H2 控制台   http://127.0.0.1:8100/h2-console  (JDBC: jdbc:h2:mem:ontology;MODE=MySQL)
#   安装插件    ./scripts/install-plugin.sh（需 Harness 8090 已启动）
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_DIR}"

BUILD=0
REST=()
for a in "$@"; do
  case "$a" in
    --build|-b) BUILD=1 ;;
    *) REST+=("$a") ;;
  esac
done

if [[ "${BUILD}" -eq 1 ]]; then
  echo "== 构建 dsh-java-ontology =="
  mvn -q -pl dsh-onto-app,dsh-onto-plugin -am package -DskipTests
fi

JAR="$(ls dsh-onto-app/target/dsh-onto-app-*-exec.jar 2>/dev/null | sort | tail -1 || true)"
[[ -z "${JAR}" ]] && JAR="${REPO_DIR}/dsh-onto-app.jar"
if [[ ! -f "${JAR}" ]]; then
  echo "未找到可运行 jar，请先执行 ./scripts/start-local-jar.sh --build" >&2
  exit 1
fi

H2_JAR="${H2_JAR:-$(find "${HOME}/.m2/repository/com/h2database/h2" -name 'h2-2.2.224.jar' -print 2>/dev/null | head -1)}"
if [[ -z "${H2_JAR}" || ! -f "${H2_JAR}" ]]; then
  echo "未找到 H2 驱动 jar，请执行 mvn dependency:go-offline 或设置 H2_JAR=<path>" >&2
  exit 1
fi

if [[ ${#REST[@]} -gt 0 ]]; then
  set -- "${REST[@]}"
else
  set --
fi

JAVA_BIN="${JAVA_HOME:+${JAVA_HOME}/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"

echo "== 启动 dsh-java-ontology (本地/H2) jar: ${JAR} =="
echo "   平台 http://127.0.0.1:8100 | H2 内存库 MODE=MySQL | H2: ${H2_JAR}"
echo "   Ctrl+C 停止"
exec "${JAVA_BIN}" ${JAVA_OPTS:-} \
  -Dloader.path="${H2_JAR}" \
  -cp "${JAR}" org.springframework.boot.loader.launch.PropertiesLauncher \
  --spring.datasource.url='jdbc:h2:mem:ontology;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE' \
  --spring.datasource.driver-class-name=org.h2.Driver \
  --spring.datasource.username=sa \
  --spring.datasource.password= \
  --spring.datasource.hikari.connection-test-query='SELECT 1' \
  "$@"
