package domain;

import java.io.Serializable;

/**
 * Configuracion de un partido: modalidad, tipo de partido y su valor
 * (minutos si es por tiempo, goles si es por goles). En jugador vs maquina
 * tambien guarda el perfil de la maquina.
 */
public class MatchConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MIN_MINUTES = 1;
    public static final int MAX_MINUTES = 10;
    public static final int MIN_GOALS = 1;
    public static final int MAX_GOALS = 10;
    public static final MachineProfile DEFAULT_MACHINE = MachineProfile.EXPERT;

    private final GameMode gameMode;
    private final MatchType matchType;
    private final int minutes;
    private final int goalsTarget;
    private final MachineProfile machineProfile;

    private MatchConfig(GameMode gameMode, MatchType matchType, int minutes, int goalsTarget,
                        MachineProfile machineProfile) {
        this.gameMode = gameMode;
        this.matchType = matchType;
        this.minutes = minutes;
        this.goalsTarget = goalsTarget;
        this.machineProfile = machineProfile;
    }

    /**
     * Partido por tiempo: dura los minutos indicados (1 a 10). En jugador vs maquina usa la maquina experta.
     */
    public static MatchConfig byTime(GameMode gameMode, int minutes) throws DOPOSoccerException {
        return byTime(gameMode, minutes, DEFAULT_MACHINE);
    }

    /**
     * Partido por tiempo con el perfil de maquina indicado (solo se usa en jugador vs maquina).
     */
    public static MatchConfig byTime(GameMode gameMode, int minutes, MachineProfile machine)
            throws DOPOSoccerException {
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MINUTES);
        }
        return new MatchConfig(gameMode, MatchType.TIME, minutes, 0, checkMachine(gameMode, machine));
    }

    /**
     * Partido por goles: gana el primero que llegue a los goles indicados (1 a 10). En jugador vs maquina usa la maquina experta.
     */
    public static MatchConfig byGoals(GameMode gameMode, int goals) throws DOPOSoccerException {
        return byGoals(gameMode, goals, DEFAULT_MACHINE);
    }

    /**
     * Partido por goles con el perfil de maquina indicado (solo se usa en jugador vs maquina).
     */
    public static MatchConfig byGoals(GameMode gameMode, int goals, MachineProfile machine)
            throws DOPOSoccerException {
        if (goals < MIN_GOALS || goals > MAX_GOALS) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_GOALS);
        }
        return new MatchConfig(gameMode, MatchType.GOALS, 0, goals, checkMachine(gameMode, machine));
    }

    /**
     * Valida la modalidad y devuelve el perfil de maquina que corresponde (null en jugador vs jugador).
     */
    private static MachineProfile checkMachine(GameMode gameMode, MachineProfile machine)
            throws DOPOSoccerException {
        if (gameMode == null) {
            throw new IllegalArgumentException("La modalidad no puede ser null");
        }
        if (gameMode == GameMode.PVP) {
            return null;
        }
        if (machine == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NO_MACHINE_PROFILE);
        }
        return machine;
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
     * Devuelve el perfil de la maquina (null en jugador vs jugador).
     */
    public MachineProfile getMachineProfile() {
        return machineProfile;
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
