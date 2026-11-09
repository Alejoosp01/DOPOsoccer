package domain;

/**
 * Empuje (tiro Aura Azul de Corea del Sur): el rival es lanzado hacia su
 * propio arco y mientras dura no puede moverse ni saltar.
 */
public class PushEffect extends Effect {

    public static final double PUSH_SPEED = 650;
    public static final double PUSH_LIFT = 250;

    public PushEffect(double duration) {
        super("Empuje", duration);
    }

    /**
     * Lanza al rival hacia su propio arco (contrario a su direccion de ataque).
     */
    @Override
    public void onStart(Player target) {
        target.launch(-target.getAttackDirection() * PUSH_SPEED, -PUSH_LIFT);
    }

    /**
     * Devuelve false: el rival va volando y no controla su movimiento.
     */
    @Override
    public boolean canMove() {
        return false;
    }

    /**
     * Devuelve false: no puede saltar mientras es empujado.
     */
    @Override
    public boolean canJump() {
        return false;
    }
}
