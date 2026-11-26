# Pruebas de aceptación: DOPOSoccer

Checklist de pruebas manuales de punta a punta en la GUI. Marca cada caso con `[x]` cuando pase.

**Preparación:** compilar y ejecutar (ver `README.md`). Para empezar sin progreso, borrar
`dopo_progress.dat` de la carpeta desde donde se ejecuta el juego.

**Controles de referencia**

| Acción | Jugador 1 | Jugador 2 |
|---|---|---|
| Mover | `A` / `D` | `←` / `→` |
| Saltar | `W` | `↑` |
| Patear | `S` | `↓` |
| Poder | `Q` | `Ctrl` derecho |

Generales: `Esc` o `P` pausa · `G` guardar partida · `T` terminar ya · `M` menú · `R` o `Enter` revancha.

---

## 1. Configuración en 2 pantallas

| # | Pasos | Resultado esperado |
|---|---|---|
| 1.1 | Abrir el juego. | Menú con **JUGAR**, **ABRIR PARTIDA**, **COMO JUGAR** y **SALIR**. |
| 1.2 | Pulsar **COMO JUGAR**. | Se abre el diálogo de ayuda; al cerrarlo se vuelve al menú. |
| 1.3 | Pulsar **JUGAR**. | Pantalla "Paso 1 de 2: Configuracion del partido". Valores por defecto: *Jugador vs Jugador*, *Por tiempo*, 2 minutos. |
| 1.4 | Cambiar a **Por goles** y usar `[-]` / `[+]`. | El valor pasa a goles (por defecto 3) y no baja de 1 ni sube de 10. Igual con minutos. |
| 1.5 | Elegir otra resolución y pulsar **SIGUIENTE**. | Pantalla "Paso 2 de 2: Seleccion de equipos" con columnas JUGADOR 1 y JUGADOR 2. |
| 1.6 | Pulsar **ATRAS**. | Vuelve al paso 1 con lo elegido conservado. |
| 1.7 | En el paso 2 pulsar **COMENZAR** sin elegir personaje en alguna columna. | Aviso "Elige un personaje"; no empieza el partido. |
| 1.8 | Elegir un personaje por jugador y pulsar **COMENZAR**. | Empieza el partido con los personajes, el modo y la resolución elegidos. |
| 1.9 | En el paso 1 pulsar **VOLVER**. | Regresa al menú principal. |

## 2. Modos PVP y PVM

| # | Pasos | Resultado esperado |
|---|---|---|
| 2.1 | En el paso 1 dejar **Jugador vs Jugador**. | La fila de chips de la máquina **no se ve** y la etiqueta "Maquina" desaparece. |
| 2.2 | Cambiar a **Jugador vs Maquina**. | Aparecen los chips **Torpe**, **Ofensiva**, **Defensiva** y **Experta** (seleccionada por defecto) con su descripción debajo. |
| 2.3 | Pulsar cada chip. | Se marca solo el chip pulsado y cambia la descripción. |
| 2.4 | Volver a **Jugador vs Jugador**. | Los chips se ocultan de nuevo. |
| 2.5 | Jugar un partido **PVP**. | El jugador 1 se controla con `A/D/W/S/Q` y el 2 con las flechas y `Ctrl` derecho. |
| 2.6 | Jugar un partido **PVM** con cada perfil. | El jugador 1 es humano; el 2 lo mueve la máquina (se mueve, patea y usa el poder sola). Las flechas no lo controlan. *Torpe* falla más; *Ofensiva* ataca más; *Defensiva* protege su arco. |

## 3. Patada (`S` / `↓`)

| # | Pasos | Resultado esperado |
|---|---|---|
| 3.1 | Con el balón **cerca y delante** del jugador, pulsar patear. | El balón sale disparado hacia delante y algo hacia arriba. |
| 3.2 | Saltar y pulsar patear **en el aire** con el balón cerca. | También patea: no hace falta estar en el suelo. |
| 3.3 | Pulsar patear con el balón **lejos**. | El balón no se mueve. |
| 3.4 | Pulsar patear muy seguido. | Entre una patada y otra hay una espera corta (0,25 s), aunque la patada falle. |
| 3.5 | Patear con el rival justo delante. | El rival recibe daño (baja su vida). |

## 4. Mejoras con puntos y su persistencia

| # | Pasos | Resultado esperado |
|---|---|---|
| 4.1 | Con progreso vacío, ir al paso 2. | Los botones `+` de SPD, KCK, JMP y SRV están deshabilitados (no hay puntos). |
| 4.2 | Ganar un partido y mirar la pantalla final. | El ganador ve "+100 puntos (total N)"; el perdedor gana 25. |
| 4.3 | Terminar un partido empatado con `T`. | "TERMINADO SIN GANADOR" y 0 puntos para ambos. |
| 4.4 | Acumular 500 puntos y comprar una mejora en el paso 2. | El nivel sube a 2, se descuentan 500 puntos y la siguiente mejora de esa estadística cuesta 1000. |
| 4.5 | **Al comprar:** comprar una mejora y cerrar la ventana de inmediato con la X; volver a abrir. | La mejora y los puntos restantes siguen ahí (se guardó al comprar). |
| 4.6 | **Al salir:** comprar, volver al menú y pulsar **SALIR**; volver a abrir. | El progreso se conserva. |
| 4.7 | **Con `G`:** en un partido sin terminar pulsar `G`, guardar, terminar el partido y salir; volver a abrir. | El `.dopo` guarda la partida; las mejoras y puntos se conservan porque se guardan al terminar y al salir, no por `G`. |
| 4.8 | Comprar una mejora de SPD/JMP y jugar. | El jugador corre o salta más (+10 % por nivel). KCK aumenta el daño y SRV la vida. |

## 5. Guardar y abrir partida (`G`)

| # | Pasos | Resultado esperado |
|---|---|---|
| 5.1 | En pleno partido pulsar `G`. | El juego se pausa y se abre "Guardar partida" con el nombre `partida.dopo`. |
| 5.2 | Guardar con un nombre sin extensión. | Se crea el archivo con `.dopo` y sale "Partida guardada en: <ruta>". El juego queda en pausa. |
| 5.3 | Pulsar `G` con el partido terminado. | Aviso; no se guarda. |
| 5.4 | Desde el menú, **ABRIR PARTIDA** y elegir el `.dopo`. | Se recupera el marcador, el tiempo, los personajes y las posiciones; la partida abre en pausa, con la resolución de la sesión actual (Mediana por defecto). |
| 5.5 | Intentar abrir un archivo que no es una partida. | Diálogo "No se pudo abrir"; el juego sigue en el menú. |

## 6. Partido: goles, poder y final

| # | Pasos | Resultado esperado |
|---|---|---|
| 6.1 | Meter un gol. | Banner "GOL DE <NOMBRE>", pausa de 1 s y los jugadores vuelven a sus posiciones. |
| 6.2 | Esperar 10 s de juego. | La barra de poder se llena. Cada gol recibido le suma un 25 % extra. |
| 6.3 | Con la barra llena pulsar poder y luego patear o tocar el balón. | Se ve el aura, la barra se vacía y sale el tiro de poder; si da al rival, le aplica el efecto de su personaje (Empuje, Electrocutado, Inconsciente o Confusión). |
| 6.4 | Pulsar `Esc` o `P`. | Overlay "PAUSA" con **SEGUIR** y **MENU**; todo se congela. Volver a pulsar reanuda. |
| 6.5 | Partido **por goles**: llegar a la meta. | Pantalla "GANA <PAIS>" con **REVANCHA** y **MENU**. |
| 6.6 | Partido **por tiempo** con empate al llegar a 0. | Muerte súbita ("M. SUBITA"): el siguiente gol gana. |
| 6.7 | Pulsar `R` o `Enter` en la pantalla final. | Empieza una revancha con la misma configuración. |
| 6.8 | Pulsar `M` a mitad de partido. | Vuelve al menú; el partido sin terminar se pierde y no da puntos. |
