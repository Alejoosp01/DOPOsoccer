package domain;

/**
 * Marcador de un partido: goles del lado izquierdo y del derecho.
 */
public final class Score {

    private int leftGoals;
    private int rightGoals;

    public Score() {
        reset();
    }

    /**
     * Suma un gol al lado indicado.
     */
    public void score(Side side) {
        if (side == Side.LEFT) {
            leftGoals++;
        } else if (side == Side.RIGHT) {
            rightGoals++;
        } else {
            throw new IllegalArgumentException("El lado del gol no puede ser null");
        }
    }

    /**
     * Devuelve los goles del lado indicado.
     */
    public int getGoals(Side side) {
        if (side == Side.LEFT) {
            return leftGoals;
        }
        if (side == Side.RIGHT) {
            return rightGoals;
        }
        throw new IllegalArgumentException("El lado no puede ser null");
    }

    /**
     * Deja el marcador en cero.
     */
    public void reset() {
        leftGoals = 0;
        rightGoals = 0;
    }

    /**
     * Devuelve si ambos lados tienen los mismos goles.
     */
    public boolean isTie() {
        return leftGoals == rightGoals;
    }

    /**
     * Devuelve el lado que va ganando o NONE si estan empatados.
     */
    public Winner leader() {
        if (leftGoals > rightGoals) {
            return Winner.LEFT;
        }
        if (rightGoals > leftGoals) {
            return Winner.RIGHT;
        }
        return Winner.NONE;
    }

    public int getLeftGoals() {
        return leftGoals;
    }

    public int getRightGoals() {
        return rightGoals;
    }

    public int getTotalGoals() {
        return leftGoals + rightGoals;
    }
}
