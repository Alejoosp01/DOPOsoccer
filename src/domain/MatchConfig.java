package domain;

/**
 * Configuracion de un partido: modalidad, tipo de partido y su valor
 * (minutos si es por tiempo, goles si es por goles).
 */
public class MatchConfig {

    public static final int MIN_MINUTES = 1;
    public static final int MAX_MINUTES = 10;
    public static final int MIN_GOALS = 1;
    public static final int MAX_GOALS = 10;

    private final GameMode gameMode;
    private final MatchType matchType;
    private final int minutes;
    private final int goalsTarget;

    private MatchConfig(GameMode gameMode, MatchType matchType, int minutes, int goalsTarget) {
        if (gameMode == null) {
            throw new IllegalArgumentException("La modalidad no puede ser null");
        }
        this.gameMode = gameMode;
        this.matchType = matchType;
        this.minutes = minutes;
        this.goalsTarget = goalsTarget;
    }

    /**
     * Partido por tiempo: dura los minutos indicados (1 a 10).
     */
    public static MatchConfig byTime(GameMode gameMode, int minutes) throws DOPOSoccerException {
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MINUTES);
        }
        return new MatchConfig(gameMode, MatchType.TIME, minutes, 0);
    }

    /**
     * Partido por goles: gana el primero que llegue a los goles indicados (1 a 10).
     */
    public static MatchConfig byGoals(GameMode gameMode, int goals) throws DOPOSoccerException {
        if (goals < MIN_GOALS || goals > MAX_GOALS) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_GOALS);
        }
        return new MatchConfig(gameMode, MatchType.GOALS, 0, goals);
    }

    /**
     * Devuelve la modalidad.
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Devuelve el tipo de partido.
     */
    public MatchType getMatchType() {
        return matchType;
    }

    /**
     * Devuelve los minutos del partido (0 si es por goles).
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Devuelve los goles para ganar (0 si es por tiempo).
     */
    public int getGoalsTarget() {
        return goalsTarget;
    }

    /**
     * Devuelve la duracion en segundos (0 si es por goles).
     */
    public double getDurationSeconds() {
        return minutes * 60.0;
    }

    /**
     * Devuelve si el partido es por tiempo.
     */
    public boolean isTimed() {
        return matchType == MatchType.TIME;
    }
}
