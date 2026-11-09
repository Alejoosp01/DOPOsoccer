package domain;

/**
 * Tornado (Nigeria): remolino a ras de suelo que rebota rapidamente.
 * Efecto: Inconsciente durante 3 s. Daño 40.
 */
public class TornadoShot extends PowerShot {

    public static final double SHOT_SPEED = 850;
    public static final double BOUNCE_SPEED = 350;

    public TornadoShot() {
        super("Tornado", 40, 3);
    }

    /**
     * Baja el balon al suelo y lo lanza rebotando hacia el arco rival.
     */
    @Override
    public void launch(Ball ball, Player owner) {
        placeInFront(ball, owner);
        double groundY = owner.getY() + owner.getHeight() / 2;
        ball.setPosition(ball.getX(), groundY - ball.getRadius());
        ball.setVelocity(owner.getAttackDirection() * SHOT_SPEED, -BOUNCE_SPEED);
    }

    /**
     * Devuelve true: el tornado cae y rebota contra el suelo.
     */
    @Override
    public boolean isAffectedByGravity() {
        return true;
    }

    /**
     * Crea el efecto Inconsciente.
     */
    @Override
    public Effect createEffect() {
        return new UnconsciousEffect(getEffectDuration());
    }
}
