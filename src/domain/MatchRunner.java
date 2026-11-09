package domain;

/**
 * Une un partido con los controladores de cada lado.
 * El runner actualiza primero las decisiones y despues la fisica del partido.
 */
public class MatchRunner {

    private final Match match;
    private final PlayerController leftController;
    private final PlayerController rightController;

    public MatchRunner(Match match, PlayerController leftController, PlayerController rightController) {
        if (match == null) {
            throw new IllegalArgumentException("El partido no puede ser null");
        }
        if (leftController == null || rightController == null) {
            throw new IllegalArgumentException("Los controladores no pueden ser null");
        }
        this.match = match;
        this.leftController = leftController;
        this.rightController = rightController;
    }

    /**
     * Crea los controladores que corresponden a la modalidad del partido.
     * En PVM el humano controla el lado izquierdo y la maquina el derecho.
     */
    public static MatchRunner forMatch(Match match) {
        if (match == null) {
            throw new IllegalArgumentException("El partido no puede ser null");
        }
        GameMode mode = match.getConfig().getGameMode();
        switch (mode) {
            case PVP:
                return new MatchRunner(match, new HumanController(), new HumanController());
            default:
                return new MatchRunner(match, new HumanController(), new MachineController());
        }
    }

    /**
     * Actualiza los controladores y luego avanza el partido.
     */
    public void update(double dt) {
        if (dt <= 0) {
            return;
        }
        if (match.getState() == MatchState.PLAYING) {
            leftController.update(match, Side.LEFT, dt);
            rightController.update(match, Side.RIGHT, dt);
        }
        match.update(dt);
    }

    /**
     * Inicia el partido.
     */
    public void start() {
        match.start();
    }

    public Match getMatch() {
        return match;
    }

    public PlayerController getLeftController() {
        return leftController;
    }

    public PlayerController getRightController() {
        return rightController;
    }
}
