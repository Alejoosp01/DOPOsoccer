package domain;

/**
 * Barra de poder: se llena en 10 segundos. Al recibir un gol sube un cuarto.
 * Al activar el poder se vacia y en ese mismo instante empieza a recargarse.
 */
public class PowerBar {

    public static final double FILL_SECONDS = 10;
    public static final double QUARTER = 0.25;

    private double charge;

    public PowerBar() {
        charge = 0;
    }

    /**
     * Carga la barra con el tiempo. dt esta en segundos.
     */
    public void update(double dt) {
        if (dt <= 0) {
            return;
        }
        charge = Math.min(1, charge + dt / FILL_SECONDS);
    }

    /**
     * Suma un cuarto de barra (se llama cuando el jugador recibe un gol).
     */
    public void addQuarter() {
        charge = Math.min(1, charge + QUARTER);
    }

    /**
     * Devuelve si la barra esta llena.
     */
    public boolean isFull() {
        return charge >= 1;
    }

    /**
     * Vacia la barra.
     */
    public void empty() {
        charge = 0;
    }

    /**
     * Devuelve la carga entre 0 y 1.
     */
    public double getCharge() {
        return charge;
    }
}
