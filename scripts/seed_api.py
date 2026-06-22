#!/usr/bin/env python3
"""
Seeder script for contas-termometro-project API.

Usage (PowerShell):
  python .\scripts\seed_api.py

Options:
  --base-url URL     Base URL of the API (default: http://localhost:8081)
  --skip-duplicates  Skip inserting entries that appear already for the reference month

This script posts a small set of example `lancamentos` to POST /api/lancamentos
and then fetches the month to confirm insertion.
"""
import argparse
import sys
import time
from typing import List

import requests


DEFAULT_BASE = "http://localhost:8081"


def health_check(session: requests.Session, base_url: str) -> bool:
    url = f"{base_url}/api/sistema/saude"
    try:
        r = session.get(url, timeout=5)
        r.raise_for_status()
        print(f"Health: {r.json().get('status')} - {r.json().get('application')}")
        return True
    except Exception as e:
        print(f"Health check failed: {e}")
        return False


def get_lancamentos_by_mes(session: requests.Session, base_url: str, mes: str) -> List[dict]:
    url = f"{base_url}/api/lancamentos"
    params = {"mes": mes}
    r = session.get(url, params=params, timeout=10)
    r.raise_for_status()
    return r.json()


def post_lancamento(session: requests.Session, base_url: str, payload: dict) -> dict:
    url = f"{base_url}/api/lancamentos"
    r = session.post(url, json=payload, timeout=10)
    r.raise_for_status()
    return r.json()


def exists_duplicate(existing: List[dict], payload: dict) -> bool:
    # crude duplicate check: same descricao, data, valor and tipo
    for e in existing:
        if (
            e.get("descricao") == payload.get("descricao")
            and e.get("data") == payload.get("data")
            and float(e.get("valor")) == float(payload.get("valor"))
            and e.get("tipo") == payload.get("tipo")
        ):
            return True
    return False


def seed(session: requests.Session, base_url: str, payloads: List[dict], skip_duplicates: bool = True):
    if not health_check(session, base_url):
        print("API not healthy, aborting seeding.")
        return

    # group payloads by mesReferencia to avoid repeated GETs
    by_month = {}
    for p in payloads:
        by_month.setdefault(p["mesReferencia"], []).append(p)

    created = []
    for mes, items in by_month.items():
        try:
            existing = get_lancamentos_by_mes(session, base_url, mes)
        except Exception as e:
            print(f"Failed to fetch existing lancamentos for {mes}: {e}")
            existing = []

        for p in items:
            if skip_duplicates and exists_duplicate(existing, p):
                print(f"Skipping duplicate: {p['descricao']} ({p['data']})")
                continue

            try:
                created_obj = post_lancamento(session, base_url, p)
                created.append(created_obj)
                print(f"Created: id={created_obj.get('id')} descricao={created_obj.get('descricao')}")
            except Exception as e:
                print(f"Failed to create {p}: {e}")

            # small delay to reduce chance of sqlite locking issues
            time.sleep(0.2)

    print(f"Seeding finished. Created {len(created)} items.")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default=DEFAULT_BASE, help="API base URL")
    parser.add_argument("--no-skip-duplicates", dest="skip_duplicates", action="store_false")
    args = parser.parse_args(argv)

    session = requests.Session()

    # example payloads inferred from LancamentoRequest.kt
    payloads = [
        {
            "tipo": "ENTRADA",
            "descricao": "Salário",
            "valor": 4500.00,
            "data": "2026-06-01",
            "mesReferencia": "2026-06",
            "categoria": "Receitas",
            "observacao": "Pagamento mensal",
        },
        {
            "tipo": "SAIDA_FIXA",
            "descricao": "Aluguel",
            "valor": 1500.00,
            "data": "2026-06-03",
            "mesReferencia": "2026-06",
            "categoria": "Moradia",
        },
        {
            "tipo": "GASTO_DIARIO",
            "descricao": "Supermercado",
            "valor": 120.35,
            "data": "2026-06-10",
            "mesReferencia": "2026-06",
            "categoria": "Alimentação",
            "observacao": "Compra semanal",
        },
        {
            "tipo": "INVESTIMENTO",
            "descricao": "Tesouro Direto",
            "valor": 300.00,
            "data": "2026-06-15",
            "mesReferencia": "2026-06",
            "categoria": "Investimentos",
        },
        {
            "tipo": "AJUSTE_SALDO",
            "descricao": "Ajuste contábil",
            "valor": -5.00,
            "data": "2026-06-30",
            "mesReferencia": "2026-06",
            "observacao": "Correção pequena",
        },
    ]

    seed(session, args.base_url, payloads, skip_duplicates=args.skip_duplicates)


if __name__ == "__main__":
    try:
        main(sys.argv[1:])
    except KeyboardInterrupt:
        print("Interrupted by user")
        sys.exit(2)

