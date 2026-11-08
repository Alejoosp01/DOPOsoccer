package domain;

/**
 * Poder unico de un pais. Se activa al golpear el balon y afecta al oponente.
 * Cada subclase decide que hace en activate().
 */
public abstract class Power {

    private String name;
    private double duration;

    public Power(String name, double duration) {
        this.name = name;
        this.duration = duration;
    }

    /**
     * Ejecuta el poder en el momento en que owner golpea el balon.
     */
    public abstract void activate(Ball ball, Player owner, Player opponent);

    /**
     * Se llama cuando el rival toca un balon que quedo cargado por este poder.
     * Por defecto no hace nada; los poderes que dejan el balon cargado lo sobrescriben.
     */
    public void onBallTouch(Ball ball, Player owner, Player opponent) {
    }

    /**
     * Devuelve si este poder reacciona cuando el rival toca el balon cargado.
     */
    public boolean reactsToOpponentTouch() {
        return false;
    }

    /**
     * Devuelve el nombre del poder.
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve los segundos que dura el efecto del poder.
     */
    public double getDuration() {
        return duration;
    }
}
