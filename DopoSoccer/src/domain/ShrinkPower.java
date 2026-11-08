package domain;

/**
 * Reduccion (Colombia): encoge al oponente a un tamano diminuto.
 */
public class ShrinkPower extends Power {

    public ShrinkPower() {
        super("Reduccion", 5);
    }

    /**
     * Encoge al oponente durante la duracion del poder.
     */
    @Override
    public void activate(Ball ball, Player owner, Player opponent) {
        opponent.shrink(getDuration());
    }
}
