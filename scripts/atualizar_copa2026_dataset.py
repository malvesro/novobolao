#!/usr/bin/env python3
"""Gera script SQL e datasets derivados para a Copa 2026."""

from __future__ import annotations

import argparse
import csv
import json
import sys
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Sequence, Tuple

REQUIRED_COLUMNS = {
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
}

PLACEHOLDER_GROUP_DEFAULT = "Z"
TIME_SUFFIX = ":00"


@dataclass(frozen=True)
class Team:
    name: str
    group: str
    slot: str
    is_placeholder: bool


@dataclass(frozen=True)
class Match:
    jogo_id: int
    data_iso: str
    hora: str
    local: str
    equipe1: str
    equipe2: str
    fase_codigo: int


def parse_args(argv: Sequence[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--input",
        default="data/copa2026_tabela_brt_normalizado.csv",
        help="CSV normalizado de entrada",
    )
    parser.add_argument(
        "--placeholders",
        help="JSON com substituições de placeholders (opcional)",
    )
    parser.add_argument(
        "--output-sql",
        default="data/sql/03-copa-2026-data.sql",
        help="Arquivo SQL de saída",
    )
    parser.add_argument(
        "--output-csv",
        help="CSV final com placeholders aplicados (opcional)",
    )
    parser.add_argument(
        "--starting-eqp-id",
        type=int,
        default=100,
        help="ID inicial para novas equipes (default: 100)",
    )
    parser.add_argument(
        "--starting-jogo-id",
        type=int,
        default=1000,
        help="ID inicial para jogos (default: 1000)",
    )
    parser.add_argument(
        "--placeholder-group",
        default=PLACEHOLDER_GROUP_DEFAULT,
        help=f"Grupo sintético para placeholders sem grupo (default: {PLACEHOLDER_GROUP_DEFAULT})",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Não grava arquivos, imprime apenas resumo",
    )
    return parser.parse_args(argv)


def load_placeholder_map(path: Optional[str]) -> Dict[str, Dict[str, str]]:
    if not path:
        return {}
    data_path = Path(path)
    if not data_path.exists():
        raise FileNotFoundError(f"Arquivo de placeholders não encontrado: {path}")
    with data_path.open(encoding="utf-8") as handle:
        data = json.load(handle)
    if not isinstance(data, dict):
        raise ValueError("Arquivo de placeholders deve conter um objeto JSON")
    return {str(k): {str(sub_k): str(sub_v) for sub_k, sub_v in v.items()} for k, v in data.items()}


def load_dataset(path: str) -> List[Dict[str, str]]:
    csv_path = Path(path)
    if not csv_path.exists():
        raise FileNotFoundError(f"CSV não encontrado: {path}")
    with csv_path.open(encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        columns = set(reader.fieldnames or [])
        missing = REQUIRED_COLUMNS - columns
        if missing:
            raise ValueError(f"CSV ausente colunas requeridas: {sorted(missing)}")
        rows = []
        for index, row in enumerate(reader, start=1):
            row["_order"] = index
            rows.append(row)
    return rows


def normalize_time(value: str) -> str:
    value = value.strip()
    if len(value) == 5:
        return f"{value}{TIME_SUFFIX}"
    if len(value) == 8:
        return value
    raise ValueError(f"Hora inválida: {value}")


def apply_placeholders(
    rows: List[Dict[str, str]],
    placeholder_map: Dict[str, Dict[str, str]],
) -> None:
    for row in rows:
        for key in ("mandante", "visitante"):
            current = row[key]
            slot_key = f"{key}_slot"
            slot_value = row.get(slot_key, "")
            substitution = placeholder_map.get(current) or placeholder_map.get(slot_value)
            if not substitution:
                continue
            new_name = substitution.get("name") or substitution.get("nome")
            if new_name:
                row[key] = new_name
            new_slot = substitution.get("slot")
            if new_slot:
                row[slot_key] = new_slot
            new_group = substitution.get("group") or substitution.get("grupo")
            if new_group and row["fase"] == "Grupos":
                row["grupo"] = new_group


def is_placeholder_team(name: str, slot: str) -> bool:
    if slot:
        return True
    markers = ("Playoff", "º", "V", "Repescagem")
    return any(marker in name for marker in markers)


def collect_teams(
    rows: Iterable[Dict[str, str]],
    placeholder_group: str,
) -> Dict[str, Team]:
    teams: Dict[str, Team] = {}
    for row in rows:
        for side in ("mandante", "visitante"):
            name = row[side].strip()
            if not name:
                continue
            slot = row.get(f"{side}_slot", "").strip()
            group = row["grupo"].strip()
            placeholder = is_placeholder_team(name, slot)
            if not group:
                group = placeholder_group if placeholder else ""
            if not group:
                raise ValueError(
                    f"Equipe sem grupo definido: {name} (linha {row['_order']})"
                )
            existing = teams.get(name)
            team = Team(name=name, group=group, slot=slot, is_placeholder=placeholder)
            if existing and existing != team:
                raise ValueError(f"Inconsistência de grupo/slot para {name}")
            teams[name] = team
    return teams


def assign_team_ids(teams: Dict[str, Team], start_id: int) -> Dict[str, int]:
    def sort_key(item: Tuple[str, Team]) -> Tuple[int, str, str]:
        _, team = item
        return (1 if team.is_placeholder else 0, team.group, team.name)

    ordered = sorted(teams.items(), key=sort_key)
    return {name: start_id + index for index, (name, _) in enumerate(ordered)}


def collect_matches(
    rows: Iterable[Dict[str, str]],
    start_jogo_id: int,
) -> List[Match]:
    matches: List[Match] = []
    for offset, row in enumerate(sorted(rows, key=lambda r: (int(r["fase_ordem"]), r["data_iso"], r["hora_brt"], r["_order"]))):
        fase_codigo_raw = row["fase_codigo"]
        if not fase_codigo_raw:
            raise ValueError(f"Fase sem código na linha {row['_order']}")
        fase_codigo = int(fase_codigo_raw)
        matches.append(
            Match(
                jogo_id=start_jogo_id + offset,
                data_iso=row["data_iso"],
                hora=normalize_time(row["hora_brt"]),
                local=row["estadio"].strip(),
                equipe1=row["mandante"].strip(),
                equipe2=row["visitante"].strip(),
                fase_codigo=fase_codigo,
            )
        )
    return matches


def sql_escape(value: str) -> str:
    return value.replace("'", "''")


def build_sql(
    teams: Dict[str, Team],
    team_ids: Dict[str, int],
    matches: Sequence[Match],
) -> str:
    lines: List[str] = []
    timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
    lines.append(f"-- Gerado por atualizar_copa2026_dataset.py em {timestamp}")
    lines.append("SET NAMES utf8mb4;")
    lines.append("SET FOREIGN_KEY_CHECKS = 0;")
    lines.append("TRUNCATE TABLE `PAI_PALPITE_INDIVIDUAL`;")
    lines.append("TRUNCATE TABLE `BOI_BOLAO_INDIVIDUAL`;")
    lines.append("TRUNCATE TABLE `PAL_PALPITE`;")
    lines.append("TRUNCATE TABLE `JOG_JOGO`;")
    lines.append("TRUNCATE TABLE `EQP_EQUIPE`;")
    lines.append("")

    team_entries = []
    for name, team in sorted(teams.items(), key=lambda item: team_ids[item[0]]):
        team_entries.append(
            f"  ({team_ids[name]}, '{sql_escape(name)}', '{sql_escape(team.group)}')"
        )
    lines.append("INSERT INTO `EQP_EQUIPE` (`EQP_ID`, `EQP_PAIS`, `EQP_GRUPO`) VALUES")
    lines.append(",\n".join(team_entries))
    lines.append("ON DUPLICATE KEY UPDATE")
    lines.append("  `EQP_PAIS` = VALUES(`EQP_PAIS`),")
    lines.append("  `EQP_GRUPO` = VALUES(`EQP_GRUPO`);")
    lines.append("")

    match_entries = []
    for match in matches:
        match_entries.append(
            (
                match.jogo_id,
                match.data_iso,
                match.hora,
                match.local,
                match.equipe1,
                match.equipe2,
                match.fase_codigo,
            )
        )

    values_lines = []
    for jogo_id, data_iso, hora, local, equipe1, equipe2, fase_codigo in match_entries:
        values_lines.append(
            "  ({jid}, '{data}', '{hora}', '{local}', {eq1}, {eq2}, NULL, NULL, {fase})".format(
                jid=jogo_id,
                data=data_iso,
                hora=hora,
                local=sql_escape(local),
                eq1=team_ids[equipe1],
                eq2=team_ids[equipe2],
                fase=fase_codigo,
            )
        )

    lines.append(
        "INSERT INTO `JOG_JOGO` "
        "(`JOG_ID`, `JOG_DATA`, `JOG_HORA`, `JOG_LOCAL`, `JOG_EQP1_ID`, `JOG_EQP2_ID`, "
        "`JOG_EQP1_GOLS`, `JOG_EQP2_GOLS`, `JOG_FASE`) VALUES"
    )
    lines.append(",\n".join(values_lines))
    lines.append("ON DUPLICATE KEY UPDATE")
    lines.append("  `JOG_DATA` = VALUES(`JOG_DATA`),")
    lines.append("  `JOG_HORA` = VALUES(`JOG_HORA`),")
    lines.append("  `JOG_LOCAL` = VALUES(`JOG_LOCAL`),")
    lines.append("  `JOG_EQP1_ID` = VALUES(`JOG_EQP1_ID`),")
    lines.append("  `JOG_EQP2_ID` = VALUES(`JOG_EQP2_ID`),")
    lines.append("  `JOG_FASE` = VALUES(`JOG_FASE`);")
    lines.append("")
    lines.append("SET FOREIGN_KEY_CHECKS = 1;")
    lines.append("")
    return "\n".join(lines)


def save_csv(path: str, rows: Sequence[Dict[str, str]]) -> None:
    dest = Path(path)
    dest.parent.mkdir(parents=True, exist_ok=True)
    with dest.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=[c for c in rows[0] if c != "_order"])
        writer.writeheader()
        for row in rows:
            row_copy = {k: v for k, v in row.items() if k != "_order"}
            writer.writerow(row_copy)


def save_sql(path: str, content: str) -> None:
    dest = Path(path)
    dest.parent.mkdir(parents=True, exist_ok=True)
    with dest.open("w", encoding="utf-8") as handle:
        handle.write(content)


def print_summary(teams: Dict[str, Team], matches: Sequence[Match]) -> None:
    total_placeholders = sum(1 for team in teams.values() if team.is_placeholder)
    print(f"Equipes: {len(teams)} (placeholders: {total_placeholders})")
    print(f"Jogos: {len(matches)}")
    phases = defaultdict(int)
    for match in matches:
        phases[match.fase_codigo] += 1
    for fase_codigo in sorted(phases):
        print(f"  Fase {fase_codigo}: {phases[fase_codigo]} jogos")


def main(argv: Sequence[str]) -> int:
    args = parse_args(argv)
    rows = load_dataset(args.input)
    placeholder_map = load_placeholder_map(args.placeholders)
    apply_placeholders(rows, placeholder_map)
    teams = collect_teams(rows, args.placeholder_group)
    team_ids = assign_team_ids(teams, args.starting_eqp_id)
    matches = collect_matches(rows, args.starting_jogo_id)
    sql_content = build_sql(teams, team_ids, matches)

    if args.output_csv and not args.dry_run:
        save_csv(args.output_csv, rows)
    if not args.dry_run:
        save_sql(args.output_sql, sql_content)

    print_summary(teams, matches)
    if args.dry_run:
        print("Dry-run concluído: nenhum arquivo gravado.")
    else:
        print(f"SQL gravado em: {args.output_sql}")
        if args.output_csv:
            print(f"CSV final gravado em: {args.output_csv}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
