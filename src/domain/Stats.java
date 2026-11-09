package domain;

/**
 * Estadisticas de un jugador: speed, kick, jump y survival, de 1 a 10.
 * Aplican a todos los personajes del jugador. Cada punto por encima de 1
 * suma un 10 % sobre el valor base de la estadistica.
 */
public class Stats {

    public static final int MIN = 1;
    public static final int MAX = 10;
    public static final double BONUS_PER_POINT = 0.10;
    public static final double BASE_LIFE = 100;
    public static final int FIRST_UPGRADE_COST = 500;

    private int speed;
    private int kick;
    private int jump;
    private int survival;

    public Stats() {
        speed = MIN;
        kick = MIN;
        jump = MIN;
        survival = MIN;
    }

    /**
     * Devuelve el valor (1 a 10) de una estadistica.
     */
    public int get(StatType type) {
        if (type == StatType.SPEED) {
            return speed;
        }
        if (type == StatType.KICK) {
            return kick;
        }
        if (type == StatType.JUMP) {
            return jump;
        }
        if (type == StatType.SURVIVAL) {
            return survival;
        }
        throw new IllegalArgumentException("El tipo de estadistica no puede ser null");
    }

    /**
     * Sube un punto una estadistica. No puede pasar de 10.
     */
    public void increase(StatType type) throws DOPOSoccerException {
        if (get(type) >= MAX) {
            throw new DOPOSoccerException(DOPOSoccerException.STAT_AT_MAX);
        }
        if (type == StatType.SPEED) {
            speed++;
        } else if (type == StatType.KICK) {
            kick++;
        } else if (type == StatType.JUMP) {
            jump++;
        } else {
            survival++;
        }
    }

    /**
     * Devuelve el multiplicador de una estadistica: 1 + 0.10 * (valor - 1).
     */
    public double multiplier(StatType type) {
        return 1 + BONUS_PER_POINT * (get(type) - 1);
    }

    /**
     * Devuelve el multiplicadorKick, que escala el daño que hace el jugador.
     */
    public double getKickMultiplier() {
        return multiplier(StatType.KICK);
    }

    /**
     * Devuelve la vida maxima: 100 * multiplicador de survival.
     */
    public double getMaxLife() {
        return BASE_LIFE * multiplier(StatType.SURVIVAL);
    }

    /**
     * Devuelve el precio de subir un punto la estadistica: 500, 1000, 2000...
     * Se cuenta por estadistica (decision pendiente de confirmar con el monitor).
     */
    public int getUpgradeCost(StatType type) {
        int cost = FIRST_UPGRADE_COST;
        for (int i = MIN; i < get(type); i++) {
            cost = cost * 2;
        }
        return cost;
    }
}
