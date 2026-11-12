#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""静态本体模型快照统计（一次性脚本，不参与 Maven 构建）。

统计口径与 Java 引擎一致：
- 聚合根数：顶层 aggregates / entities 列表长度（不递归）
- 字段数：attributes + 嵌套 entities 递归合计
- 枚举数：type == Enum 的字段，递归合计

用法：
  python3 snapshot.py            # 输出两层统计
  python3 snapshot.py --json     # 输出 JSON
  python3 snapshot.py --check    # 与冻结基线比对，不一致时退出码 1
"""

import argparse
import glob
import json
import os
import sys

try:
    import yaml
except ImportError:
    sys.exit("需要 PyYAML：pip3 install pyyaml")

BASELINE = {
    "base": {"aggregates": 25, "attributes": 200, "enums": 16},
    "siis": {"aggregates": 27, "attributes": 292, "enums": 14},
}


def walk(aggregate):
    attributes = aggregate.get("attributes") or []
    count = len(attributes)
    enums = sum(1 for item in attributes if str(item.get("type", "")).lower() == "enum")
    for child in aggregate.get("entities") or []:
        child_count, child_enums = walk(child)
        count += child_count
        enums += child_enums
    return count, enums


def stat_layer(directory):
    files = sorted(glob.glob(os.path.join(directory, "*.yaml")))
    total = {"aggregates": 0, "attributes": 0, "enums": 0}
    per_file = {}
    for path in files:
        with open(path, encoding="utf-8") as handle:
            document = yaml.safe_load(handle) or {}
        aggregates = document.get("entities") or document.get("aggregates") or []
        file_stat = {"aggregates": len(aggregates), "attributes": 0, "enums": 0}
        for aggregate in aggregates:
            count, enums = walk(aggregate)
            file_stat["attributes"] += count
            file_stat["enums"] += enums
        per_file[os.path.basename(path)] = file_stat
        for key in total:
            total[key] += file_stat[key]
    return {"summary": total, "files": per_file}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=os.path.dirname(os.path.abspath(__file__)))
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    result = {
        "base": stat_layer(os.path.join(args.root, "current", "base")),
        "siis": stat_layer(os.path.join(args.root, "current", "siis")),
    }
    result["total"] = {
        key: result["base"]["summary"][key] + result["siis"]["summary"][key]
        for key in ("aggregates", "attributes", "enums")
    }

    if args.json:
        print(json.dumps(result, ensure_ascii=False, indent=2))
    else:
        for layer in ("base", "siis"):
            summary = result[layer]["summary"]
            print(f"{layer:5s} aggregates={summary['aggregates']} "
                  f"attributes={summary['attributes']} enums={summary['enums']}")
            for name, item in result[layer]["files"].items():
                print(f"  - {name}: {item['aggregates']}/{item['attributes']}/{item['enums']}")
        total = result["total"]
        print(f"total aggregates={total['aggregates']} "
              f"attributes={total['attributes']} enums={total['enums']}")

    if args.check:
        failed = False
        for layer, expected in BASELINE.items():
            for key, value in expected.items():
                actual = result[layer]["summary"][key]
                if actual != value:
                    print(f"[FAIL] {layer}.{key}: expected {value}, actual {actual}",
                          file=sys.stderr)
                    failed = True
        if failed:
            sys.exit(1)
        print("[OK] 快照与冻结基线一致", file=sys.stderr)


if __name__ == "__main__":
    main()
