package presentation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.Ball;
import domain.FreezePower;
import domain.GameCharacter;
import domain.GameMode;
import domain.HumanController;
import domain.MachineController;
import domain.Match;
import domain.MatchConfig;
import domain.MatchState;
import domain.POOBSoccerException;
import domain.Side;
import domain.Winner;

public class GameSessionTest {

    private GameCharacter leftCharacter;
    private GameCharacter rightCharacter;

    @BeforeEach
    public void setUp() throws POOBSoccerException {
        leftCharacter = new GameCharacter("Left",
                new domain.Country("Left", new FreezePower()), 3, 3, 3);
        rightCharacter = new GameCharacter("Right",
                new domain.Country("Right", new FreezePower()), 3, 3, 3);
    }

    private GameSession session(MatchConfig config) {
        return new GameSession(config, leftCharacter, rightCharacter);
    }

    private void scoreInRightGoal(GameSession session) {
        Match match = session.getMatch();
        Ball ball = match.getBall();
        ball.setPosition(1230, 500);
        ball.setVelocity(0, 0);
    }

    private void scoreInLeftGoal(GameSession session) {
        Match match = session.getMatch();
        Ball ball = match.getBall();
        ball.setPosition(50, 500);
        ball.setVelocity(0, 0);
    }

    @Test
    public void shouldCreateHumanControllersForPvp() {
        GameSession session = session(MatchConfig.time(GameMode.PVP));

        assertTrue(session.getLeftController() instanceof HumanController);
        assertTrue(session.getRightController() instanceof HumanController);
        assertFalse(session.isSurvival());
    }

    @Test
    public void shouldCreateHumanAndMachineForPvm() {
        GameSession session = session(MatchConfig.time(GameMode.PVM));

        assertTrue(session.getLeftController() instanceof HumanController);
        assertTrue(session.getRightController() instanceof MachineController);
    }

    @Test
    public void shouldCreateMachineControllersForMvm() {
        GameSession session = session(MatchConfig.time(GameMode.MVM));

        assertTrue(session.getLeftController() instanceof MachineController);
        assertTrue(session.getRightController() instanceof MachineController);
    }

    @Test
    public void shouldStartSingleMatchInPlayingState() {
        GameSession session = session(MatchConfig.time(GameMode.PVP));
        assertFalse(session.isStarted());

        session.start();

        assertTrue(session.isStarted());
        assertNotNull(session.getMatch());
        assertEquals(MatchState.PLAYING, session.getState());
        assertFalse(session.isFinished());
    }

    @Test
    public void shouldAdvanceMatchTimeWhenUpdated() {
        GameSession session = session(MatchConfig.time(GameMode.PVP));
        session.start();
        double before = session.getTimeLeft();

        session.update(0.5);

        assertTrue(session.getTimeLeft() < before);
    }

    @Test
    public void shouldMoveHumanPlayerThroughController() {
        GameSession session = session(MatchConfig.time(GameMode.PVP));
        session.start();
        HumanController controller = (HumanController) session.getLeftController();
        double before = session.getLeftPlayer().getX();

        controller.setDirection(1);
        session.update(0.1);

        assertTrue(session.getLeftPlayer().getX() > before);
    }

    @Test
    public void shouldPauseAndResumeSingleMatch() {
        GameSession session = session(MatchConfig.time(GameMode.PVP));
        session.start();
        session.update(0.5);
        double beforePause = session.getTimeLeft();

        assertTrue(session.pause());
        assertTrue(session.isPaused());
        session.update(0.5);
        assertEquals(beforePause, session.getTimeLeft(), 0.0001);

        assertTrue(session.resume());
        session.update(0.5);
        assertTrue(session.getTimeLeft() < beforePause);
    }

    @Test
    public void shouldNotPauseAfterMatchFinished() {
        GameSession session = session(MatchConfig.goals(GameMode.PVP, 1));
        session.start();
        scoreInRightGoal(session);
        session.update(0.01);
        session.update(1.1);
        assertTrue(session.isFinished());

        assertFalse(session.pause());
        assertFalse(session.isPaused());
    }

    @Test
    public void shouldStartSurvivalSeriesWithHumanAndMachine() {
        GameSession session = session(MatchConfig.survival(2));
        session.start();

        assertTrue(session.isSurvival());
        assertTrue(session.getLeftController() instanceof HumanController);
        assertTrue(session.getRightController() instanceof MachineController);
        assertEquals(0, session.getRoundsWon());
        assertEquals(0, session.getRoundsPlayed());
        assertEquals(2, session.getRemainingBalls(Side.LEFT));
        assertEquals(2, session.getRemainingBalls(Side.RIGHT));
        assertEquals(MatchState.PLAYING, session.getState());
    }

    @Test
    public void shouldAdvanceSurvivalRoundWhenPlayerWins() {
        GameSession session = session(MatchConfig.survival(1));
        session.start();
        Match first = session.getMatch();

        scoreInRightGoal(session);
        session.update(0.01);
        assertEquals(MatchState.GOAL, session.getState());
        assertEquals(0, session.getRoundsPlayed());

        session.update(1.1);

        assertEquals(1, session.getRoundsWon());
        assertEquals(1, session.getRoundsPlayed());
        assertFalse(session.isFinished());
        assertNotSame(first, session.getMatch());
        assertEquals(MatchState.PLAYING, session.getState());
        assertEquals(1, session.getRemainingBalls(Side.LEFT));
        assertEquals(1, session.getRemainingBalls(Side.RIGHT));
    }

    @Test
    public void shouldFinishSurvivalSeriesWhenPlayerLoses() {
        GameSession session = session(MatchConfig.survival(1));
        session.start();

        scoreInLeftGoal(session);
        session.update(0.01);
        session.update(1.1);

        assertTrue(session.isFinished());
        assertEquals(Winner.RIGHT, session.getWinner());
        assertEquals(0, session.getRoundsWon());
        assertEquals(1, session.getRoundsPlayed());
        assertEquals(MatchState.FINISHED, session.getState());
    }

    @Test
    public void shouldAccumulateSeveralSurvivalRoundsWon() {
        GameSession session = session(MatchConfig.survival(1));
        session.start();

        scoreInRightGoal(session);
        session.update(0.01);
        session.update(1.1);
        scoreInRightGoal(session);
        session.update(0.01);
        session.update(1.1);

        assertEquals(2, session.getRoundsWon());
        assertEquals(2, session.getRoundsPlayed());
        assertFalse(session.isFinished());
    }

    @Test
    public void shouldPauseSurvivalRound() {
        GameSession session = session(MatchConfig.survival(1));
        session.start();

        assertTrue(session.pause());
        assertEquals(MatchState.PAUSED, session.getState());
        assertTrue(session.resume());
        assertEquals(MatchState.PLAYING, session.getState());
    }
}
