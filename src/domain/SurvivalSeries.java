package domain;

/**
 * Serie de partidos de supervivencia.
 * Cada partido empieza con la misma cantidad de balones por lado. El jugador
 * (lado izquierdo) avanza de ronda mientras gane; cuando pierde, la serie
 * termina. Se lleva el conteo de rondas jugadas y ganadas.
 */
public class SurvivalSeries {

    private final MatchConfig config;
    private final Field field;
    private final Player leftPlayer;
    private final Player rightPlayer;
    private final PlayerController leftController;
    private final PlayerController rightController;

    private MatchRunner runner;
    private int roundsWon;
    private int roundsPlayed;
    private boolean finished;
    private Winner seriesWinner;

    public SurvivalSeries(MatchConfig config, Field field, Player leftPlayer, Player rightPlayer) {
        this(config, field, leftPlayer, rightPlayer,
                new HumanController(), new MachineController());
    }

    public SurvivalSeries(MatchConfig config, Field field, Player leftPlayer, Player rightPlayer,
                          PlayerController leftController, PlayerController rightController) {
        if (config == null) {
            throw new IllegalArgumentException("La configuracion no puede ser null");
        }
        if (config.getMatchType() != MatchType.SURVIVAL) {
            throw new IllegalArgumentException("La serie debe ser de supervivencia");
        }
        if (field == null) {
            throw new IllegalArgumentException("La cancha no puede ser null");
        }
        if (leftPlayer == null || rightPlayer == null) {
            throw new IllegalArgumentException("Los jugadores no pueden ser null");
        }
        if (!leftPlayer.isLeftSide() || rightPlayer.isLeftSide()) {
            throw new IllegalArgumentException("Los jugadores deben estar en lados opuestos");
        }
        if (leftController == null || rightController == null) {
            throw new IllegalArgumentException("Los controladores no pueden ser null");
        }
        this.config = config;
        this.field = field;
        this.leftPlayer = leftPlayer;
        this.rightPlayer = rightPlayer;
        this.leftController = leftController;
        this.rightController = rightController;
        this.runner = null;
        this.roundsWon = 0;
        this.roundsPlayed = 0;
        this.finished = false;
        this.seriesWinner = Winner.NONE;
    }

    /**
     * Reinicia la serie y comienza la primera ronda.
     */
    public void start() {
        roundsWon = 0;
        roundsPlayed = 0;
        finished = false;
        seriesWinner = Winner.NONE;
        startNextMatch();
    }

    /**
     * Avanza la ronda actual. Si el partido termino, decide si el jugador
     * avanza a la siguiente ronda o si la serie termina.
     */
    public void update(double dt) {
        if (finished || runner == null || dt <= 0) {
            return;
        }
        runner.update(dt);
        Match match = runner.getMatch();
        if (!match.isFinished()) {
            return;
        }

        roundsPlayed++;
        Winner roundWinner = match.getWinner();
        if (roundWinner == Winner.LEFT) {
            roundsWon++;
            startNextMatch();
        } else {
            finished = true;
            seriesWinner = roundWinner;
        }
    }

    private void startNextMatch() {
        Match match = new Match(config, field, leftPlayer, rightPlayer);
        runner = new MatchRunner(match, leftController, rightController);
        runner.start();
    }

    /**
     * Devuelve el partido de la ronda actual, o null si la serie no ha empezado.
     */
    public Match getCurrentMatch() {
        if (runner == null) {
            return null;
        }
        return runner.getMatch();
    }

    public MatchRunner getRunner() {
        return runner;
    }

    public PlayerController getLeftController() {
        return leftController;
    }

    public PlayerController getRightController() {
        return rightController;
    }

    public MatchConfig getConfig() {
        return config;
    }

    public int getBallsPerSide() {
        return config.getBallsPerSide();
    }

    public int getRoundsWon() {
        return roundsWon;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public boolean isFinished() {
        return finished;
    }

    public Winner getSeriesWinner() {
        return seriesWinner;
    }
}
