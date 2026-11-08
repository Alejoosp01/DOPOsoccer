package domain;

/**
 * Configuracion de un partido: modalidad, forma de terminar, duracion,
 * goles objetivo y balones de supervivencia.
 */
public class MatchConfig {

    public static final double DEFAULT_DURATION = 60;

    private final GameMode gameMode;
    private final MatchType matchType;
    private final double duration;
    private final int goalsTarget;
    private final int ballsPerSide;

    private MatchConfig(GameMode gameMode, MatchType matchType, double duration,
                        int goalsTarget, int ballsPerSide) {
        if (gameMode == null) {
            throw new IllegalArgumentException("La modalidad no puede ser null");
        }
        this.gameMode = gameMode;
        this.matchType = matchType;
        this.duration = duration;
        this.goalsTarget = goalsTarget;
        this.ballsPerSide = ballsPerSide;
    }

    /**
     * Partido por tiempo con la duracion por defecto (60 segundos).
     */
    public static MatchConfig time(GameMode gameMode) {
        return time(gameMode, DEFAULT_DURATION);
    }

    /**
     * Partido por tiempo con una duracion en segundos.
     */
    public static MatchConfig time(GameMode gameMode, double duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("La duracion debe ser positiva");
        }
        return new MatchConfig(gameMode, MatchType.TIME, duration, 0, 0);
    }

    /**
     * Partido a goles: gana quien llegue primero al objetivo.
     */
    public static MatchConfig goals(GameMode gameMode, int target) {
        if (!isValidGoalTarget(target)) {
            throw new IllegalArgumentException("El objetivo de goles debe ser 3, 5 o 7");
        }
        return new MatchConfig(gameMode, MatchType.GOALS, 0, target, 0);
    }

    /**
     * Devuelve si la meta de goles es una de las que permite el enunciado: 3, 5 o 7.
     */
    public static boolean isValidGoalTarget(int target) {
        return target == 3 || target == 5 || target == 7;
    }

    /**
     * Partido de supervivencia: cada lado empieza con la cantidad de balones
     * indicada y pierde uno cada vez que recibe un gol.
     */
    public static MatchConfig survival(int balls) {
        if (balls < 1) {
            throw new IllegalArgumentException("La cantidad de balones debe ser positiva");
        }
        return new MatchConfig(GameMode.PVM, MatchType.SURVIVAL, 0, 0, balls);
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public double getDuration() {
        return duration;
    }

    public int getGoalsTarget() {
        return goalsTarget;
    }

    public int getBallsPerSide() {
        return ballsPerSide;
    }

    public boolean isTimed() {
        return matchType == MatchType.TIME;
    }
}
