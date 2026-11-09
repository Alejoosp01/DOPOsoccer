package domain;

/**
 * Datos de prueba compartidos: perfiles, personajes, jugadores y partidos.
 * No es una prueba; solo evita repetir la misma preparacion en cada clase.
 */
public final class TestData {

    private TestData() {
    }

    /**
     * Crea un perfil con todas las estadisticas en 1 y 0 puntos.
     */
    public static PlayerProfile profile(String name) {
        try {
            return new PlayerProfile(name);
        } catch (DOPOSoccerException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Crea un personaje con el tiro de poder indicado.
     */
    public static GameCharacter character(String name, PowerShot shot) {
        try {
            return new GameCharacter(name, shot);
        } catch (DOPOSoccerException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Crea un jugador de Camerun (tiro Rayo) en el lado indicado.
     */
    public static Player player(boolean leftSide) {
        return new Player(profile("P"), character("Camerun", new LightningShot()), leftSide);
    }

    /**
     * Crea una configuracion por tiempo.
     */
    public static MatchConfig byTime(GameMode mode, int minutes) {
        try {
            return MatchConfig.byTime(mode, minutes);
        } catch (DOPOSoccerException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Crea una configuracion por goles.
     */
    public static MatchConfig byGoals(GameMode mode, int goals) {
        try {
            return MatchConfig.byGoals(mode, goals);
        } catch (DOPOSoccerException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Crea un partido entre dos jugadores de Camerun.
     */
    public static Match match(MatchConfig config) {
        return new Match(config, player(true), player(false));
    }

    /**
     * Llena la barra de poder de un jugador.
     */
    public static void fillPowerBar(Player player) {
        player.getPowerBar().update(PowerBar.FILL_SECONDS);
    }
}
