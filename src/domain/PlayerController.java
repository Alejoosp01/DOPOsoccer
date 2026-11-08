package domain;

/**
 * Controla las acciones de un jugador dentro de un partido.
 * Puede ser humano o maquina.
 */
public interface PlayerController {

    /**
     * Decide y ejecuta las acciones del lado indicado en este instante.
     */
    void update(Match match, Side side, double dt);
}
