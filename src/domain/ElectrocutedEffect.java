package domain;

/**
 * Electrocutado (tiro Rayo de Camerun): no puede saltar y se mueve al 50 %
 * de su velocidad.
 */
public class ElectrocutedEffect extends Effect {

    public static final double SPEED_FACTOR = 0.5;

    public ElectrocutedEffect(double duration) {
        super("Electrocutado", duration);
    }

    /**
     * Devuelve false: electrocutado no puede saltar.
     */
    @Override
    public boolean canJump() {
        return false;
    }

    /**
     * Devuelve 0.5: se mueve a la mitad de su velocidad.
     */
    @Override
    public double speedFactor() {
        return SPEED_FACTOR;
    }
}
