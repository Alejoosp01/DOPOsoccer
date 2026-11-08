package domain;

/**
 * Empuje (Corea del Sur): tiro horizontal muy rapido con aura azul.
 * El balon queda cargado; el rival que lo toque sale empujado y aturdido.
 */
public class PushPower extends Power {

    public static final double SHOT_SPEED = 900;
    public static final double PUSH_DISTANCE = 150;

    public PushPower() {
        super("Empuje", Player.STUN_SECONDS);
    }

    /**
     * Dispara el balon en linea recta hacia el arco rival y lo deja cargado con el Empuje.
     */
    @Override
    public void activate(Ball ball, Player owner, Player opponent) {
        ball.setVelocity(owner.getAttackDirection() * SHOT_SPEED, 0);
        ball.chargePush();
    }

    /**
     * Se llama cuando el rival toca el balon cargado.
     */
    public void hit(Ball ball, Player opponent) {
        if (ball.isPushCharged()) {
            opponent.pushBack(PUSH_DISTANCE);
            opponent.stun();
            ball.releasePush();
        }
    }
}
