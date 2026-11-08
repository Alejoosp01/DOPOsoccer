package domain;

/**
 * Ilusion (Estados Unidos): el balon se divide en 8, de los cuales 7 son falsos.
 * Mientras dura la ilusion el balon real atraviesa al rival.
 */
public class IllusionPower extends Power {

    public static final int DECOYS = 7;
    public static final double SHOT_SPEED = 700;

    public IllusionPower() {
        super("Ilusion", 1.5);
    }

    /**
     * Dispara el balon en linea recta hacia el arco rival y crea las copias falsas.
     */
    @Override
    public void activate(Ball ball, Player owner, Player opponent) {
        ball.setVelocity(owner.getAttackDirection() * SHOT_SPEED, 0);
        ball.startIllusion(DECOYS, getDuration());
    }
}
