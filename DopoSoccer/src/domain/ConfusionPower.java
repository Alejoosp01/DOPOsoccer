package domain;

/**
 * Confusion (Brasil): invierte los controles de movimiento del oponente.
 */
public class ConfusionPower extends Power {

    public ConfusionPower() {
        super("Confusion", 5);
    }

    /**
     * Invierte los controles del oponente durante la duracion del poder.
     */
    @Override
    public void activate(Ball ball, Player owner, Player opponent) {
        opponent.confuse(getDuration());
    }
}
