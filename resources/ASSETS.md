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
