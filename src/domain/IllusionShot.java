package domain;

/**
 * Ilusion (Estados Unidos): el balon se divide en 8 balones identicos;
 * 7 son falsos y desaparecen al terminar el efecto.
 * Efecto: Confusion durante 2.5 s. Daño 30.
 */
public class IllusionShot extends PowerShot {

    public static final double SHOT_SPEED = 900;
    public static final int DECOYS = 7;

    public IllusionShot() {
        super("Ilusion", 30, 2.5);
    }

    /**
     * Dispara el balon recto y crea los 7 balones falsos.
     */
    @Override
    public void launch(Ball ball, Player owner) {
        placeInFront(ball, owner);
        ball.setVelocity(owner.getAttackDirection() * SHOT_SPEED, 0);
        ball.startDecoys(DECOYS, getEffectDuration());
    }

    /**
     * Crea el efecto Confusion.
     */
    @Override
    public Effect createEffect() {
        return new ConfusionEffect(getEffectDuration());
    }
}
