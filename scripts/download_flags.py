#!/usr/bin/env python3
import csv
import os
import re
import sys
import unicodedata
from pathlib import Path

import requests

DATA_FILE = Path("Copa2026_Paises_Bandeiras_Completo.txt")
OUTPUT_DIR = Path("webapp/img/bandeiras")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

if not DATA_FILE.exists():
    print(f"Arquivo {DATA_FILE} não encontrado", file=sys.stderr)
    sys.exit(1)

url_pattern = re.compile(r"https?://\S+")

mapping_lines = []

with DATA_FILE.open("r", encoding="utf-8") as f:
    for raw_line in f:
        line = raw_line.strip()
        if not line or line.startswith("PAÍS") or line.startswith("Fonte"):
            continue
        if "–" not in line:
            continue
        country, link = [part.strip() for part in line.split("–", 1)]
        match = url_pattern.search(link)
        if not match:
            print(f"Aviso: linha sem URL reconhecida: {line}")
            continue
        url = match.group(0)
        # Deduzir código do final da URL
        filename = url.split("/")[-1]
        if not filename.lower().endswith(".png"):
            print(f"Pulando {url} (não termina com .png)")
            continue
        iso_code = Path(filename).stem.lower()
        # Normalizar nome do país para ISO
        normalized_country = unicodedata.normalize('NFD', country).encode('ascii', 'ignore').decode('ascii')
        normalized_country = normalized_country.lower()
        normalized_country = normalized_country.replace("'", "")
        normalized_country = normalized_country.replace("-", " ")
        normalized_country = re.sub(r"\s+", " ", normalized_country).strip()

        output_path = OUTPUT_DIR / f"{iso_code}.png"
        if output_path.exists():
            print(f"Arquivo {output_path} já existe, pulando download")
        else:
            print(f"Baixando {country} ({iso_code})...")
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            output_path.write_bytes(response.content)
        mapping_lines.append((normalized_country, iso_code))

# Gerar mapping atualizado
with Path("src/main/resources/flags.properties").open("w", encoding="utf-8") as mapping_file:
    for country, iso_code in sorted(set(mapping_lines)):
        mapping_file.write(f"{country}={iso_code}\n")

print("Concluído.")
