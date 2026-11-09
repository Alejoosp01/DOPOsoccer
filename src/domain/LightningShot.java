package domain;

/**
 * Rayo (Camerun): sale recto, cubierto de electricidad.
 * Efecto: Electrocutado durante 3 s. Daño 60.
 */
public class LightningShot extends PowerShot {

    public static final double SHOT_SPEED = 950;

    public LightningShot() {
        super("Rayo", 60, 3);
    }

    /**
     * Dispara el balon recto hacia el arco rival.
     */
    @Override
    public void launch(Ball ball, Player owner) {
        placeInFront(ball, owner);
        ball.setVelocity(owner.getAttackDirection() * SHOT_SPEED, 0);
    }

    /**
     * Crea el efecto Electrocutado.
     */
    @Override
    public Effect createEffect() {
        return new ElectrocutedEffect(getEffectDuration());
    }
}
