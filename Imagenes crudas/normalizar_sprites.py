#!/usr/bin/env python3
"""
Segunda etapa: toma los frames sueltos de procesar_sprites.py y los deja iguales para los 4 equipos.

  - Selecciona y ordena los frames segun mapa_frames.json (mismo numero de frames y mismo
    significado de cada posicion en todos los equipos).
  - Alinea cada frame por los PIES: ancla horizontal = centro del torso/cabeza,
    ancla vertical = base del personaje (el efecto no cuenta si es una pieza separada).
  - Exporta frames sueltos, una hoja ordenada por equipo y un JSON con las animaciones.

Uso:
    python normalizar_sprites.py --entrada sprites_limpios --mapa mapa_frames.json --salida normalizado
"""
import argparse
import json
import os

import numpy as np
from PIL import Image
from scipy import ndimage as ndi

LIENZO = 384
ANCLA = (LIENZO // 2, 340)   # (x, y) donde quedan los pies de todos los frames
ALTURA_IDLE = 240            # altura objetivo (px) del personaje en idle; 0 = no escalar


def anclaje(im):
    """Devuelve (x, y_pies) del personaje: pieza conectada mas grande del frame."""
    a = np.array(im)[..., 3] > 40
    unida = ndi.binary_dilation(a, iterations=2)
    et, n = ndi.label(unida)
    if n == 0:
        return LIENZO // 2, LIENZO - 1
    areas = ndi.sum(a, et, range(1, n + 1))
    k = int(np.argmax(areas)) + 1
    cuerpo = (et == k) & a
    ys, xs = np.nonzero(cuerpo)
    y_pies = ys.max()
    alto = y_pies - ys.min()
    sup = ys < ys.min() + 0.55 * alto            # cabeza + torso
    x = xs[sup].mean() if sup.any() else xs.mean()
    return float(x), float(y_pies)


def altura(im):
    a = np.array(im)[..., 3] > 40
    ys = np.nonzero(a.any(axis=1))[0]
    return float(ys.max() - ys.min()) if len(ys) else 1.0


def colocar(im, escala):
    """Escala el frame, calcula su ancla y lo pega con los pies en ANCLA."""
    bb = im.getbbox()
    if bb is None:
        return im
    rec = im.crop(bb)
    if abs(escala - 1) > 0.005:
        rec = rec.resize((max(1, round(rec.width * escala)), max(1, round(rec.height * escala))),
                         Image.LANCZOS)
    x, y = anclaje(rec)
    out = Image.new("RGBA", (LIENZO, LIENZO), (0, 0, 0, 0))
    out.paste(rec, (int(round(ANCLA[0] - x)), int(round(ANCLA[1] - y))))
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--entrada", default="sprites_limpios")
    ap.add_argument("--mapa", default="mapa_frames.json")
    ap.add_argument("--salida", default="normalizado")
    args = ap.parse_args()

    mapa = json.load(open(args.mapa, encoding="utf-8"))
    anims = mapa["orden_animaciones"]
    indice = {}
    avisos = []

    for equipo, sel in mapa.items():
        if equipo.startswith("_") or equipo in ("orden_animaciones", "posiciones"):
            continue
        carpeta = os.path.join(args.salida, equipo)
        os.makedirs(os.path.join(carpeta, "frames"), exist_ok=True)
        escala = 1.0
        if ALTURA_IDLE:
            hs = [altura(Image.open(os.path.join(args.entrada, equipo, "frames",
                  f"{anims.index('idle') + 1:02d}_idle_{o}.png").replace("\\", "/")).convert("RGBA"))
                  for o in sel["idle"]]
            escala = ALTURA_IDLE / (sum(hs) / len(hs))
        max_cols = max(len(sel[a]) for a in anims)
        hoja = Image.new("RGBA", (max_cols * LIENZO, len(anims) * LIENZO), (0, 0, 0, 0))
        info = {"celda": LIENZO, "ancla_pies": list(ANCLA), "escala_aplicada": round(escala, 3),
                "animaciones": {}}
        for fila, anim in enumerate(anims):
            nfila = anims.index(anim) + 1
            for k, orig in enumerate(sel[anim], start=1):
                ruta = os.path.join(args.entrada, equipo, "frames", f"{nfila:02d}_{anim}_{orig}.png")
                im = Image.open(ruta).convert("RGBA")
                im = colocar(im, escala)
                bb = im.getbbox()
                if bb and (bb[0] <= 0 or bb[1] <= 0 or bb[2] >= LIENZO or bb[3] >= LIENZO):
                    avisos.append(f"{equipo} {anim} {k}: toca el borde del lienzo")
                im.save(os.path.join(carpeta, "frames", f"{anim}_{k}.png"))
                hoja.paste(im, ((k - 1) * LIENZO, fila * LIENZO))
            info["animaciones"][anim] = {"fila": fila, "frames": len(sel[anim]),
                                         "frames_originales": sel[anim]}
        hoja.save(os.path.join(carpeta, f"{equipo}_spritesheet.png"))
        json.dump(info, open(os.path.join(carpeta, f"{equipo}_spritesheet.json"), "w",
                             encoding="utf-8"), indent=2, ensure_ascii=False)
        indice[equipo] = {a: len(sel[a]) for a in anims}
        print(f"[{equipo}] escala x{escala:.3f} | " + ", ".join(f"{a}={len(sel[a])}" for a in anims))
    for av in avisos:
        print("AVISO:", av)


if __name__ == "__main__":
    main()
