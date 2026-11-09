package domain;

/**
 * Inconsciente (tiro Tornado de Nigeria): el rival es lanzado al aire y no
 * puede moverse, saltar ni patear.
 */
public class UnconsciousEffect extends Effect {

    public static final double LAUNCH_SPEED = 750;

    public UnconsciousEffect(double duration) {
        super("Inconsciente", duration);
    }

    /**
     * Lanza al rival hacia arriba.
     */
    @Override
    public void onStart(Player target) {
        target.launch(0, -LAUNCH_SPEED);
    }

    /**
     * Devuelve false: inconsciente no puede moverse.
     */
    @Override
    public boolean canMove() {
        return false;
    }

    /**
     * Devuelve false: inconsciente no puede saltar.
     */
    @Override
    public boolean canJump() {
        return false;
    }

    /**
     * Devuelve false: inconsciente no puede patear.
     */
    @Override
    public boolean canKick() {
        return false;
    }
}
