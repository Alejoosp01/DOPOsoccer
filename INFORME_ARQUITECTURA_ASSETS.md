# Informe — Análisis y plan de arquitectura de assets (DOPOSoccer)

**Fecha:** 22 de noviembre de 2026
**Proyecto:** DOPOSoccer 2026-2 (Head Soccer 2D, Java 21 + Swing + Java2D)
**Rama actual:** `modificaciones-finales` (HEAD: `1b3a549` — "arreglos configuraciones")
**Rama origen del trabajo de assets:** `origin/alejo-modificaciones` (commit `5728128` — "Arquitectura para los assets y modificacion en la gui"), fusionada vía PR #1 (merge `3e13b31`).

---

## 1. Contexto: la nueva arquitectura encontrada

Al re-analizar el proyecto se descubrió que **la nueva arquitectura de assets ya estaba implementada** en el árbol actual (vía rama `alejo-modificaciones` y su posterior fusión en `modificaciones-finales`):

- `src/presentation/SpriteLoader.java` — **nuevo**, +286 líneas respecto a `main`.
- `test/presentation/SpriteLoaderTest.java` — **nuevo**, +87 líneas.
- `resources/ASSETS.md` — **nuevo**, documenta la convención de assets.
- Carpetas `resources/` (`sprites/characters/*`, `sprites/ball`, `sprites/shots`, `sprites/effects`, `field/`, `ui/buttons`, `ui/icons`, `ui/flags`, `fonts/`) con suelo `.gitkeep` (sin PNG reales todavía).
- `README.md` actualizado: ejecuta con `java -cp "out;resources" presentation.DOPOSoccerApp`; resultado actual de pruebas: **195 pruebas, 195 exitosas**.
- `.classpath` y `.project` eliminados (ahora solo VS Code: `.vscode/` versionado).
- El resto del árbol de presentación (`GamePanel`, menús, HUD) **sigue dibujando proceduralmente**; ningún consumidor real de `SpriteLoader` existe fuera del propio loader y su test.

---

## 2. Diagnóstico inicial (re-analizado con la nueva arquitectura)

### 2.1 Capa de presentación
- **Stack:** Java 21, Swing + Java2D (`Graphics2D`), sin JavaFX/LibGDX/SpriteIO.
- **Dibujo:** procedural (`paintComponent`), componentes Swing custom-pintados (`MenuButton`, `CharacterCard`, `ChoiceChip`), todo centralizado en `UiTheme`.

### 2.2 Nuevo: `SpriteLoader` (Asset Manager ya implementado)
- Clase `final`, constructor privado, métodos **estáticos** (estilo utility), con:
  - Rutas classpath constantes: `/sprites/characters/`, `/sprites/ball/ball.png`, `/sprites/shots/`, `/sprites/effects/`, `/field/`, `/ui/buttons/`, `/ui/icons/`, `/ui/flags/`, `/fonts/`; extensión `.png`.
  - Acciones de personaje: `IDLE`, `WALK`, `JUMP`, `KICK`, `STUNNED`.
  - Cache: `Map<String, BufferedImage> IMAGES`, `Map<String, BufferedImage[]> FRAMES`, `Set<String> MISSING`.
  - Fallback: `null` / arreglo vacío / fuente fallback; log vía `domain.ErrorLog`.
  - Utilidades: `load`, `exists`, `frames`, `split`, `frameAt(frames, seconds, fps)`, `flipHorizontally`, `draw(g2, image, x, y, w, h, flipped)`, `characterFolder`, `characterPath`, `characterFrames`, `flag`, `font`, `clear`.
- Convención (`resources/ASSETS.md`): un personaje por carpeta (`korea`, `cameroon`, `nigeria`, `usa`); una tira horizontal por acción; personajes mirando a la derecha (el derecho se voltea); espacio base 1280x720; PNG con fondo transparente; licencia libre (CC0/Kenney).

### 2.3 Qué falta ahora
1. Consumir `SpriteLoader` en la GUI real (GamePanel, menús, HUD, botones).
2. Máquina de estados de animación (estado partida → acción: idle/walk/jump/kick/aura/stun/efecto) usando `frameAt`.
3. Assets reales (PNG).
4. Escalado por resolución.

---

## 3. Propuesta de arquitectura (recomendación actualizada)

- **Mantener `SpriteLoader` como Asset Manager** (no crear uno nuevo): ya implementa loader + caché + fallback + ErrorLog desde classpath, siguiendo el patrón propuesto (Repository/Loader + Factory implícita + Cache + fallback).
- **Capa de orquestación siguiente:** adapter/factory en presentation que, dado `Player`/`GameCharacter`, decida la acción y devuelva frames (`SpriteLoader.characterFrames`).
- **Dominio intacto:** sin cambios (Player sigue exponiendo estado: posición, aura, efecto, stun, powerBar, character).
- **Convención:** se mantiene `resources/ASSETS.md`.

---

## 4. Impacto en el código (estado actual vs. objetivo)

### Hecho
- `SpriteLoader`, `SpriteLoaderTest`, `ASSETS.md`, estructura `resources/`, classpath runtime con `resources`, README, migración a VS Code, 195 tests en verde.

### Pendiente
| Archivo | Acción |
|---|---|
| `src/presentation/GamePanel.java` | Consumir `SpriteLoader` (fondo, jugador, balón, goal, shots/effects, HUD, overlays) con fallback procedural. |
| `MainMenuPanel`, `SetupPanel`, `HelpDialog`, `MenuButton`, `CharacterCard`, `UiTheme` | Consumir assets UI (`/ui/buttons/`, `/ui/flags/`, `/ui/icons/`, `/field/`, `/fonts/`, `/sprites/`). |
| `GameFrame` | Preparar assets/fuente de fachada (ligero). |
| `resources/**` | Añadir PNG reales. |
| Tests | Faltan tests de integración (mapping acción↔estado, fallback en panel). |

Dominio: sin cambios por esta feature.

---

## 5. Plan de implementación actualizado (reordenado)

1. ✅ **Hecho:** SpriteLoader + tests + ASSETS.md + recursos/esqueleto + classpath/README + 195 tests.
2. **Siguiente (prioridad alta):** Integrar `SpriteLoader` en `GamePanel` (fondo, jugador, balón, goal, shots/effects) con fallback procedural.
3. Menús/UI: `MainMenuPanel`, `SetupPanel`, `MenuButton`, `HelpDialog`, `CharacterCard` (+ flags/icons/buttons/fonts).
4. HUD: scoreboard, barras, icons, banner.
5. Máquina de estados de animación (`frameAt`, acciones).
6. Resoluciones: escalado/pre-escalado.
7. Assets reales (PNG) + ajustes finales + docs/diagrama/tests finales.

---

## 6. Riesgos y alternativas (revisados con la nueva arquitectura)

- Cache estática en `SpriteLoader` (con `clear()` para tests) — bien pensada para Swing EDT y headless.
- Recomendación: **seguir con `SpriteLoader`** actual (ya implementado/probado/documentado); refactorizar a inyectable solo si se requiere.
- Fallback ya integrado en el propio loader.
- Alternativas consideradas: AssetManager inyectable (descartada como paso inmediato), assets por imagen individual (no usada actualmente), migración gradual (sí, adoptada).

---

## 7. Estado para revisión

- **Sí:** nueva arquitectura de assets encontrada y analizada con detalle.
- **Pendiente:** integración real en la GUI + assets finales.
- **Siguiente paso propuesto:** aprobar y comenzar con el paso 2 del plan (integrar `SpriteLoader` en `GamePanel`).

---

*Fin del informe.*
