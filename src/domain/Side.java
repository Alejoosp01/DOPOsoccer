package domain;

/**
 * Lado de la cancha que defiende un jugador.
 * LEFT es el lado izquierdo y RIGHT el derecho.
 */
public enum Side {

    LEFT,
    RIGHT;

    /**
     * Devuelve el lado contrario.
     */
    public Side opposite() {
        if (this == LEFT) {
            return RIGHT;
        }
        return LEFT;
    }
}
