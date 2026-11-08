package domain;

/**
 * Ganador de un partido. NONE indica que todavia no hay ganador.
 */
public enum Winner {

    LEFT,
    RIGHT,
    NONE;

    /**
     * Traduce un lado a un ganador. Si el lado es null devuelve NONE.
     */
    public static Winner fromSide(Side side) {
        if (side == Side.LEFT) {
            return LEFT;
        }
        if (side == Side.RIGHT) {
            return RIGHT;
        }
        return NONE;
    }
}
