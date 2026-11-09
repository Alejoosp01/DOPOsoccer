package domain;

/**
 * Tiro de poder unico de un personaje.
 * Cada subclase decide como sale el balon (launch) y que efecto le deja al
 * rival si lo golpea (createEffect). El daño se escala con el multiplicadorKick
 * de quien lo lanza: dañoPoder = dañoTiro * multiplicadorKick.
 */
public abstract class PowerShot {

    private final String name;
    private final int damage;
    private final double effectDuration;

    public PowerShot(String name, int damage, double effectDuration) {
        this.name = name;
        this.damage = damage;
        this.effectDuration = effectDuration;
    }

    /**
     * Le da al balon la trayectoria propia del tiro. owner es quien lo lanza.
     */
    public abstract void launch(Ball ball, Player owner);

    /**
     * Crea el efecto que sufre el rival cuando el tiro lo golpea.
     */
    public abstract Effect createEffect();

    /**
     * Aplica el daño y el efecto al rival golpeado por el tiro.
     */
    public void hit(Player rival, double kickMultiplier) {
        rival.receiveDamage(damage * kickMultiplier);
        rival.applyEffect(createEffect());
    }

    /**
     * Devuelve si el balon cae por la gravedad mientras dura el tiro.
     * Por defecto no: los tiros rectos van en linea recta.
     */
    public boolean isAffectedByGravity() {
        return false;
    }

    /**
     * Pone el balon justo delante de quien lanza, para que no lo vuelva a tocar.
     */
    protected void placeInFront(Ball ball, Player owner) {
        double front = owner.getWidth() / 2 + ball.getRadius() + 1;
        ball.setPosition(owner.getX() + owner.getAttackDirection() * front, ball.getY());
    }

    /**
     * Devuelve el nombre del tiro, por ejemplo "Rayo".
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el daño base del tiro (DañoTiro x 5 de la tabla del enunciado).
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Devuelve los segundos que dura el efecto sobre el rival.
     */
    public double getEffectDuration() {
        return effectDuration;
    }
}
