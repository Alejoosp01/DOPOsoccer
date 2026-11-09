package domain;

/**
 * Efecto que sufre un jugador cuando lo golpea un tiro de poder.
 * Dura unos segundos. Cada subclase decide que le impide hacer al jugador.
 * Un jugador solo tiene un efecto a la vez: uno nuevo reemplaza al anterior.
 */
public abstract class Effect {

    private final String name;
    private double timeLeft;

    public Effect(String name, double duration) {
        this.name = name;
        this.timeLeft = duration;
    }

    /**
     * Se ejecuta una vez cuando el efecto empieza. Por defecto no hace nada.
     */
    public void onStart(Player target) {
    }

    /**
     * Descuenta el tiempo del efecto. dt esta en segundos.
     */
    public void update(double dt) {
        timeLeft = Math.max(0, timeLeft - dt);
    }

    /**
     * Devuelve si el efecto ya termino.
     */
    public boolean isOver() {
        return timeLeft <= 0;
    }

    /**
     * Devuelve si el jugador puede moverse mientras dura el efecto.
     */
    public boolean canMove() {
        return true;
    }

    /**
     * Devuelve si el jugador puede saltar mientras dura el efecto.
     */
    public boolean canJump() {
        return true;
    }

    /**
     * Devuelve si el jugador puede patear mientras dura el efecto.
     */
    public boolean canKick() {
        return true;
    }

    /**
     * Devuelve el factor que multiplica la velocidad del jugador (1 = normal).
     */
    public double speedFactor() {
        return 1;
    }

    /**
     * Devuelve el nombre del efecto, por ejemplo "Electrocutado".
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve los segundos que le quedan al efecto.
     */
    public double getTimeLeft() {
        return timeLeft;
    }
}
