package domain;

/**
 * Personaje seleccionable: nombre, pais y estadisticas de 1 a 5.
 * Se llama GameCharacter y no Character para no chocar con java.lang.Character.
 */
public class GameCharacter {

    public static final int MIN_STAT = 1;
    public static final int MAX_STAT = 5;

    private String name;
    private Country country;
    private int speed;
    private int jump;
    private int strength;

    public GameCharacter(String name, Country country, int speed, int jump, int strength) throws DopoSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new DopoSoccerException(DopoSoccerException.EMPTY_NAME);
        }
        if (country == null) {
            throw new DopoSoccerException(DopoSoccerException.NO_COUNTRY);
        }
        if (!isValidStat(speed) || !isValidStat(jump) || !isValidStat(strength)) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_STAT);
        }
        this.name = name;
        this.country = country;
        this.speed = speed;
        this.jump = jump;
        this.strength = strength;
    }

    /**
     * Devuelve si una estadistica esta entre 1 y 5.
     */
    private boolean isValidStat(int value) {
        return value >= MIN_STAT && value <= MAX_STAT;
    }

    /**
     * Devuelve el nombre del personaje.
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el pais del personaje.
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Devuelve el poder del personaje, que es el de su pais.
     */
    public Power getPower() {
        return country.getPower();
    }

    /**
     * Devuelve la velocidad del personaje.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Devuelve el salto del personaje.
     */
    public int getJump() {
        return jump;
    }

    /**
     * Devuelve la fuerza del personaje.
     */
    public int getStrength() {
        return strength;
    }
}
