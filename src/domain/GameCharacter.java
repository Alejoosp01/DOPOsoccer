package domain;

/**
 * Personaje seleccionable: representa a un pais y tiene un tiro de poder unico.
 * Las estadisticas NO son del personaje sino del jugador (ver Stats).
 * Se llama GameCharacter y no Character para no chocar con java.lang.Character.
 */
public class GameCharacter {

    private final String name;
    private final PowerShot powerShot;

    public GameCharacter(String name, PowerShot powerShot) throws DOPOSoccerException {
        if (name == null || name.trim().isEmpty()) {
            throw new DOPOSoccerException(DOPOSoccerException.EMPTY_NAME);
        }
        if (powerShot == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NO_POWER_SHOT);
        }
        this.name = name;
        this.powerShot = powerShot;
    }

    /**
     * Devuelve el nombre del personaje (su pais).
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el tiro de poder del personaje.
     */
    public PowerShot getPowerShot() {
        return powerShot;
    }
}
