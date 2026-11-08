# POOBsoccer

Simulacion de futbol de cabezas para el proyecto final de DOPO 2026-2.
El proyecto esta dividido en un motor de dominio independiente de la interfaz y
una aplicacion de escritorio hecha con Swing y Java2D.

## Requisitos

- JDK 21 o superior (el proyecto se compila con `--release 21`).
- No requiere librerias externas para jugar.
- JUnit 5 se usa solo para las pruebas (`lib/junit-platform-console-standalone-1.14.4.jar`).

## Como ejecutar

Desde la carpeta `POOBsoccer`:

```powershell
javac --release 21 -encoding UTF-8 -d out (Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp out presentation.HeadSoccerApp
```

En VS Code o Eclipse tambien se puede ejecutar la clase
`presentation.HeadSoccerApp`.

## Como jugar

1. En el menu principal elige `JUGAR`.
2. Selecciona modalidad, forma de terminar, el valor de la regla y un pais para cada lado.
3. Presiona `COMENZAR`.

### Controles

| Accion | Jugador 1 (izquierda) | Jugador 2 (derecha) |
|---|---|---|
| Mover | `A` / `D` | Flechas izquierda / derecha |
| Saltar | `W` | Flecha arriba |
| Patear | `S` | Flecha abajo |
| Poder | `Q` | `Ctrl` |
| Pausa | `ESC` o `P` | `ESC` o `P` |
| Volver al menu | `M` | `M` |
| Revancha al terminar | `R` o `Enter` | `R` o `Enter` |

En `Humano vs Maquina` el jugador 1 controla el lado izquierdo y la maquina el
derecho. En `Maquina vs Maquina` no se usan las teclas de jugador.

## Modalidades

- **1 vs 1:** dos jugadores humanos.
- **Humano vs Maquina:** el jugador controla la izquierda.
- **Maquina vs Maquina:** la IA controla ambos lados.

## Formas de terminar

- **Por tiempo:** 60 segundos por defecto. Si hay empate se juega gol de oro.
- **Por goles:** gana el primero que llegue al objetivo (3 por defecto).
- **Supervivencia:** cada lado empieza con 5 balones. Cada gol le quita un balon
  al lado que lo recibe. El jugador avanza de ronda mientras gane; la serie
  termina cuando pierde.

## Poderes

La barra de poder se llena en 10 segundos. Al presionar la tecla de poder queda
armado y se activa con el siguiente golpe al balon.

| Pais | Poder | Efecto |
|---|---|---|
| Canada | Congelacion | Congela al rival 3 segundos |
| Colombia | Reduccion | Encoge al rival 5 segundos |
| Brasil | Confusion | Invierte los controles del rival 5 segundos |
| Corea del Sur | Empuje | Dispara el balon y aturde al rival que lo toque |
| Estados Unidos | Ilusion | Dispara el balon con 7 copias falsas; el balon real atraviesa al rival |

## Pruebas

```powershell
javac --release 21 -encoding UTF-8 -d out (Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac --release 21 -encoding UTF-8 -cp "out;lib/junit-platform-console-standalone-1.14.4.jar" -d out (Get-ChildItem test -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -jar lib/junit-platform-console-standalone-1.14.4.jar --class-path out --scan-class-path --disable-banner --details=summary
```

Resultado actual: **139 pruebas, 139 exitosas**.

## Documentacion

El detalle de la arquitectura, las reglas, las constantes de fisica, las fases y
las pruebas esta en [INFORME_FASES.md](INFORME_FASES.md).

## Estructura

```
src/domain/        Motor del juego, fisica, reglas, poderes, IA y supervivencia
src/presentation/  Interfaz Swing/Java2D, menus, HUD y controles
test/domain/       Pruebas del dominio
test/presentation/ Pruebas headless de la sesion de juego
lib/               Runner de JUnit 5
resources/         Reservado para recursos futuros
```
