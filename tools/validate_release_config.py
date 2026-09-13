#!/usr/bin/env python3
"""Fails fast when release placeholders/test AdMob IDs are still configured."""
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
props = {}
for raw in (root / "monetization.properties").read_text(encoding="utf-8").splitlines():
    line = raw.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, v = line.split("=", 1)
    props[k.strip()] = v.strip()

test_prefix = "ca-app-pub-3940256099942544"
errors = []
for key in ("ADMOB_APP_ID", "ADMOB_BANNER_ID", "ADMOB_INTERSTITIAL_ID", "ADMOB_REWARDED_ID"):
    value = props.get(key, "")
    if not value or value.startswith(test_prefix):
        errors.append(f"{key}: sigue usando un ID de prueba")
if props.get("PREMIUM_PRODUCT_ID", "") in ("", "premium_lifetime"):
    print("INFO: PREMIUM_PRODUCT_ID usa 'premium_lifetime'. Es válido si creas el producto con ese mismo ID en Play Console.")
if props.get("SUPPORT_EMAIL", "").endswith(".invalid"):
    errors.append("SUPPORT_EMAIL: sustituye el placeholder por un contacto real antes de publicar")
for key in ("PRIVACY_URL", "TERMS_URL"):
    if not props.get(key, "").startswith("https://"):
        errors.append(f"{key}: debe ser una URL https pública")

if errors:
    print("CONFIGURACIÓN DE RELEASE PENDIENTE:")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print("OK: configuración de release sin IDs de prueba detectados.")
