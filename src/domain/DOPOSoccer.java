package domain;

/**
 * Fachada del juego: es la unica clase del dominio que necesita la interfaz.
 * Une la configuracion de una partida con el motor y oculta si se juega un
 * partido unico o una serie de supervivencia, de modo que la interfaz
 * siempre use los mismos metodos.
 */
public class DOPOSoccer {

    private final MatchConfig config;
    private final Field field;
    private final GameCharacter leftCharacter;
    private final GameCharacter rightCharacter;
    private final Player leftPlayer;
    private final Player rightPlayer;
    private final MatchRunner runner;
    private final SurvivalSeries series;

    private boolean started;
    private boolean paused;

    public DOPOSoccer(MatchConfig config, GameCharacter leftCharacter, GameCharacter rightCharacter) {
        this(config, new Field(), leftCharacter, rightCharacter);
    }

    public DOPOSoccer(MatchConfig config, Field field,
                      GameCharacter leftCharacter, GameCharacter rightCharacter) {
        if (config == null) {
            throw new IllegalArgumentException("La configuracion no puede ser null");
        }
        if (field == null) {
            throw new IllegalArgumentException("La cancha no puede ser null");
        }
        if (leftCharacter == null || rightCharacter == null) {
            throw new IllegalArgumentException("Los personajes no pueden ser null");
        }
        this.config = config;
        this.field = field;
        this.leftCharacter = leftCharacter;
        this.rightCharacter = rightCharacter;
        this.leftPlayer = new Player(leftCharacter, true, 0, 0);
        this.rightPlayer = new Player(rightCharacter, false, 0, 0);
        if (config.getMatchType() == MatchType.SURVIVAL) {
            this.series = new SurvivalSeries(config, field, leftPlayer, rightPlayer);
            this.runner = null;
        } else {
            Match match = new Match(config, field, leftPlayer, rightPlayer);
            this.runner = MatchRunner.forMatch(match);
            this.series = null;
        }
        this.started = false;
        this.paused = false;
    }

    /**
     * Inicia la partida o la primera ronda de la serie.
     */
    public void start() {
        if (series != null) {
            series.start();
        } else {
            runner.start();
        }
        started = true;
        paused = false;
    }

    /**
     * Avanza el motor de juego un instante. dt esta en segundos.
     */
    public void update(double dt) {
        if (!started || paused || dt <= 0) {
            return;
        }
        if (series != null) {
            series.update(dt);
        } else {
            runner.update(dt);
        }
    }

    /**
     * Pausa la partida actual. Devuelve false si no se puede pausar.
     */
    public boolean pause() {
        if (!started || paused || isFinished()) {
            return false;
        }
        Match match = getMatch();
        if (match == null) {
            return false;
        }
        MatchState state = match.getState();
        if (state != MatchState.PLAYING && state != MatchState.GOAL) {
            return false;
        }
        match.pause();
        paused = true;
        return true;
    }

    /**
     * Reanuda la partida pausada.
     */
    public boolean resume() {
        if (!paused) {
            return false;
        }
        Match match = getMatch();
        if (match != null) {
            match.resume();
        }
        paused = false;
        return true;
    }

    /**
     * Alterna entre pausa y juego. Devuelve el estado de pausa resultante.
     */
    public boolean togglePause() {
        if (paused) {
            resume();
        } else {
            pause();
        }
        return paused;
    }

    /**
     * Devuelve el partido que se esta jugando ahora mismo.
     */
    public Match getMatch() {
        if (series != null) {
            return series.getCurrentMatch();
        }
        if (runner == null) {
            return null;
        }
        return runner.getMatch();
    }

    /**
     * Devuelve el marcador del partido actual, o null si no hay partido.
     */
    public Score getScore() {
        Match match = getMatch();
        if (match == null) {
            return null;
        }
        return match.getScore();
    }

    /**
     * Devuelve el estado del partido actual.
     */
    public MatchState getState() {
        Match match = getMatch();
        if (match == null) {
            return MatchState.READY;
        }
        return match.getState();
    }

    /**
     * Devuelve el tiempo restante del partido actual.
     */
    public double getTimeLeft() {
        Match match = getMatch();
        if (match == null) {
            return 0;
        }
        return match.getTimeLeft();
    }

    /**
     * Devuelve los balones que le quedan a un lado en supervivencia.
     */
    public int getRemainingBalls(Side side) {
        Match match = getMatch();
        if (match == null) {
            return 0;
        }
        return match.getRemainingBalls(side);
    }

    /**
     * Devuelve si la partida ya termino.
     */
    public boolean isFinished() {
        if (!started) {
            return false;
        }
        if (series != null) {
            return series.isFinished();
        }
        return getMatch() != null && getMatch().isFinished();
    }

    /**
     * Devuelve el ganador de la partida o de la serie completa.
     */
    public Winner getWinner() {
        if (series != null) {
            return series.getSeriesWinner();
        }
        Match match = getMatch();
        if (match == null) {
            return Winner.NONE;
        }
        return match.getWinner();
    }

    /**
     * Devuelve si la partida es una serie de supervivencia.
     */
    public boolean isSurvival() {
        return series != null;
    }

    /**
     * Devuelve las rondas ganadas en supervivencia (0 si no es supervivencia).
     */
    public int getRoundsWon() {
        if (series == null) {
            return 0;
        }
        return series.getRoundsWon();
    }

    /**
     * Devuelve las rondas jugadas en supervivencia (0 si no es supervivencia).
     */
    public int getRoundsPlayed() {
        if (series == null) {
            return 0;
        }
        return series.getRoundsPlayed();
    }

    /**
     * Devuelve los balones con que empieza cada lado en supervivencia.
     */
    public int getBallsPerSide() {
        return config.getBallsPerSide();
    }

    /**
     * Devuelve el controlador del lado izquierdo (humano o maquina).
     */
    public PlayerController getLeftController() {
        if (series != null) {
            return series.getLeftController();
        }
        return runner.getLeftController();
    }

    /**
     * Devuelve el controlador del lado derecho (humano o maquina).
     */
    public PlayerController getRightController() {
        if (series != null) {
            return series.getRightController();
        }
        return runner.getRightController();
    }

    /**
     * Devuelve la configuracion de la partida.
     */
    public MatchConfig getConfig() {
        return config;
    }

    /**
     * Devuelve la cancha.
     */
    public Field getField() {
        return field;
    }

    /**
     * Devuelve el personaje del lado izquierdo.
     */
    public GameCharacter getLeftCharacter() {
        return leftCharacter;
    }

    /**
     * Devuelve el personaje del lado derecho.
     */
    public GameCharacter getRightCharacter() {
        return rightCharacter;
    }

    /**
     * Devuelve el jugador del lado izquierdo.
     */
    public Player getLeftPlayer() {
        return leftPlayer;
    }

    /**
     * Devuelve el jugador del lado derecho.
     */
    public Player getRightPlayer() {
        return rightPlayer;
    }

    /**
     * Devuelve si la partida esta pausada.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Devuelve si la partida ya empezo.
     */
    public boolean isStarted() {
        return started;
    }
}
