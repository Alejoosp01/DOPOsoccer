package presentation;

import domain.GameCharacter;
import domain.MatchConfig;

/**
 * Datos elegidos en la configuracion para comenzar una partida.
 */
public final class GameSetup {

    private final MatchConfig config;
    private final GameCharacter leftCharacter;
    private final GameCharacter rightCharacter;
    private final Resolution resolution;

    public GameSetup(MatchConfig config, GameCharacter leftCharacter, GameCharacter rightCharacter,
                     Resolution resolution) {
        if (config == null || leftCharacter == null || rightCharacter == null || resolution == null) {
            throw new IllegalArgumentException("La configuracion, los personajes y la resolucion son obligatorios");
        }
        this.config = config;
        this.leftCharacter = leftCharacter;
        this.rightCharacter = rightCharacter;
        this.resolution = resolution;
    }

    /**
     * Devuelve la configuracion del partido.
     */
    public MatchConfig getConfig() {
        return config;
    }

    /**
     * Devuelve el personaje del jugador 1.
     */
    public GameCharacter getLeftCharacter() {
        return leftCharacter;
    }

    /**
     * Devuelve el personaje del jugador 2.
     */
    public GameCharacter getRightCharacter() {
        return rightCharacter;
    }

    /**
     * Devuelve la resolucion elegida.
     */
    public Resolution getResolution() {
        return resolution;
    }
}
