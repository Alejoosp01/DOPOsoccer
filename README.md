# DOPOSoccer

Futbol de cabezas 2D para el proyecto final de DOPO 2026-2 (Desarrollo Orientado
por Objetos), basado en Head Soccer. Esta dividido en un motor de dominio
(`src/domain`) independiente de la interfaz y una aplicacion de escritorio hecha
con Swing y Java2D (`src/presentation`).

Estado: **Version uno** (Presentacion + Dominio, nivel basico Jugador vs Jugador).

## Requisitos

- JDK 21 o superior.
- JUnit 5 solo para las pruebas (`lib/junit-platform-console-standalone-1.14.4.jar`).

## Como ejecutar

Desde la raiz del repositorio:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp out presentation.DOPOSoccerApp
```

En VS Code o Eclipse se ejecuta la clase `presentation.DOPOSoccerApp`.

## Como jugar

1. En el menu principal elige `JUGAR`.
2. Elige tipo de partido (por tiempo: 1 a 10 minutos; por goles: 1 a 10 goles),
   la resolucion de la cancha y el personaje de cada jugador.
3. Presiona `COMENZAR`.

### Controles

| Accion | Jugador 1 (izquierda) | Jugador 2 (derecha) |
|---|---|---|
| Mover | `A` / `D` | Flechas izquierda / derecha |
| Saltar | `W` | Flecha arriba |
| Patear | `S` | Flecha abajo |
| Poder | `Q` | `Ctrl derecho` |
| Pausa | `ESC` o `P` | `ESC` o `P` |
| Terminar el partido | `T` | `T` |
| Volver al menu | `M` | `M` |
| Revancha al terminar | `R` o `Enter` | `R` o `Enter` |

## Reglas implementadas

- **Por tiempo:** gana quien tenga mas goles; si empatan, muerte subita.
- **Por goles:** gana el primero que llegue a la meta.
- **Vida:** 100 x (1 + 0.10 x (Survival - 1)). Una patada que alcanza al rival le
  quita 10 x multiplicadorKick. Con vida 0 el jugador queda aturdido 3 segundos.
- **Poder:** la barra se llena en 10 s y sube 1/4 al recibir un gol. Con la barra
  llena se activa un aura; al tocar el balon sale el tiro de poder. Si golpea al
  rival hace daño x multiplicadorKick y deja su efecto.
- **Puntos:** 100 al ganador y 25 al perdedor.

| Pais | Tiro | Daño | Efecto |
|---|---|---|---|
| Corea del Sur | Aura Azul | 40 | Empuje 1.5 s |
| Camerun | Rayo | 60 | Electrocutado 3 s |
| Nigeria | Tornado | 40 | Inconsciente 3 s |
| Estados Unidos | Ilusion | 30 | Confusion 2.5 s |

## Pruebas

```powershell
javac -encoding UTF-8 -cp lib/junit-platform-console-standalone-1.14.4.jar -d out (Get-ChildItem src, test -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -jar lib/junit-platform-console-standalone-1.14.4.jar execute --class-path out --scan-class-path --details=summary
```

Resultado actual: **148 pruebas, 148 exitosas**.

## Estructura

```
src/domain/        Motor: fisica, reglas, jugadores, tiros de poder, efectos, excepciones
src/presentation/  Interfaz Swing/Java2D: menu, configuracion, juego y HUD
test/domain/       Pruebas del dominio
test/presentation/ Pruebas del teclado sin abrir ventana
lib/               JUnit 5
```

Los errores para programadores se registran en `dopo_errors.log`.
