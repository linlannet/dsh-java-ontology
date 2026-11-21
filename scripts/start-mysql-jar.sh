#!/usr/bin/env bash
# 启动 dsh-java-ontology 平台（MySQL 模式，需本地/可达 MySQL 实例）。
#
# 对应：默认 application.yml（Flyway 自动迁移）。
#   库名   dsh_java_ontodb
#   用户   取 application.yml 的 DB_USER（默认 lldba），可用 DB_USER 覆盖
#   密码   取 application.yml 的 DB_PASSWORD，可用 DB_PASSWORD 覆盖
#   URL    jdbc:mysql://127.0.0.1:3306/dsh_java_ontodb
#
# 前置条件：
#   1) MySQL 已启动（本机 3306，或设置 DB_HOST / DB_PORT）
#   2) 数据库用户具备建库/建表权限（url 含 createDatabaseIfNotExist=true）
#
# 用法：
#   ./start-mysql-jar.sh                  直接运行已构建 jar
#   ./start-mysql-jar.sh --build          先构建再运行
#   DB_PASSWORD=xxx ./start-mysql-jar.sh  覆盖数据库密码
#   JAVA_OPTS="-Xmx1g" ./start-mysql-jar.sh
#   其余位置参数作为 Spring Boot 程序参数，如 --server.port=8101
#
# 启动后：
#   平台地址    http://127.0.0.1:8100
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
  echo "未找到可运行 jar，请先执行 ./scripts/start-mysql-jar.sh --build" >&2
  exit 1
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-dsh_java_ontodb}"

# 账号密码默认取 application.yml（DB_USER / DB_PASSWORD），仅在显式设置环境变量时覆盖，
# 避免这里写死的默认值（曾为 root/root）优先于配置文件导致 Access denied。
DB_ARGS=(--spring.datasource.url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true")
[[ -n "${DB_USER:-}" ]] && DB_ARGS+=(--spring.datasource.username="${DB_USER}")
[[ -n "${DB_PASSWORD:-}" ]] && DB_ARGS+=(--spring.datasource.password="${DB_PASSWORD}")

if [[ ${#REST[@]} -gt 0 ]]; then
  set -- "${REST[@]}"
else
  set --
fi

JAVA_BIN="${JAVA_HOME:+${JAVA_HOME}/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"

echo "== 启动 dsh-java-ontology (MySQL) jar: ${JAR} =="
echo "   平台 http://127.0.0.1:8100 | ${DB_USER:-application.yml 默认账号}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "   Ctrl+C 停止"
exec "${JAVA_BIN}" ${JAVA_OPTS:-} -jar "${JAR}" "${DB_ARGS[@]}" "$@"
