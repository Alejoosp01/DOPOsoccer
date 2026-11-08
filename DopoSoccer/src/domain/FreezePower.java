package domain;

/**
 * Congelacion (Canada): congela al oponente en un bloque de hielo.
 */
public class FreezePower extends Power {

    public FreezePower() {
        super("Congelacion", 3);
    }

    /**
     * Congela al oponente durante la duracion del poder.
     */
    @Override
    public void activate(Ball ball, Player owner, Player opponent) {
        opponent.freeze(getDuration());
    }
}
