package domain;

/**
 * Aura Azul (Corea del Sur): sale recto y horizontal a alta velocidad.
 * Efecto: Empuje durante 1.5 s. Daño 40.
 */
public class BlueAuraShot extends PowerShot {

    public static final double SHOT_SPEED = 1100;

    public BlueAuraShot() {
        super("Aura Azul", 40, 1.5);
    }

    /**
     * Dispara el balon recto y horizontal hacia el arco rival.
     */
    @Override
    public void launch(Ball ball, Player owner) {
        placeInFront(ball, owner);
        ball.setVelocity(owner.getAttackDirection() * SHOT_SPEED, 0);
    }

    /**
     * Crea el efecto Empuje.
     */
    @Override
    public Effect createEffect() {
        return new PushEffect(getEffectDuration());
    }
}
