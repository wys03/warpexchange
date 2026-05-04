#!/usr/bin/env python3
import re
import urllib.parse
import requests

PAGE = "https://finance.baidu.com/foreign/global-BTCCNY"

def main():
    html = requests.get(PAGE, timeout=20).text
    srcs = re.findall(r"<script[^>]+src=[\"']([^\"']+)[\"']", html, re.I)
    urls = [urllib.parse.urljoin(PAGE, s) for s in srcs]
    print(f"script_count={len(urls)}")
    candidates = set()
    for u in urls:
        try:
            js = requests.get(u, timeout=20).text
        except Exception:
            continue
        for m in re.findall(r"https?://[^\"'\\s)]+", js):
            low = m.lower()
            if any(k in low for k in ("kline", "foreign", "market", "finstock", "quote", "global-btccny", "api")):
                candidates.add(m)
        for m in re.findall(r"/[^\"'\\s)]+", js):
            low = m.lower()
            if any(k in low for k in ("kline", "foreign", "market", "quote", "api")):
                candidates.add(urllib.parse.urljoin(PAGE, m))
    for c in sorted(candidates):
        print(c)

if __name__ == "__main__":
    main()

