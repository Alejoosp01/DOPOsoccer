package presentation;

import domain.GameCharacter;
import domain.MatchConfig;

/**
 * Datos elegidos en el menu de configuracion para comenzar una partida.
 */
public final class GameSetup {

    private final MatchConfig config;
    private final GameCharacter leftCharacter;
    private final GameCharacter rightCharacter;

    public GameSetup(MatchConfig config, GameCharacter leftCharacter, GameCharacter rightCharacter) {
        if (config == null || leftCharacter == null || rightCharacter == null) {
            throw new IllegalArgumentException("La configuracion y los personajes son obligatorios");
        }
        this.config = config;
        this.leftCharacter = leftCharacter;
        this.rightCharacter = rightCharacter;
    }

    public MatchConfig getConfig() {
        return config;
    }

    public GameCharacter getLeftCharacter() {
        return leftCharacter;
    }

    public GameCharacter getRightCharacter() {
        return rightCharacter;
    }
}
