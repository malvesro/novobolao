#!/usr/bin/env python3
"""
Script utilitário para baixar apenas as bandeiras que ainda não existem localmente.
"""

import re
import sys
from pathlib import Path

import requests

DATA_FILE = Path("Copa2026_Paises_Bandeiras_Completo.txt")
OUTPUT_DIR = Path("webapp/img/bandeiras")
TIMEOUT = 10

URL_PATTERN = re.compile(r"https?://\S+")


def parse_dataset():
    if not DATA_FILE.exists():
        print(f"Arquivo {DATA_FILE} não encontrado", file=sys.stderr)
        sys.exit(1)

    entries = []
    with DATA_FILE.open("r", encoding="utf-8") as handle:
        for raw_line in handle:
            line = raw_line.strip()
            if not line or line.startswith("PAÍS") or line.startswith("Fonte"):
                continue
            if "–" not in line:
                continue
            country, link = [part.strip() for part in line.split("–", 1)]
            match = URL_PATTERN.search(link)
            if not match:
                print(f"Aviso: linha sem URL reconhecida: {line}", file=sys.stderr)
                continue
            url = match.group(0)
            filename = url.split("/")[-1]
            if not filename.lower().endswith(".png"):
                print(f"Aviso: ignorando {url} (não é PNG)", file=sys.stderr)
                continue
            iso_code = Path(filename).stem.lower()
            entries.append((country, iso_code, url))
    return entries


def ensure_output_dir():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)


def download_missing_flags(entries):
    missing = [(country, iso, url) for country, iso, url in entries if not (OUTPUT_DIR / f"{iso}.png").exists()]

    if not missing:
        print("Nenhuma bandeira faltante localizada.")
        return

    for country, iso_code, url in missing:
        print(f"Baixando {country} ({iso_code})...")
        try:
            response = requests.get(url, timeout=TIMEOUT)
            response.raise_for_status()
        except requests.RequestException as exc:
            print(f"Falha ao baixar {country}: {exc}", file=sys.stderr)
            continue
        (OUTPUT_DIR / f"{iso_code}.png").write_bytes(response.content)
    print("Concluído.")


def main():
    entries = parse_dataset()
    ensure_output_dir()
    download_missing_flags(entries)


if __name__ == "__main__":
    main()
