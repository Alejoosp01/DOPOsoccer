package presentation;

import domain.Field;
import domain.GameCharacter;
import domain.Match;
import domain.MatchConfig;
import domain.MatchRunner;
import domain.MatchState;
import domain.Player;
import domain.PlayerController;
import domain.Score;
import domain.Side;
import domain.SurvivalSeries;
import domain.MatchType;
import domain.Winner;

/**
 * Une la configuracion de una partida con el motor del dominio.
 * Oculta si la partida es un partido unico o una serie de supervivencia,
 * de modo que la interfaz siempre use la misma API.
 */
public class GameSession {

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

    public GameSession(MatchConfig config, GameCharacter leftCharacter, GameCharacter rightCharacter) {
        this(config, new Field(), leftCharacter, rightCharacter);
    }

    public GameSession(MatchConfig config, Field field,
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
     * Avanza el motor de juego un instante.
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
     * Devuelve el marcador del partido actual.
     */
    public Score getScore() {
        Match match = getMatch();
        return match == null ? null : match.getScore();
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
        return match == null ? 0 : match.getTimeLeft();
    }

    /**
     * Devuelve los balones que le quedan a un lado en supervivencia.
     */
    public int getRemainingBalls(Side side) {
        Match match = getMatch();
        return match == null ? 0 : match.getRemainingBalls(side);
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
        return match == null ? Winner.NONE : match.getWinner();
    }

    public boolean isSurvival() {
        return series != null;
    }

    public int getRoundsWon() {
        return series == null ? 0 : series.getRoundsWon();
    }

    public int getRoundsPlayed() {
        return series == null ? 0 : series.getRoundsPlayed();
    }

    public int getBallsPerSide() {
        return config.getBallsPerSide();
    }

    public PlayerController getLeftController() {
        if (series != null) {
            return series.getLeftController();
        }
        return runner.getLeftController();
    }

    public PlayerController getRightController() {
        if (series != null) {
            return series.getRightController();
        }
        return runner.getRightController();
    }

    public MatchConfig getConfig() {
        return config;
    }

    public Field getField() {
        return field;
    }

    public GameCharacter getLeftCharacter() {
        return leftCharacter;
    }

    public GameCharacter getRightCharacter() {
        return rightCharacter;
    }

    public Player getLeftPlayer() {
        return leftPlayer;
    }

    public Player getRightPlayer() {
        return rightPlayer;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isStarted() {
        return started;
    }
}
