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

- Nombres en inglés, en minúscula y separados con guion bajo:
  `idle.png`, `walk.png`, `kick.png`.
- Solo PNG con fondo transparente.
- **Personajes:** dibujados mirando a la DERECHA. El jugador de la derecha
  se voltea al dibujarlo, así que no hay que duplicar imágenes.
- **Animaciones:** una tira horizontal por acción, con cuadros del mismo
  tamaño (por ejemplo `walk.png` de 4 cuadros de 128x128). El código divide
  la tira por el alto de la imagen.
- **Archivos por personaje:** `idle.png`, `walk.png`, `jump.png`, `kick.png`
  y `stunned.png`.
- **Tamaño de dibujo:** pensado para la cancha lógica de 1280x720. El
  jugador ocupa 70x120 y el balón tiene un diámetro de 36; el juego escala
  todo según la resolución elegida.
- **Arco:** se dibuja solo el izquierdo (`field/goal.png`); el derecho es
  el mismo volteado.
- **Fondo:** `field/background.png` a 1280x720.
- **Licencia:** usar solo assets propios o libres (CC0, por ejemplo Kenney).
  No usar imágenes del Head Soccer original.
