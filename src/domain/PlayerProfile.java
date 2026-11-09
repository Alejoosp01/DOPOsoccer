package domain;

/**
 * El jugador como persona: nombre, estadisticas y puntos acumulados.
 * Se conserva entre partidos (y en la Version dos se guardara en archivo).
 */
public class PlayerProfile {

    private final String name;
    private final Stats stats;
    private int points;

    public PlayerProfile(String name) throws DOPOSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new DOPOSoccerException(DOPOSoccerException.EMPTY_NAME);
        }
        this.name = name;
        this.stats = new Stats();
        this.points = 0;
    }

    /**
     * Suma puntos ganados en un partido.
     */
    public void addPoints(int amount) {
        if (amount > 0) {
            points = points + amount;
        }
    }

    /**
     * Compra un punto de mejora en una estadistica, si alcanzan los puntos.
     */
    public void upgrade(StatType type) throws DOPOSoccerException {
        int cost = getUpgradeCost(type);
        if (stats.get(type) >= Stats.MAX) {
            throw new DOPOSoccerException(DOPOSoccerException.STAT_AT_MAX);
        }
        if (points < cost) {
            throw new DOPOSoccerException(DOPOSoccerException.NOT_ENOUGH_POINTS);
        }
        stats.increase(type);
        points = points - cost;
    }

    /**
     * Devuelve cuanto cuesta subir un punto la estadistica indicada.
     */
    public int getUpgradeCost(StatType type) {
        return stats.getUpgradeCost(type);
    }

    /**
     * Devuelve el nombre del jugador.
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve las estadisticas del jugador.
     */
    public Stats getStats() {
        return stats;
    }

    /**
     * Devuelve los puntos acumulados.
     */
    public int getPoints() {
        return points;
    }
}
