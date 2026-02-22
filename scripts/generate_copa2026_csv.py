#!/usr/bin/env python3
"""
Gera os arquivos `data/copa2026_tabela_brt.csv` e
`data/copa2026_tabela_brt_normalizado.csv` a partir do Excel
`Copa_do_Mundo_2026_Fase_de_Grupos_Completa_Brasilia.xlsx`.

O script utiliza apenas bibliotecas padrão (zipfile/xml) para manter
portabilidade no ambiente restrito.
"""

from __future__ import annotations

import csv
import sys
import zipfile
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Dict, Iterable, List, Tuple
from xml.etree import ElementTree as ET

DATA_DIR = Path("data")
EXCEL_FILE = DATA_DIR / "Copa_do_Mundo_2026_Fase_de_Grupos_Completa_Brasilia.xlsx"
RAW_CSV = DATA_DIR / "copa2026_tabela_brt.csv"
NORMALIZED_CSV = DATA_DIR / "copa2026_tabela_brt_normalizado.csv"

PLACEHOLDER_MARKERS = (
    "Repescagem",
    "Playoff",
    "Vencedor",
    "Winner",
    "3º",
    "3°",
    "3o",
    "Classificado",
)

STADIUM_COUNTRY = {
    "Estádio Azteca": "México",
    "Estadio Akron": "México",
    "Estadio BBVA": "México",
    "BMO Field": "Canadá",
    "BC Place": "Canadá",
    # Demais sedes ficam nos Estados Unidos
}

FUSO_PADRAO = "BRT"
FASE_GRUPOS = {
    "nome": "Grupos",
    "codigo": 11,
    "ordem": 1,
}


@dataclass
class MatchRow:
    data_str: str
    hora_str: str
    grupo: str
    mandante: str
    visitante: str
    cidade: str
    estadio: str

    @property
    def timestamp(self) -> datetime:
        return datetime.strptime(f"{self.data_str} {self.hora_str}", "%d/%m/%Y %H:%M")


def load_excel_matches(path: Path) -> List[MatchRow]:
    if not path.exists():
        raise FileNotFoundError(f"Planilha não encontrada: {path}")

    with zipfile.ZipFile(path) as workbook:
        strings = _read_shared_strings(workbook)
        rows = _read_sheet_rows(workbook, strings)

    matches: List[MatchRow] = []
    for row in rows[1:]:
        if len(row) < 7:
            continue
        match = MatchRow(
            data_str=row[0],
            hora_str=row[1],
            grupo=row[2],
            mandante=row[3],
            visitante=row[4],
            cidade=row[5],
            estadio=row[6],
        )
        matches.append(match)
    return matches


def _read_shared_strings(workbook: zipfile.ZipFile) -> List[str]:
    xml = ET.fromstring(workbook.read("xl/sharedStrings.xml"))
    ns = {"a": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
    values: List[str] = []
    for si in xml.findall("a:si", ns):
        text = "".join(node.text or "" for node in si.findall(".//a:t", ns))
        values.append(text)
    return values


def _read_sheet_rows(
    workbook: zipfile.ZipFile, shared_strings: Iterable[str]
) -> List[List[str]]:
    xml = ET.fromstring(workbook.read("xl/worksheets/sheet1.xml"))
    ns = {"a": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
    rows: List[List[str]] = []
    for row in xml.findall("a:sheetData/a:row", ns):
        values: List[str] = []
        for cell in row.findall("a:c", ns):
            value_node = cell.find("a:v", ns)
            if value_node is None:
                values.append("")
                continue
            content = value_node.text or ""
            if cell.get("t") == "s":
                content = shared_strings[int(content)]
            values.append(content)
        rows.append(values)
    return rows


def is_placeholder(name: str) -> bool:
    upper = name.upper()
    return any(marker.upper() in upper for marker in PLACEHOLDER_MARKERS)


def slot_value(name: str) -> str:
    return name if is_placeholder(name) else ""


def stadium_country(stadium: str) -> str:
    return STADIUM_COUNTRY.get(stadium, "EUA")


def compute_group_rounds(matches: Iterable[MatchRow]) -> Dict[str, Dict[datetime, int]]:
    grouped: Dict[str, List[MatchRow]] = defaultdict(list)
    for match in matches:
        grouped[match.grupo].append(match)

    group_rounds: Dict[str, Dict[datetime, int]] = {}
    for group, group_matches in grouped.items():
        ordered = sorted(group_matches, key=lambda m: m.timestamp)
        rounds: Dict[datetime, int] = {}
        for idx, match in enumerate(ordered):
            round_number = idx // 2 + 1  # dois jogos por rodada
            rounds[match.timestamp] = round_number
        group_rounds[group] = rounds
    return group_rounds


def write_raw_csv(matches: Iterable[MatchRow]) -> None:
    RAW_CSV.parent.mkdir(parents=True, exist_ok=True)
    with RAW_CSV.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(
            ["fase", "data", "hora_brt", "mandante", "visitante", "estadio", "cidade", "pais"]
        )
        for match in sorted(matches, key=lambda m: m.timestamp):
            writer.writerow(
                [
                    FASE_GRUPOS["nome"],
                    match.data_str,
                    match.hora_str,
                    match.mandante,
                    match.visitante,
                    match.estadio,
                    match.cidade,
                    stadium_country(match.estadio),
                ]
            )


def write_normalized_csv(matches: Iterable[MatchRow]) -> None:
    group_rounds = compute_group_rounds(matches)
    NORMALIZED_CSV.parent.mkdir(parents=True, exist_ok=True)
    fieldnames = [
        "fase",
        "fase_codigo",
        "fase_ordem",
        "grupo",
        "rodada",
        "data",
        "data_iso",
        "hora_brt",
        "mandante",
        "mandante_slot",
        "visitante",
        "visitante_slot",
        "estadio",
        "cidade",
        "pais",
        "fuso_lista",
    ]
    with NORMALIZED_CSV.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        for match in sorted(matches, key=lambda m: m.timestamp):
            ts = match.timestamp
            writer.writerow(
                {
                    "fase": FASE_GRUPOS["nome"],
                    "fase_codigo": FASE_GRUPOS["codigo"],
                    "fase_ordem": FASE_GRUPOS["ordem"],
                    "grupo": match.grupo,
                    "rodada": group_rounds[match.grupo][ts],
                    "data": match.data_str,
                    "data_iso": ts.strftime("%Y-%m-%d"),
                    "hora_brt": match.hora_str,
                    "mandante": match.mandante,
                    "mandante_slot": slot_value(match.mandante),
                    "visitante": match.visitante,
                    "visitante_slot": slot_value(match.visitante),
                    "estadio": match.estadio,
                    "cidade": match.cidade,
                    "pais": stadium_country(match.estadio),
                    "fuso_lista": FUSO_PADRAO,
                }
            )


def main() -> int:
    try:
        matches = load_excel_matches(EXCEL_FILE)
    except Exception as exc:  # pragma: no cover
        print(f"Erro ao processar planilha: {exc}", file=sys.stderr)
        return 1

    if not matches:
        print("Nenhum jogo encontrado na planilha.", file=sys.stderr)
        return 1

    write_raw_csv(matches)
    write_normalized_csv(matches)
    print(
        f"Foram processados {len(matches)} jogos. Arquivos gerados:\n"
        f"- {RAW_CSV}\n"
        f"- {NORMALIZED_CSV}"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())

