#!/usr/bin/env python3
"""Build app/src/main/assets/index.html from web/planner.html.

web/planner.html is the same page published as the web artifact. For the app it gets:
  - a full HTML document skeleton (the web host normally adds this)
  - bundled fonts instead of Google Fonts, so the app works offline

Run from the project root after editing web/planner.html:
    python3 tools/build_page.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "web" / "planner.html"
OUT = ROOT / "app" / "src" / "main" / "assets" / "index.html"

FONTS = [
    ("Bricolage Grotesque", 600, "bricolage-grotesque-latin-600-normal.woff2"),
    ("Bricolage Grotesque", 700, "bricolage-grotesque-latin-700-normal.woff2"),
    ("IBM Plex Sans", 400, "ibm-plex-sans-latin-400-normal.woff2"),
    ("IBM Plex Sans", 500, "ibm-plex-sans-latin-500-normal.woff2"),
    ("IBM Plex Sans", 600, "ibm-plex-sans-latin-600-normal.woff2"),
    ("IBM Plex Mono", 400, "ibm-plex-mono-latin-400-normal.woff2"),
    ("IBM Plex Mono", 500, "ibm-plex-mono-latin-500-normal.woff2"),
]

font_css = "\n".join(
    f'@font-face{{font-family:"{fam}";font-style:normal;font-weight:{w};font-display:swap;'
    f'src:url("fonts/{file}") format("woff2")}}'
    for fam, w, file in FONTS
)

# Same base reset the web host applies around the page
BASE_CSS = """
:root{color-scheme:light dark}
body{margin:0;font-size:14px;-webkit-tap-highlight-color:transparent}
img{max-width:100%}
[hidden]{display:none!important}
"""

src = SRC.read_text(encoding="utf-8")

# Drop Google Fonts links and the web-only viewport-fit meta
src = re.sub(r'<link[^>]+fonts\.(googleapis|gstatic)\.com[^>]*>\s*', "", src)
src = re.sub(r'<meta name="claude:viewport-fit-sides"[^>]*>\s*', "", src)

title_match = re.search(r"<title>.*?</title>", src, re.S)
title = title_match.group(0) if title_match else "<title>Sourdough Planner</title>"
if title_match:
    src = src.replace(title, "", 1)

page = f"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
{title}
<style>
{font_css}
{BASE_CSS}
</style>
</head>
<body>
{src.strip()}
</body>
</html>
"""

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(page, encoding="utf-8")
print(f"Wrote {OUT.relative_to(ROOT)} ({len(page):,} bytes)")
