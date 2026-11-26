# Assets de DOPOSoccer

Todo lo de esta carpeta se carga desde el classpath, por ejemplo
`getClass().getResource("/sprites/ball/ball.png")`. Así funciona igual en
Eclipse, en VS Code y dentro de un `.jar`.

## Estructura

```
resources/
  sprites/
    characters/<pais>/   un personaje por carpeta (korea, cameroon, nigeria, usa)
    ball/                balón
    shots/               cómo se ve cada tiro de poder (aura azul, rayo, tornado, ilusión)
    effects/             aura y efectos sobre el rival (chispas, estrellas, empuje)
  field/                 fondo, piso y arco
  ui/
    buttons/             botones del menú
    icons/               íconos del HUD (vida, poder, reloj, pausa)
    flags/               bandera de cada país para las tarjetas
  fonts/                 fuentes .ttf
```

## Convenciones

- Nombres en inglés, en minúscula y separados con guion bajo.
- Solo PNG con fondo transparente.
- **Personajes:** dibujados mirando a la DERECHA. El jugador de la derecha
  se voltea al dibujarlo, así que no hay que duplicar imágenes.
- **Animaciones de personaje:** cuadros sueltos numerados desde 1, por
  ejemplo `run_1.png` … `run_6.png`. El código los carga en orden hasta que
  falta un número.
- **Acciones por personaje:** `idle` (4), `run` (6), `jump` (5), `kick` (5),
  `header` (5), `power` (6), `celebration` (4) y `defeat` (4).
- **Cuadros de personaje:** de 384x384, con los pies siempre en el punto
  (192, 340) y el personaje parado de unos 240 px de alto. El juego lo
  escala para que mida lo mismo que el cuerpo del jugador (70x120) y apoya
  ese punto en el piso.
- **Origen:** los sprites salen de `Imagenes crudas/sprites_normalizados`
  (script `normalizar_sprites.py`). Las carpetas `camerun` y
  `estados_unidos` de allá corresponden aquí a `cameroon` y `usa`.
- **Tamaño de dibujo:** pensado para la cancha lógica de 1280x720. El
  jugador ocupa 70x120 y el balón tiene un diámetro de 36; el juego escala
  todo según la resolución elegida.
- **Arco:** se dibuja solo el izquierdo (`field/goal.png`); el derecho es
  el mismo volteado.
- **Fondo:** `field/background.png` a 1280x720, SIN arcos pintados. Los
  arcos del juego están en x 0–100 (izquierdo) y x 1180–1280 (derecho), de
  280 px de alto sobre el piso (y = 640); si el fondo trae arcos en otra
  posición, el balón entra donde no se ve el arco.

## Balón, tiros, cancha e interfaz (ya conectados en el juego)

Todas las imágenes pasan por `SpriteLoader`, que las lee del classpath una sola
vez y guarda en caché también las versiones escaladas (y en 9-slice), así que
nunca se escala dentro de `paintComponent`. Si una imagen falta, se registra
una vez y quien dibuja usa su respaldo en Java2D (`ImageButton` cae a
`MenuButton`, `HudButton` muestra texto, las insignias se omiten).

- `sprites/ball/roll_1..6.png`: balón rodando (`GamePanel`).
- `sprites/ball/kick_1..6.png`: animación corta del balón al ser pateado.
- `sprites/shots/<tiro>/…` (`blue_aura`, `lightning`, `tornado`, `illusion`):
  `charge_N` se reproduce una vez al iniciar el tiro, `fly_N` se repite mientras
  el tiro está activo, `impact_N` se reproduce sobre el balón al impactar y
  `tornado/bounce_N` al rebotar el tornado. Los balones falsos de la Ilusión
  usan los mismos cuadros.
- `sprites/characters/<pais>/<accion>_N`: animaciones de los jugadores
  (`PlayerAnimation`).
- Cuadros del balón: de 256x256, con el balón de 96 px en el centro. El juego
  los escala al diámetro real del balón (36).
- `field/background.png`: cancha del partido (de `campo 2.jpeg`), a 1280x720.
- `field/stadium.png`: fondo del menú (de `campo.jpeg`).
- `field/goal.png`: arco de un solo lado (es el derecho; el izquierdo se
  voltea). Se dibuja 1.4 veces más ancho que la zona de gol.
- `ui/hud/` en el partido (`GamePanel`):
  - `scoreboard_panel`: fondo del marcador.
  - `clock_timer_bar`: fondo del tiempo restante o de la meta de goles.
  - `ribbon_blue` / `ribbon_red`: cinta con el nombre del jugador izquierdo y
    derecho.
  - `life_bar` / `power_bar`: traen el relleno pintado, así que se dibuja un
    fondo oscuro y encima la imagen recortada horizontalmente según el
    porcentaje de vida o de poder.
  - `badge_fire`: aura activa o barra de poder llena. `badge_bolt`: jugador
    aturdido o electrocutado.
  - `goal_banner` (al marcar) y `confetti_burst` (al ganar, a ambos lados).
- `ui/hud/stats_board`: fondo en 9-slice del panel de mejoras del paso 2
  (`SetupPanel`) y fondo de la pantalla de fin de partido.
- `ui/buttons/<nombre>_normal.png` y `_pressed.png` (vía `ImageButton` y
  `HudButton`, ambas escaladas al mismo tamaño):
  - `play`: JUGAR (menú) y COMENZAR (paso 2).
  - `settings`: CÓMO JUGAR (abre `HelpDialog`).
  - `confirm`: ENTENDIDO en la ayuda y comprar mejora en el paso 2.
  - `back` / `forward`: volver y siguiente entre menú, paso 1 y paso 2.
  - `pause`: pausar y reanudar. `home`: volver al menú (`M`).
  - `restart`: revancha. `trophy`: ícono del resultado final.

### Disponibles pero sin usar

- `ui/buttons/sound_*` y `music_*`: el juego no tiene audio.
- `ui/hud/badge_shield.png`: no hay un efecto de defensa en el dominio.
- `ui/icons/`, `ui/flags/`, `fonts/` y `sprites/effects/`: carpetas vacías
  (solo `.gitkeep`).
- Los JPEG originales venían con fondo magenta (balones) o blanco (arco). Se
  recortaron quitando ese color y dividiendo cada hoja en cuadros iguales
  (6 columnas por fila).

## Cómo se usa desde la GUI

```java
PlayerAnimation leftAnimation = new PlayerAnimation(Side.LEFT);   // una por jugador

// en cada cuadro del juego:
leftAnimation.update(match, dt);
Player left = match.getLeftPlayer();
Hitbox body = left.getBounds();
BufferedImage frame = leftAnimation.currentFrame(left);
if (!SpriteLoader.drawCharacter(g2, frame, body.getX(), body.getY(),
        body.getWidth(), body.getHeight(), leftAnimation.isFlipped())) {
    // no hay imagen: dibujar como antes
}
```
- **Licencia:** usar solo assets propios o libres (CC0, por ejemplo Kenney).
  No usar imágenes del Head Soccer original.
