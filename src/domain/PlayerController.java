package domain;

import java.io.Serializable;

/**
 * Controla las acciones de un jugador dentro de un partido.
 * Puede ser humano o maquina.
 */
public interface PlayerController extends Serializable {

    /**
     * Decide y ejecuta las acciones del lado indicado en este instante.
     */
    void update(Match match, Side side, double dt);

    /**
     * Olvida las ordenes pendientes (al pausar, al abrir una partida o al reiniciar).
     */
    void clearInput();
}
