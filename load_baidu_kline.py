#!/usr/bin/env python3
"""
Load BTC/CNY kline data from Baidu page/API into Redis `_min_bars_`.

Usage examples:
  python load_baidu_kline.py --page-url "https://finance.baidu.com/foreign/global-BTCCNY"
  python load_baidu_kline.py --api-url "https://example.com/kline-api"
"""

import argparse
import json
import re
import subprocess
import sys
from typing import Iterable, List

import requests


def normalize_bar(bar: List[float]) -> List[float]:
    # Expected: [timestamp, open, high, low, close, volume]
    ts = int(float(bar[0]))
    o = float(bar[1])
    h = float(bar[2])
    l = float(bar[3])
    c = float(bar[4])
    v = float(bar[5]) if len(bar) > 5 else 0.0
    return [ts, round(o, 2), round(h, 2), round(l, 2), round(c, 2), round(v, 4)]


def parse_bars_from_page(html: str) -> List[List[float]]:
    # Try to capture JSON-like 6-field kline tuples in scripts.
    # Example match: [1710000000000,70000,70100,69900,70080,12.3]
    pattern = re.compile(
        r"\[(\d{10,13}),\s*(-?\d+(?:\.\d+)?),\s*(-?\d+(?:\.\d+)?),\s*(-?\d+(?:\.\d+)?),\s*(-?\d+(?:\.\d+)?),\s*(-?\d+(?:\.\d+)?)\]"
    )
    bars = []
    for m in pattern.finditer(html):
        raw = [m.group(i) for i in range(1, 7)]
        bars.append(normalize_bar(raw))
    return bars


def parse_bars_from_api(payload) -> List[List[float]]:
    # Accept either raw list of bars or dict with common keys.
    if isinstance(payload, list):
        return [normalize_bar(x) for x in payload if isinstance(x, list) and len(x) >= 5]
    if isinstance(payload, dict):
        for key in ("data", "klines", "kline", "bars", "result"):
            val = payload.get(key)
            if isinstance(val, list):
                return [normalize_bar(x) for x in val if isinstance(x, list) and len(x) >= 5]
    return []


def redis_cli(*args: str) -> str:
    cmd = ["redis-cli", "-h", "127.0.0.1", "-p", "6379", *args]
    out = subprocess.check_output(cmd, text=True, encoding="utf-8", errors="ignore").strip()
    return out


def write_bars_to_redis(bars: Iterable[List[float]]) -> int:
    redis_cli("DEL", "_min_bars_")
    count = 0
    for bar in bars:
        ts = str(int(bar[0]))
        member = json.dumps(bar, separators=(",", ":"))
        redis_cli("ZADD", "_min_bars_", ts, member)
        count += 1
    return count


def publish_last_bar(bar: List[float]) -> None:
    payload = json.dumps(
        {"type": "bar", "resolution": "MIN", "sequenceId": 2000000, "data": bar},
        separators=(",", ":"),
    )
    redis_cli("PUBLISH", "notification", payload)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--page-url", default="https://finance.baidu.com/foreign/global-BTCCNY")
    parser.add_argument("--api-url", default="")
    parser.add_argument("--min-bars", type=int, default=120, help="Require at least N bars to accept page parsing")
    args = parser.parse_args()

    bars: List[List[float]] = []

    if args.api_url:
        r = requests.get(args.api_url, timeout=20)
        r.raise_for_status()
        bars = parse_bars_from_api(r.json())
        print(f"[api] parsed bars: {len(bars)}")

    if not bars:
        r = requests.get(args.page_url, timeout=20)
        r.raise_for_status()
        bars = parse_bars_from_page(r.text)
        print(f"[page] parsed bars: {len(bars)}")

    # De-duplicate by timestamp and keep sorted.
    uniq = {}
    for b in bars:
        uniq[int(b[0])] = b
    bars = [uniq[k] for k in sorted(uniq.keys())]

    if len(bars) < args.min_bars:
        print(f"Not enough bars parsed ({len(bars)}). You likely need a real --api-url endpoint.")
        return 2

    n = write_bars_to_redis(bars)
    publish_last_bar(bars[-1])
    print(f"Loaded {n} bars into _min_bars_ and published latest MIN bar.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

