#!/usr/bin/env bash
# 把 dsh-onto-plugin 安装并激活到 dsh-java Harness（默认 http://127.0.0.1:8090）。
#
# 前置：
#   1) Harness 8090 已启动（dsh-java）
#   2) ontology 平台 8100 已启动（插件工具执行时会回调平台 API）
#
# 用法：
#   ./install-plugin.sh
#   HARNESS_BASE_URL=http://host:8090 ./install-plugin.sh
#   PLUGIN_JAR=/path/to/dsh-onto-plugin-0.2.2.jar ./install-plugin.sh
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

HARNESS="${HARNESS_BASE_URL:-http://127.0.0.1:8090}"
PLUGIN_ID="dsh-onto-plugin"
VERSION="${PLUGIN_VERSION:-0.2.2}"
PLUGIN_JAR="${PLUGIN_JAR:-$(ls "${REPO_DIR}/dsh-onto-plugin/target/dsh-onto-plugin-*-exec.jar" 2>/dev/null | sort | tail -1 || true)}"
[[ -n "${PLUGIN_JAR}" && -f "${PLUGIN_JAR}" ]] || PLUGIN_JAR="${REPO_DIR}/dsh-onto-plugin.jar"
if [[ ! -f "${PLUGIN_JAR}" ]]; then
  echo "未找到插件 jar，请先执行 ./package.sh 或 mvn -pl dsh-onto-plugin -am package -DskipTests" >&2
  exit 1
fi

echo "== 检查 Harness ${HARNESS} 是否可达 =="
if ! curl -sS -m 5 "${HARNESS}/actuator/health" >/dev/null 2>&1; then
  echo "警告：${HARNESS}/actuator/health 不可达，继续尝试安装（若失败请确认 Harness 已启动）" >&2
fi

echo "== 安装插件 ${PLUGIN_ID} ${VERSION} =="
echo "   jar: ${PLUGIN_JAR}"
INSTALL_BODY=$(printf '{"pluginId":"%s","displayName":"DSH Onto Plugin","pluginVersion":"%s","runtimeType":"JAVA_NATIVE","sourcePath":"%s","entrypoint":"%s"}' \
  "${PLUGIN_ID}" "${VERSION}" "${PLUGIN_JAR}" "dsh-onto-plugin-${VERSION}.jar")
resp=$(curl -sS -X POST "${HARNESS}/api/harness/plugins/install" \
  -H 'Content-Type: application/json' -d "${INSTALL_BODY}")
echo "  install -> ${resp}"

echo "== 激活插件 =="
resp=$(curl -sS -X POST "${HARNESS}/api/harness/plugins/activate" \
  -H 'Content-Type: application/json' -d "{\"pluginId\":\"${PLUGIN_ID}\"}")
echo "  activate -> ${resp}"

echo "== 插件状态 =="
curl -sS "${HARNESS}/api/harness/plugins/${PLUGIN_ID}/status"; echo

echo ""
echo "完成。3 个工具（在 Agent 会话中验证）："
echo "  plugin__dsh-onto-plugin__workspace_overview"
echo "  plugin__dsh-onto-plugin__knowledge_aggregate"
echo "  plugin__dsh-onto-plugin__director_overview"
