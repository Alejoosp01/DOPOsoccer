package domain;

/**
 * Pais (equipo) de un personaje. El poder depende del pais.
 */
public class Country {

    private String name;
    private Power power;

    public Country(String name, Power power) throws DopoSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new DopoSoccerException(DopoSoccerException.EMPTY_NAME);
        }
        if (power == null) {
            throw new DopoSoccerException(DopoSoccerException.NO_POWER);
        }
        this.name = name;
        this.power = power;
    }

    /**
     * Devuelve el nombre del pais.
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el poder del pais.
     */
    public Power getPower() {
        return power;
    }
}
