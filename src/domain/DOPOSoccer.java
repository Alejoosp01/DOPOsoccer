package domain;

/**
 * Fachada del juego: es la unica clase del dominio que necesita la interfaz.
 * Crea el partido con la configuracion, los personajes y los perfiles de los
 * dos jugadores, lo avanza, lo pausa, lo termina y reparte los puntos.
 */
public class DOPOSoccer {

    public static final int WINNER_POINTS = 100;
    public static final int LOSER_POINTS = 25;

    private final MatchConfig config;
    private final Field field;
    private final PlayerProfile leftProfile;
    private final PlayerProfile rightProfile;
    private final Player leftPlayer;
    private final Player rightPlayer;
    private final MatchRunner runner;

    private boolean started;
    private boolean paused;
    private boolean pointsAwarded;
    private int leftPointsEarned;
    private int rightPointsEarned;

    public DOPOSoccer(MatchConfig config, GameCharacter leftCharacter, GameCharacter rightCharacter,
                      PlayerProfile leftProfile, PlayerProfile rightProfile) {
        if (config == null) {
            throw new IllegalArgumentException("La configuracion no puede ser null");
        }
        if (leftCharacter == null || rightCharacter == null) {
            throw new IllegalArgumentException("Los personajes no pueden ser null");
        }
        if (leftProfile == null || rightProfile == null) {
            throw new IllegalArgumentException("Los perfiles no pueden ser null");
        }
        this.config = config;
        this.field = new Field();
        this.leftProfile = leftProfile;
        this.rightProfile = rightProfile;
        this.leftPlayer = new Player(leftProfile, leftCharacter, true);
        this.rightPlayer = new Player(rightProfile, rightCharacter, false);
        Match match = new Match(config, field, leftPlayer, rightPlayer);
        this.runner = MatchRunner.forMatch(match);
        this.started = false;
        this.paused = false;
        this.pointsAwarded = false;
        this.leftPointsEarned = 0;
        this.rightPointsEarned = 0;
    }

    /**
     * Inicia el partido.
     */
    public void start() {
        runner.start();
        started = true;
        paused = false;
    }

    /**
     * Avanza el juego un instante. dt esta en segundos.
     * Cuando el partido termina, reparte los puntos (una sola vez).
     */
    public void update(double dt) {
        if (!started || paused || dt <= 0) {
            return;
        }
        runner.update(dt);
        if (isFinished()) {
            awardPoints();
        }
    }

    /**
     * Pausa el partido. Devuelve false si no se puede pausar.
     */
    public boolean pause() {
        if (!started || paused || isFinished()) {
            return false;
        }
        MatchState state = getMatch().getState();
        if (state != MatchState.PLAYING && state != MatchState.GOAL) {
            return false;
        }
        getMatch().pause();
        paused = true;
        return true;
    }

    /**
     * Reanuda el partido pausado.
     */
    public boolean resume() {
        if (!paused) {
            return false;
        }
        getMatch().resume();
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
     * Termina el partido en cualquier momento y reparte los puntos.
     */
    public void endMatch() {
        if (!started || isFinished()) {
            return;
        }
        if (paused) {
            resume();
        }
        getMatch().finishNow();
        awardPoints();
    }

    /**
     * Da 100 puntos al ganador y 25 al perdedor. Si no hay ganador (empate al
     * terminar antes de tiempo) no se dan puntos.
     */
    private void awardPoints() {
        if (pointsAwarded) {
            return;
        }
        pointsAwarded = true;
        Winner winner = getMatch().getWinner();
        if (winner == Winner.LEFT) {
            leftPointsEarned = WINNER_POINTS;
            rightPointsEarned = LOSER_POINTS;
        } else if (winner == Winner.RIGHT) {
            leftPointsEarned = LOSER_POINTS;
            rightPointsEarned = WINNER_POINTS;
        }
        leftProfile.addPoints(leftPointsEarned);
        rightProfile.addPoints(rightPointsEarned);
    }

    /**
     * Devuelve los puntos que gano un lado en este partido.
     */
    public int getPointsEarned(Side side) {
        if (side == Side.LEFT) {
            return leftPointsEarned;
        }
        return rightPointsEarned;
    }

    /**
     * Devuelve el partido.
     */
    public Match getMatch() {
        return runner.getMatch();
    }

    /**
     * Devuelve si el partido ya termino.
     */
    public boolean isFinished() {
        return started && getMatch().isFinished();
    }

    /**
     * Devuelve el ganador del partido, o NONE.
     */
    public Winner getWinner() {
        return getMatch().getWinner();
    }

    /**
     * Devuelve el controlador del lado izquierdo.
     */
    public PlayerController getLeftController() {
        return runner.getLeftController();
    }

    /**
     * Devuelve el controlador del lado derecho.
     */
    public PlayerController getRightController() {
        return runner.getRightController();
    }

    /**
     * Devuelve la configuracion.
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
     * Devuelve el perfil del jugador izquierdo.
     */
    public PlayerProfile getLeftProfile() {
        return leftProfile;
    }

    /**
     * Devuelve el perfil del jugador derecho.
     */
    public PlayerProfile getRightProfile() {
        return rightProfile;
    }

    /**
     * Devuelve el jugador izquierdo (en la cancha).
     */
    public Player getLeftPlayer() {
        return leftPlayer;
    }

    /**
     * Devuelve el jugador derecho (en la cancha).
     */
    public Player getRightPlayer() {
        return rightPlayer;
    }

    /**
     * Devuelve el personaje del lado izquierdo.
     */
    public GameCharacter getLeftCharacter() {
        return leftPlayer.getCharacter();
    }

    /**
     * Devuelve el personaje del lado derecho.
     */
    public GameCharacter getRightCharacter() {
        return rightPlayer.getCharacter();
    }

    /**
     * Devuelve si esta pausado.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Devuelve si ya empezo.
     */
    public boolean isStarted() {
        return started;
    }
}
